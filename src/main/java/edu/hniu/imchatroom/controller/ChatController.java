package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.model.enums.ResponseCodeEnum;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.ChatService;
import edu.hniu.imchatroom.service.FriendService;
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
    private ChatService chatService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public  void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }
    @Autowired
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
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

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 群聊消息

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 系统公告消息

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("消息类型匹配不上：" + thisMsgType + " not equal to " + MessageTypeEnum.values() + "！");
            log.warn("消息类型匹配不上：{} not equal to '{}'！", thisMsgType, MessageTypeEnum.values());
            return resultVO;
        }

        return resultVO;
    }

    /**
     * 处理消息类型：以获取真正的消息类型
     * @param message
     * @return
     */
    private Message doDispatchMessage(Message message) {

        Message messageToUse = new Message();
        final String thisMsgType = message.getMessageType();

        // 1、判断消息类型是否能够匹配
        if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {  // 私聊消息
            messageToUse = new PrivateMessage();

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 群聊消息

        } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 系统公告消息

        } else {
            return null;
        }

        messageToUse.setMessageType(message.getMessageType());
        messageToUse.setContent(message.getContent());
        messageToUse.setSendTime(message.getSendTime());

        return messageToUse;
    }

    private ResultVO<PrivateMessage> doChatToPersonal(Message message, String friendId, User thisUser) {
        ResultVO<PrivateMessage> resultVO = new ResultVO<>();

        // 2、检查传入的好友id是否为空
        if (StringUtil.isEmpty(friendId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的对象不能为空！");
            log.warn("发送消息的对象不能为空！");
            return resultVO;
        }

        // 3、查找该用户id是否存在
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));

        // 4、检查本人与该用户id好友 的友谊状况（即双方是否处于好友阶段）
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
        log.info("本次操作是 {} 给 {} 发送的私聊消息：{}", privateMessage.getSendUser().getNickname(),
                privateMessage.getReceiveUser().getNickname(), privateMessage.getContent());

        // 5、本人发送消息给对方（好友）
        int result = chatService.doChat(privateMessage);
//        int result = 1;

        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setData(privateMessage);
            resultVO.setMsg("消息发送成功！");
            log.warn("消息发送成功！");

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("消息发送失败！");
            log.warn("消息发送失败！");
        }

        return resultVO;
    }

    /*@ResponseBody
    @PostMapping("/to-personal-chat/{friendId}")
    public ResultVO<PrivateMessage> chatToPersonal(Message message,
                                                   @PathVariable("friendId") String friendId,
                                                   HttpServletRequest request
    ) {
        log.info("chatToPersonal's Message: {}", message);

        ResultVO<PrivateMessage> resultVO = new ResultVO<>();
        String thisMsgType = MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG);
        // 1、判断消息类型是否能够匹配
        if (!message.getMessageType().equals(thisMsgType)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("消息类型匹配不上：" + thisMsgType + " not equal to " + message.getMessageType() + "！");
            log.warn("消息类型匹配不上：{} not equal to {}！", thisMsgType, message.getMessageType());
            return resultVO;
        }

        // 2、检查传入的好友id是否为空
        if (StringUtil.isEmpty(friendId)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("发送消息的对象不能为空！");
            log.warn("发送消息的对象不能为空！");
            return resultVO;
        }

        // 3、查找该用户id是否存在
        User friendUser = userService.doGetUserById(Integer.valueOf(friendId));

        // 4、检查本人与该用户id好友 的友谊状况（即双方是否处于好友阶段）
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));

        if (null == friendShip) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("您已不是对方的好友，无法向对方发送消息！");
            log.warn("您已不是id为：{} 用户 {} 的好友，无法向对方发送消息！", friendId, friendUser.getNickname());
            return resultVO;
        }

        PrivateMessage privateMessage = (PrivateMessage) message;
        // 设置消息发送者为本人
        privateMessage.setSendUser(thisUser);
        // 设置消息接收者为对方（好友）
        privateMessage.setReceiveUser(friendUser);
//        chatService.doChatToPersonal(privateMessage);

        return resultVO;
    }*/

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
            resultVO.setMsg("需要查找的用户Id不能为空！");
            log.warn("需要查找的用户Id不能为空！");
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
        List<? extends Message> privateMessages = chatService.doGetChatMessage(privateMessage);
        /*System.out.println("getPrivateMessage: ");
        privateMessages.forEach(message -> System.out.println(message));*/

        resultVO.setCode(ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS));
        resultVO.setData(privateMessages);
        log.info("已查询出本人：{} 与用户：{} 间的历史私聊消息！", thisUser.getNickname(), friendUser.getNickname());

        return resultVO;
    }
}
