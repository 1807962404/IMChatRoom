package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.model.enums.ResponseCodeEnum;
import edu.hniu.imchatroom.model.enums.RoleEnum;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.*;

/**
 * 收发消息控制器
 */
@Slf4j
@Controller
@RequestMapping("/user/chat")
public class ChatController {

    private UserService userService;
    private FriendService friendService;
    private MessageService messageService;
    private GroupService groupService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public  void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }
    @Autowired
    public void setChatService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @ResponseBody
    @PostMapping("/let-chat/{id}")
    public ResultVO<? extends Message> letUsChat(Message message,
                                                 @PathVariable(value = "id", required = false) String id,
                                                 HttpServletRequest request
    ) {
        log.info("Let us chat's Message: {}", message);

        ResultVO<? extends Message> resultVO = new ResultVO<>();

        // 本人
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 匹配该消息类型
        final String thisMsgType = message.getMessageType();
        // 1、判断消息类型是否能够匹配
        if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {  // 私聊消息

            return doChatToPersonal(message, id, thisUser);

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息
            return doChatToGroup(message, id, thisUser);

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG)) ||
                thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // 系统公告消息、优文摘要消息
            return doChatToEveryone(message, thisUser);

        }

        resultVO.setCode(RESPONSE_FAILED_CODE);
        resultVO.setMsg("消息类型匹配不上：" + thisMsgType + " not equal to " + MessageTypeEnum.values() + "！");
        log.warn("消息类型匹配不上：{} not equal to '{}'！", thisMsgType, MessageTypeEnum.values());
        return resultVO;
    }

    /**
     * 处理消息类型：以获取真正的消息类型
     * @param message
     * @return
     */
    private Message doDispatchMessage(Message message) {

        Message messageToUse;
        final String thisMsgType = message.getMessageType();

        // 1、判断消息类型是否能够匹配
        if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {  // 私聊消息
            messageToUse = new PrivateMessage();

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息
            messageToUse = new PublicMessage();

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 系统公告消息
            messageToUse = new BroadcastMessage();

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // 优文摘要消息
            messageToUse = new ArticleMessage();

        } else {
            return null;
        }

        messageToUse.setMessageType(message.getMessageType());
        messageToUse.setContent(message.getContent());
        messageToUse.setSendTime(message.getSendTime());

        return messageToUse;
    }

    /**
     * 处理私聊消息
     * @param message
     * @param friendId
     * @param thisUser
     * @return
     */
    private ResultVO<PrivateMessage> doChatToPersonal(Message message, String friendId, User thisUser) {
        ResultVO<PrivateMessage> resultVO = new ResultVO<>();

        // 1、检查传入的好友id是否为空
        if (StringUtil.isEmpty(friendId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的对象不能为空！");
            log.warn("发送消息的对象不能为空！");
            return resultVO;
        }

        // 2、查找该用户id是否存在
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));
        if (null == friendUser) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的用户对象不存在！");
            log.warn("发送消息的用户对象不存在！");
            return resultVO;
        }

        // 3、检查本人与该用户id好友 的友谊状况（即双方是否处于好友阶段）
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));

        if (null == friendShip) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您已不是对方的好友，无法向对方发送消息！");
            log.warn("您已不是id为：{} 用户 {} 的好友，无法向对方发送消息！", friendId, friendUser.getNickname());
            return resultVO;
        }

        PrivateMessage privateMessage = (PrivateMessage) doDispatchMessage(message);
        // 设置消息发送者为本人
        privateMessage.setSendUser(thisUser);
        // 设置消息接收者为对方（好友）
        privateMessage.setReceiveUser(friendUser);
        log.info("本次操作是：{} 给好友：{} 发送的私聊消息：{}",
                thisUser.getNickname(), friendUser.getNickname(), message.getContent());

        // 4、本人发送消息给对方（好友）
        int result = messageService.doChat(privateMessage);

        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setData(privateMessage);
            resultVO.setMsg("消息发送成功！");
            log.info("消息发送成功！");

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("消息发送失败！");
            log.warn("消息发送失败！");
        }

        return resultVO;
    }

    /**
     * 处理群聊消息
     * @param message
     * @param gCode
     * @param thisUser
     * @return
     */
    private ResultVO<PublicMessage> doChatToGroup(Message message, String gCode, User thisUser) {

        ResultVO<PublicMessage> resultVO = new ResultVO<>();

        // 1、检查传入的gCode是否为空
        if (StringUtil.isEmpty(gCode)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的群组对象不能为空！");
            log.warn("发送消息的群组对象不能为空！");
            return resultVO;
        }

        // 2、查询出消息需要发送至哪个群组
        Group receiveGroup = groupService.doGetGroupByGCode(gCode);
        if (null == receiveGroup) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的群组对象不存在！");
            log.warn("发送消息的群组对象不存在！");
            return resultVO;
        }

        // 3、检查此用户是否还在该群组中
        GroupUser groupUser = groupService.doCheckUserIsInGroup(receiveGroup.getGId(), thisUser.getUId());
        if (null == groupUser) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您已不在群组：" + receiveGroup.getGName() + " 中了，无法发送消息！");
            log.warn("您已不在群组：{} 中了，无法发送消息！", receiveGroup.getGName());
            return resultVO;
        }

        PublicMessage publicMessage = (PublicMessage) doDispatchMessage(message);
        // 设置消息发送者为本人
        publicMessage.setSendUser(thisUser);
        // 设置消息接收群组的所有群成员
        receiveGroup.setMembers(groupService.doGetGroupsUsersById(receiveGroup.getGId(), null));
        // 设置消息接收者为群组
        publicMessage.setReceiveGroup(receiveGroup);
        log.info("本次操作是：{} 向群组：{} 发送的群聊消息：{}",
                thisUser.getNickname(), receiveGroup.getGName(), message.getContent());

        // 4、本人发送消息给对方（好友）
        int result = messageService.doChat(publicMessage);

        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setData(publicMessage);
            resultVO.setMsg("消息发送成功！");
            log.info("消息发送成功！");

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("消息发送失败！");
            log.warn("消息发送失败！");
        }

        return resultVO;
    }

    /**
     * 管理员用户发布系统广播信息
     * @param message
     * @param thisUser
     * @return
     */
    private ResultVO<Message> doChatToEveryone(Message message, User thisUser) {
        ResultVO<Message> resultVO = new ResultVO<>();

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(RoleEnum.getRoleName(RoleEnum.ADMIN))) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("用户权限不够，无法操作此功能！");
            log.warn("用户权限不够，无法操作此功能！");
            return resultVO;
        }

        int result = 0;
        boolean success = false;
        String responseMsg = "";    // 响应信息
        String thisMsgType = message.getMessageType();
        // 根据消息类别分别操作
        if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // ·系统广播公告信息
            BroadcastMessage broadcastMessage = (BroadcastMessage) doDispatchMessage(message);
            // 设置系统公告发布人
            broadcastMessage.setPublisher(thisUser);

            // 2、发布系统公告
            result = messageService.doChat(broadcastMessage);
            if (1 == result) {
                resultVO.setData(broadcastMessage);
                responseMsg = "系统公告发布成功！";
                success = true;

            } else {
                responseMsg = "系统公告发布失败！";
            }

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // ·优文摘要信息
            ArticleMessage articleMessage = (ArticleMessage) doDispatchMessage(message);
            // 设置优文摘要发表人
            articleMessage.setPublisher(thisUser);

            // 2、发表优文摘要
            result = messageService.doChat(articleMessage);
            if (1 == result) {
                resultVO.setData(articleMessage);
                responseMsg = "优文摘要信息发表成功！";
                success = true;

            } else {
                responseMsg = "优文摘要信息发表失败！";
            }
        }

        resultVO.setMsg(responseMsg);
        if (success) {  // 成功
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            log.info(responseMsg);

        } else {        // 失败
            resultVO.setCode(RESPONSE_FAILED_CODE);
            log.warn(responseMsg);
        }

        return resultVO;
    }

    /**
     * 获取本人与好友间的 历史私聊消息
     * @param friendId
     * @return
     */
    @ResponseBody
    @GetMapping("/private-history-msg/{friendId}")
    public ResultVO<List<? extends Message>> getPrivateMessage(
            @PathVariable("friendId") String friendId,
            HttpServletRequest request
    ) {
        ResultVO<List<? extends Message>> resultVO = new ResultVO<>();
        PrivateMessage privateMessage = new PrivateMessage();

        // 1、判断用户Id是否为空，若为空则报错
        if (StringUtil.isEmpty(friendId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要查找消息的用户Id不能为空！");
            log.warn("需要查找消息的用户Id不能为空！");
            return resultVO;
        }

        // 2、检查是否存在该uId的用户
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));
        if (null == friendUser) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("friendId为：" + friendId + " 的用户不存在，无法获取聊天信息！");
            log.warn("friendId为：{} 的用户不存在，无法获取聊天信息！", friendId);
            return resultVO;
        }

        // 3、设置消息发送者为本人
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        privateMessage.setSendUser(thisUser);
        // 4、设置消息接收者为好友（对方）
        privateMessage.setReceiveUser(friendUser);
        // 5、设置消息类型为：private-message
        privateMessage.setMessageType(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG));

        // 6、查询二人间的 历史私聊消息
        List<? extends Message> privateMessages = messageService.doGetChatMessage(privateMessage);
        /*System.out.println("getPrivateMessage: ");
        privateMessages.forEach(message -> System.out.println(message));*/

        resultVO.setCode(ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS));
        resultVO.setData(privateMessages);
        log.info("已查询出本人：{} 与用户：{} 间的历史私聊消息！", thisUser.getNickname(), friendUser.getNickname());

        return resultVO;
    }

    @ResponseBody
    @GetMapping("/public-history-msg/{gCode}")
    public ResultVO<List<? extends Message>> getPublicMessage(@PathVariable("gCode") String gCode) {
        ResultVO<List<? extends Message>> resultVO = new ResultVO<>();
        PublicMessage publicMessage = new PublicMessage();

        // 1、判断群唯一码gCode是否为空，若为空则报错
        if (StringUtil.isEmpty(gCode)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("需要查找消息的唯一码gCode不能为空！");
            log.warn("需要查找消息的唯一码gCode不能为空！");
            return resultVO;
        }

        // 2、检查是否存在该gCode的群组
        Group group = groupService.doGetGroupByGCode(gCode);
        if (null == group) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("gCode为：" + gCode + " 的群组不存在，无法获取聊天信息！");
            log.warn("gCode为：{} 的用户不存在，无法获取聊天信息！", gCode);
            return resultVO;
        }

        // 3、设置消息接收者为该群组（对方）
        publicMessage.setReceiveGroup(group);
        // 4、设置消息类型为：public-message
        publicMessage.setMessageType(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG));

        // 5、查询指定群组的历史群聊消息
        List<? extends Message> publicMessages = messageService.doGetChatMessage(publicMessage);
        /*System.out.println("getPublicMessage: ");
        publicMessages.forEach(message -> System.out.println(message));*/

        resultVO.setCode(ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS));
        resultVO.setData(publicMessages);
        log.info("已查询出群组：{} 的历史群聊消息！", group.getGName());

        return resultVO;
    }
}
