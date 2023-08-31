package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.*;
import static edu.hniu.imchatroom.util.VariableUtil.RESPONSE_SUCCESS_CODE;

@Slf4j
@Controller
@RequestMapping("/user")
public class FriendController {

    private UserService userService;
    private FriendService friendService;
    private MessageService messageService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 获取我的好友列表
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/get-my-friends")
    public List<Friend> getMyFriends(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        // 获取本人所有的好友列表
        List<Friend> myFriends = friendService.doGetFriendsByUId(thisUser.getUId());
        log.info("My Friends: {}", myFriends);

        return myFriends;
    }

    /**
     * 根据 data（account或email）查找用户
     * @param data
     * @return
     */
    @ResponseBody
    @PostMapping("/find-friend")
    public ResultVO<List<User>> findFriends(String data) {
        ResultVO<List<User>> resultVO = new ResultVO<>();

        log.info("搜索用户的data为: {}", data);
        // 1、检查传入的数据是否为空
        if (StringUtil.isEmpty(data)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("搜索的用户内容不能为空！");
            log.warn("搜索的用户内容不能为空！", data);
            return resultVO;
        }

        // 2、检查用户是否存在
        List<User> userToUse = userService.doGetUsersByFuzzyQuery(data);
        Iterator<User> iterator = userToUse.iterator();
        while (iterator.hasNext()) {
            User next = iterator.next();
//            该用户账号处于非激活状态
            if (!next.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                log.info("Invalid User: {}", next);
                iterator.remove();
            }
        }
        // 不存在此用户
        if (null == userToUse || userToUse.size() == 0) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("未搜索到账号为：'" + data + "' 的用户，请查证后再试！");
            log.warn("未搜索到账号为：'{}' 的用户，请查证后再试！", data);
            return resultVO;
        }

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(userToUse);
        resultVO.setMsg("已查找出账号信息大致为：" + data + " 的用户信息！");
        log.info("已查找出账号信息大致为：{} 的用户信息 {}！", data, userToUse);
        return resultVO;
    }

    /**
     * 添加好友
     * @param uId
     * @return
     */
    @ResponseBody
    @PostMapping("/add-friend/{uId}")
    public ResultVO<FriendShip> addFriend(@PathVariable("uId") String uId, HttpServletRequest request) {
        ResultVO<FriendShip> resultVO = new ResultVO<>();
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

        // 3、检查需要添加的好友是否为本人账号
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (friendUser.equals(thisUser)) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("无法添加自己的账号为好友！");
            log.warn("无法添加自己的账号为好友！");
            return resultVO;
        }

        // 3、检查该uId的用户是否 已经是自己的好友 或者为 已发送过好友请求，正处于好友关系确认中
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, null);
        log.info("{} 与 {} 间的好友关系为：{}", thisUser.getNickname(), friendUser.getNickname(),
                friendShip == null ? "暂无关系！" : friendShip);

        if (null != friendShip) {
            // 若对方账号尚未激活，也不可添加好友！
            if (!friendShip.getFriendUser().getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("添加好友失败，uId为：" + uId + " 的用户 " + friendUser.getNickname() + " 的账号尚未激活！");
                log.warn("添加好友失败，uId为：{} 的用户 {} 的账号尚未激活！", uId, friendUser.getNickname());
                return resultVO;
            }

            if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND))) {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("添加好友失败，uId为：" + uId + " 的用户 " + friendUser.getNickname() + " 已经是您的朋友了！");
                log.warn("添加好友失败，uId为：{} 的用户 {} 已经是您的朋友了！", uId, friendUser.getNickname());
                return resultVO;

            } else if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING))) {
                // 如果好友关系正在确认中，则判断 对方是否给我发送过好友申请。如果有则将两人的关系直接升为好友，否则提示错误信息
                if (friendUser.getUId() == friendShip.getHostUser().getUId()) {
                    log.info("对方已给我发送过好友请求，所以我们可以直接成为朋友！");
                    /**
                     * 对方（该uId用户）已经向本人 发送过好友申请，直接升级为好友关系
                     * 只需更改好友关系状态为正常状态，并向friend表中插入一条数据即可
                     */
                    int result = friendService.doMakeFriend(friendShip.getFsId());
                    if (2 != result) {
                        resultVO.setCode(RESPONSE_FAILED_CODE);
                        resultVO.setMsg("添加好友失败！");
                        log.warn("添加好友失败！");

                    } else {
                        resultVO.setCode(RESPONSE_SUCCESS_CODE);
                        // 设置二者友谊状况为：已是好友关系
                        resultVO.setData(friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND)));
                        resultVO.setMsg("已成功与用户：" + friendUser.getNickname() + " 成为好友！");
                        log.warn("已成功与用户：{} 成为好友！", friendUser.getNickname());
                    }

                    return resultVO;
                } else {
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("您已发送过此好友请求，请耐心等待好友确认！");
                    log.warn("您已发送过此好友请求，请耐心等待好友确认！");
                    return resultVO;
                }
            }
        }

        // 二者间没有关系：
        // 3、发送好友请求
        int result = friendService.doAddFriendShip(thisUser, friendUser);
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            // 设置二者友谊状况为：正在验证确认中
            resultVO.setData(friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING)));
            resultVO.setMsg("已成功向用户 " + friendUser.getNickname() + " 发送好友请求，请敬候佳音！");
            log.info("已成功向用户 {} 发送好友请求，请敬候佳音！", friendUser.getNickname());

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("发送好友申请失败，请稍后再试！");
            log.info("发送好友申请失败，请稍后再试！");
        }

        return resultVO;
    }

    /**
     * 根据好友的id 删除我的好友
     * @param friendId
     * @param request
     * @return
     */
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

        int result = 0;
        // 2、删除双方的聊天记录
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSendUser(thisUser);
        privateMessage.setReceiveUser(friendUser);
        privateMessage.setMessageType(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG));

        // 2.1、获取双方的聊天记录信息
        List<? extends Message> messages = messageService.doGetChatMessage(privateMessage);
        // 2.2、删除双方的私聊记录
        result += messageService.doDestroyMessage(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG), messages);

        // 3、删除好友（存在好友关系）
        result += friendService.doDelMyFriend(friendShip);

        if (3 != result) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("删除用户id为：" + friendId + " 的好友：" + friendUser.getNickname() + " 失败！");
            log.warn("删除用户id为：{} 的好友：{} 失败！", friendId, friendUser.getNickname());

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("已成功删除用户id为：" + friendId + " 的好友： " + friendUser.getNickname() + "！");
            log.warn("已成功删除用户id为：{} 的好友：{}！", friendId, friendUser.getNickname());
        }

        return resultVO;
    }

    /**
     * 查询本人尚未同意好友申请的用户信息（新好友通知）
     * @return
     */
    @ResponseBody
    @GetMapping("/find-new-friends")
    public List<FriendShip> findMyNewFriends(HttpServletRequest request) {

        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        // 获取其他用户给我发送过的好友请求
        List<FriendShip> ownFriendship = friendService.doGetOwnFriendship(thisUser.getUId());
        log.info("其他用户给我发送过的好友请求：{}", ownFriendship);
        return ownFriendship;
    }
}
