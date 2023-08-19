package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.FriendShip;
import edu.hniu.imchatroom.model.bean.ResultVO;
import edu.hniu.imchatroom.model.bean.User;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static edu.hniu.imchatroom.util.VariableUtil.*;
import static edu.hniu.imchatroom.util.VariableUtil.RESPONSE_SUCCESS_CODE;

@Slf4j
@Controller
@RequestMapping("/user")
public class FriendController {

    private UserService userService;
    private FriendService friendService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }
    /**
     * 根据 data（account或email）查找用户
     * @param data
     * @return
     */
    @ResponseBody
    @PostMapping("/find-friend")
    public ResultVO<User> findUser(String data, HttpServletRequest request) {
        ResultVO<User> resultVO = new ResultVO<>();

        log.info("查找用户的data（account或email）为: {}", data);
        // 1、检查传入的数据是否为空
        if (StringUtil.isEmpty(data)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("账号异常：'" + data + "'，请查证后再试！");

            log.warn("账号异常：'{}'，请查证后再试！", data);
            return resultVO;
        }

        User user = new User();
        user.setEmail(data);

        // 2、检查用户是否存在
        User userToUse = userService.doCheckUserExists(user, false);
        if (null == userToUse) {        // 不存在此用户
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("未搜索到账号为：'" + data + "' 的用户，请查证后再试！");
            log.warn("未搜索到账号为：'{}' 的用户，请查证后再试！", data);
            return resultVO;
        }

        // 3、检查是否为本人账号
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (userToUse.equals(thisUser)) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("无法添加自己的账号为好友！");
            log.warn("无法添加自己的账号为好友！");
            return resultVO;
        }

        // 4、检查输入的账号是否 已经是自己的好友
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, userToUse, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));
        if (null != friendShip) {
            if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND))) {
                resultVO.setCode(RESPONSE_WARNING_CODE);
                resultVO.setMsg("账号为：" + data + "的用户：" + userToUse.getNickname() + " 已经是您的朋友了！");
                log.warn("账号为：{} 的用户：{} 已经是您的朋友了！", data, userToUse.getNickname());
                return resultVO;
            }
        }

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(userToUse);
        resultVO.setMsg("已搜索到账号为：" + data + " 的用户：" + userToUse.getNickname() + " ！");
        log.info("搜索到账号为：{} 的好友信息为：{}", data, userToUse);

        return resultVO;
    }


    /**
     * 添加好友
     * @param uId
     * @return
     */
    @ResponseBody
    @PostMapping("/add-friend/{uId}")
    public ResultVO addFriend(@PathVariable("uId") String uId, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        log.info("添加好友的uId为：{}", uId);

        // 1、判断用户Id是否为空，若为空则报错
        if (StringUtil.isEmpty(uId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要添加的用户Id不能为空！");
            log.warn("需要添加的用户Id不能为空！");
            return resultVO;
        }

        // 2、检查是否存在该uId的用户
        User friendUser = userService.doGetUserById(Integer.valueOf(uId));
        if (null == friendUser) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("添加好友失败，uId为：" + uId + "的用户不存在！");
            log.warn("添加好友失败，uId为：{} 的用户不存在！", uId);
            return resultVO;
        }

        // 3、检查该uId的用户是否 已经是自己的好友 或者为 已发送过好友请求，正处于好友关系确认中
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, null);
        log.info("{} 与 {} 间的好友关系为：{}", thisUser.getNickname(), friendUser.getNickname(),
                friendShip == null ? "暂无关系！" : friendShip);

        if (null != friendShip) {
            if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND))) {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("添加好友失败，uId为：" + uId + " 的用户 " + friendUser.getNickname() + " 已经是您的朋友了！");
                log.warn("添加好友失败，uId为：{} 的用户 {} 已经是您的朋友了！", uId, friendUser.getNickname());
                return resultVO;

            } else if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFRIMFRIEND))) {
                // 如果好友关系正在确认中，则判断 对方是否给我发送过好友申请。如果有则将两人的关系直接升为好友，否则提示错误信息
                if (friendUser.getUId() == friendShip.getHostUser().getUId()) {
                    log.info("对方已给我发送过好友请求，所以我们可以直接成为朋友！");
                    /**
                     * 对方（该uId用户）已经向本人 发送过好友申请，直接升级为好友关系
                     * 只需更改好友关系状态为正常状态，并向friend表中插入一条数据即可
                     */
                    int result = friendService.doMakeFriend(friendShip.getFsId());
                    if (2 != result) {
                        resultVO.setCode(RESPONSE_WARNING_CODE);
                        resultVO.setMsg("添加好友失败！");
                        log.warn("添加好友失败！");
                        return resultVO;
                    }

                    resultVO.setCode(RESPONSE_SUCCESS_CODE);
                    resultVO.setMsg("已成功与用户：" + friendUser.getNickname() + " 成为好友！");
                    log.warn("已成功与用户：{} 成为好友！", friendUser.getNickname());

                    /**
                     * 3、更新两个用户的好友列表
                     *  因为无法跟踪会话，所以无法做到同时更新两个用户的好友列表，只能更新当前删除对方好友的会话状态。
                     *  在删除对方好友后，若对方再次发消息来则能够提示已不是他的好友！
                     */
                    thisUser.setMyFriendList(friendService.doGetFriendsByUId(thisUser.getUId()));
                    request.getSession().setAttribute(SIGNINED_USER, thisUser);
                    return resultVO;

                } else {
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("您已发送过此好友请求，请耐心等待好友确认！");
                    log.warn("您已发送过此好友请求，请耐心等待好友确认！");
                    return resultVO;
                }
            }
        }

        //

        // 3、发送好友请求
        int result = friendService.doAddFriendShip(thisUser, friendUser);
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已成功向用户 " + friendUser.getNickname() + " 发送好友请求，请敬候佳音！");
            log.info("已成功向用户 {} 发送好友请求，请敬候佳音！", friendUser.getNickname());

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("发送好友申请失败，请稍后再试！");
            log.info("发送好友申请失败，请稍后再试！");
        }

        return resultVO;
    }

    @ResponseBody
    @PostMapping("/del-friend/{friendId}")
    public ResultVO delMyFriend(@PathVariable("friendId") String friendId, HttpServletRequest request) {

        ResultVO resultVO = new ResultVO();

        // 1、判断好友Id是否为空，若为空则报错
        if (StringUtil.isEmpty(friendId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要删除的好友Id不能为空！");
            log.warn("需要删除的好友Id不能为空！");
            return resultVO;
        }

        // 2、查询需要删除的好友是否存在
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));
        if (null == friendUser) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要删除的用户id为：" + friendId + " 的好友不存在！");
            log.warn("需要删除的用户id为：{} 的好友不存在！", friendId);
            return resultVO;
        }

        // 3、检查 thisUser 和 friendUser 是否为好友关系状态
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));
        if (null == friendShip) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要删除的用户id为：" + friendId + " 的用户 " + friendUser.getNickname() + " 不是您的好友！");
            log.warn("需要删除的用户id为：{} 的用户 {} 不是您的好友！", friendId, friendUser.getNickname());
            return resultVO;
        }

        // 2、删除好友（存在好友关系）
        int result = friendService.doDelMyFriend(friendShip);
        if (2 != result) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("删除用户id为：" + friendId + " 的好友：" + friendUser.getNickname() + " 失败！");
            log.warn("删除用户id为：{} 的好友：{} 失败！", friendId, friendUser.getNickname());
            return resultVO;
        }

        /**
         * 3、更新两个用户的好友列表
         *  因为无法跟踪会话，所以无法做到同时更新两个用户的好友列表，只能更新当前删除对方好友的会话状态。
         *  在删除对方好友后，若对方再次发消息来则能够提示已不是他的好友！
         */
        thisUser.setMyFriendList(friendService.doGetFriendsByUId(thisUser.getUId()));
        request.getSession().setAttribute(SIGNINED_USER, thisUser);
        /*Set<Map.Entry<String, User>> entriesUser = userToUseMap.entrySet();
        Iterator<Map.Entry<String, User>> iteratorUsers = entriesUser.iterator();
        while (iteratorUsers.hasNext()) {
            Map.Entry<String, User> nextUser = iteratorUsers.next();
            User user = nextUser.getValue();
            if (user.getUId() == thisUser.getUId() || user.getUId() == friendUser.getUId()) {
                user.setMyFriendList(friendService.doGetFriendsByUId(thisUser.getUId()));
                request.getSession().setAttribute(SIGNINED_USER, thisUser);
            }
        }*/

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setMsg("已成功删除用户id为：" + friendId + " 的好友： " + friendUser.getNickname() + "！");
        log.warn("已成功删除用户id为：{} 的好友：{}！", friendId, friendUser.getNickname());


        return resultVO;
    }
}
