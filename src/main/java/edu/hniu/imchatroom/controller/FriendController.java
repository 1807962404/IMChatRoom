package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.*;

@Slf4j
@RestController
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
    @GetMapping("/get-my-friends")
    public List<Friend> doGetMyFriends(HttpServletRequest request) {
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
    @PostMapping("/find-friend")
    public ResultVO<List<User>> doFindFriends(String data) {

        log.info("搜索用户的data为: {}", data);
        // 1、检查传入的数据是否为空
        if (StringUtil.isEmpty(data)) {
            log.warn("搜索用户的内容不能为空！", data);
            return Result.warn("搜索用户的内容不能为空！");
        }

        // 2、检查用户是否存在
        List<User> userToUse = userService.doGetUsersByFuzzyQuery(data);
        // 需要排除未激活的用户账号
        Iterator<User> iterator = userToUse.iterator();
        while (iterator.hasNext()) {
            User next = iterator.next();
            // 该用户账号处于非激活状态
            if (!next.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                log.info("Invalid User: {}", next);
                iterator.remove();
            }
        }
        // 不存在此用户
        if (userToUse.isEmpty()) {
            log.warn("未搜索到账号为：'{}' 的用户，请查证后再试！", data);
            return Result.warn("未搜索到账号为：'" + data + "' 的用户，请查证后再试！");
        }

        log.info("已查找出账号信息大致为：{} 的用户信息 {}！", data, userToUse);
        return Result.ok("已查找出账号信息大致为：" + data + " 的用户信息！", userToUse);
    }

    /**
     * 添加好友
     * @param uId
     * @return
     */
    @PostMapping("/add-friend/{uId}")
    public ResultVO<FriendShip> doAddFriend(@PathVariable("uId") String uId, HttpServletRequest request) {

        log.info("添加好友的uId为：{}", uId);

        // 1、判断用户Id是否为空，若为空则报错
        if (StringUtil.isEmpty(uId)) {
            log.warn("需要添加好友的用户Id不能为空！");
            return Result.failed("需要添加好友的用户Id不能为空！");
        }

        // 2、检查是否存在该uId的用户
        User friendUser = userService.doGetUserById(Integer.valueOf(uId));
        if (null == friendUser) {
            log.warn("添加好友失败，uId为：{} 的用户不存在！", uId);
            return Result.failed("添加好友失败，uId为：" + uId + "的用户不存在！");
        }

        // 3、检查需要添加的好友是否为本人账号
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        if (friendUser.equals(thisUser)) {
            log.warn("无法添加自己的账号为好友！");
            return Result.warn("无法添加自己的账号为好友！");
        }

        // 3、检查该uId的用户是否 已经是自己的好友 或者为 已发送过好友请求，正处于好友关系确认中
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, null);
        log.info("{} 与 {} 间的好友关系为：{}", thisUser.getNickname(), friendUser.getNickname(),
                friendShip == null ? "暂无关系！" : friendShip);

        if (null != friendShip) {
            // 若对方账号尚未激活，也不可添加好友！
            if (!friendShip.getFriendUser().getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                log.warn("添加好友失败，uId为：{} 的用户 {} 的账号尚未激活！", uId, friendUser.getNickname());
                return Result.failed("添加好友失败，uId为：" + uId + " 的用户 " + friendUser.getNickname() + " 的账号尚未激活！");
            }

            if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND))) {
                log.warn("添加好友失败，uId为：{} 的用户 {} 已经是您的朋友了！", uId, friendUser.getNickname());
                return Result.warn("添加好友失败，uId为：" + uId + " 的用户 " + friendUser.getNickname() + " 已经是您的好友了！");

            } else if (friendShip.getFsStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING))) {
                // 如果好友关系正在确认中，则判断 对方是否给我发送过好友申请。如果有则将两人的关系直接升为好友，否则提示错误信息
                if (friendUser.getUId() == friendShip.getHostUser().getUId()) {
                    log.warn("对方已给我发送过好友请求，所以我们可以直接成为朋友！");
                    /**
                     * 对方（该uId用户）已经向本人 发送过好友申请，直接升级为好友关系
                     * 只需更改好友关系状态为正常状态，并向friend表中插入一条数据即可
                     */
                    int result = friendService.doMakeFriend(friendShip.getFsId());
                    if (2 != result) {
                        log.warn("添加好友失败！");
                        return Result.failed("添加好友失败！");

                    }

                    log.info("已成功与用户：{} 成为好友！", friendUser.getNickname());
                    // 设置二者友谊状况为：已是好友关系
                    return Result.ok("已成功与用户：" + friendUser.getNickname() + " 成为好友！",
                            friendService.doCheckIsFriend(thisUser, friendUser,
                                    StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND)));

                } else {
                    log.warn("您已发送过此好友请求，请耐心等待好友确认！");
                    return Result.warn("您已发送过此好友请求，请耐心等待好友确认！");
                }
            }
        }

        // 二者间没有关系：
        // 3、发送好友请求
        int result = friendService.doAddFriendShip(thisUser, friendUser);
        if (1 != result) {
            log.warn("发送好友申请失败，请稍后再试！");
            return Result.failed("发送好友申请失败，请稍后再试！");
        }

        log.info("已成功向用户 {} 发送好友请求，请敬候佳音！", friendUser.getNickname());
        // 设置二者友谊状况为：正在验证确认中
        return Result.ok("已成功向用户 " + friendUser.getNickname() + " 发送好友请求，请敬候佳音！",
                friendService.doCheckIsFriend(thisUser, friendUser,
                        StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING)));
    }

    /**
     * 根据好友的id 删除我的好友
     * @param friendId
     * @param request
     * @return
     */
    @PostMapping("/del-friend/{friendId}")
    public ResultVO doDelMyFriend(@PathVariable("friendId") String friendId, HttpServletRequest request) {

        // 1、判断好友Id是否为空，若为空则报错
        if (StringUtil.isEmpty(friendId)) {
            log.warn("需要删除的好友Id不能为空！");
            return Result.failed("需要删除的好友Id不能为空！");
        }

        // 2、查询需要删除的好友是否存在
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));
        if (null == friendUser) {
            log.warn("需要删除的用户id为：{} 的好友不存在！", friendId);
            return Result.failed("需要删除的用户id为：" + friendId + " 的好友不存在！");
        }

        // 3、检查 thisUser 和 friendUser 是否为好友关系状态
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));
        if (null == friendShip) {
            log.warn("需要删除的用户id为：{} 的用户 {} 不是您的好友！", friendId, friendUser.getNickname());
            return Result.failed("需要删除的用户id为：" + friendId + " 的用户 " + friendUser.getNickname() + " 不是您的好友！");
        }

        // 3、删除好友（存在好友关系）
        int result = friendService.doDelMyFriend(friendShip);
        if (2 > result) {
            log.warn("删除用户id为：{} 的好友：{} 失败！", friendId, friendUser.getNickname());
            return Result.failed("删除用户id为：" + friendId + " 的好友：" + friendUser.getNickname() + " 失败！");
        }

        log.info("已成功删除用户id为：{} 的好友：{}！", friendId, friendUser.getNickname());
        return Result.ok("已成功删除用户id为：" + friendId + " 的好友： " + friendUser.getNickname() + "！");
    }

    /**
     * 查询本人尚未同意好友申请的用户信息（查看新好友通知）
     * @return
     */
    @GetMapping("/find-new-friends")
    public List<FriendShip> doFindMyNewFriends(HttpServletRequest request) {

        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        // 获取其他用户给我发送过的好友请求
        List<FriendShip> ownFriendship = friendService.doGetOwnFriendship(thisUser.getUId());
        /*if (null != ownFriendship) {
            if (!ownFriendship.isEmpty()) {
                // 排除新好友通知中新好友账号已注销的情况
                Iterator<FriendShip> iterator = ownFriendship.iterator();
                while (iterator.hasNext()) {
                    FriendShip next = iterator.next();
                    if (null == next.getHostUser())
                        iterator.remove();
                }
            }
        }*/
        log.info("其他用户给我发送过的好友请求：{}", ownFriendship);
        return ownFriendship;
    }
}
