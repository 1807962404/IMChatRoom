package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.bean.messages.*;
import edu.hniu.imchatroom.model.bean.response.Result;
import edu.hniu.imchatroom.model.bean.response.ResultVO;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.MoreUtil;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static edu.hniu.imchatroom.util.VariableUtil.*;

/**
 * 收发消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/chat")
public class ChatController {

    private UserService userService;
    private FriendService friendService;
    private GroupService groupService;
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
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 处理消息转发
     *
     * @param message
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/communicate/{id}")
    public ResultVO<? extends Message> doCommunication(Message message,
                                                       @PathVariable(value = "id", required = false) String id,
                                                       HttpServletRequest request
    ) {
        log.info("Let us chat's or communicate Message: {}", message);

        // 1、检查消息是否为空
        if (null == message || StringUtil.isEmpty(String.valueOf(message.getContent()))) {
            return Result.failed("发送失败，消息不能为空！");
        }

        // 本人
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 匹配该消息类型
        final String thisMsgType = message.getMessageType();
        // 2、判断消息类型是否能够匹配
        if (thisMsgType.equals(MessageType.getPrivateMessageType())) {  // 私聊消息

            return doChatToPersonal(message, id, thisUser);

        } else if (thisMsgType.equals(MessageType.getPublicMessageType())) {
            // 群聊消息
            return doChatToGroup(message, id, thisUser);

        } else if (thisMsgType.equals(MessageType.getSystemMessageType()) ||
                thisMsgType.equals(MessageType.getAbstractMessageType())) {
            // 系统公告消息、优文摘要消息
            return doChatToEveryone(message, thisUser, request);

        } else if (thisMsgType.equals(MessageType.getFeedbackMessageType())) {
            // 意见反馈消息
            return doChatToSystem(message, thisUser);
        }

        log.warn("消息类型匹配不上：{} not equal to '{}'！", thisMsgType, MessageType.getAllMessageTypes());
        return Result.failed("未匹配上消息类型：" + thisMsgType + " ！");
    }

    /**
     * 处理匹配消息类型：获取真正的消息类型
     *
     * @param message
     * @return
     */
    private Message doDispatchMessage(Message message) {

        Message messageToUse;
        final String thisMsgType = message.getMessageType();

        // 1、判断消息类型是否能够匹配
        if (thisMsgType.equals(MessageType.getPrivateMessageType())) {  // 私聊消息
            messageToUse = new PrivateMessage();

        } else if (thisMsgType.equals(MessageType.getPublicMessageType())) {
            // 群聊消息
            messageToUse = new PublicMessage();

        } else if (thisMsgType.equals(MessageType.getSystemMessageType())) {
            // 系统公告消息
            messageToUse = new BroadcastMessage();

        } else if (thisMsgType.equals(MessageType.getAbstractMessageType())) {
            // 优文摘要消息
            messageToUse = new ArticleMessage();

        } else if (thisMsgType.equals(MessageType.getFeedbackMessageType())) {
            // 意见反馈消息
            messageToUse = new FeedbackMessage();

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
     *
     * @param message
     * @param friendId
     * @param thisUser
     * @return
     */
    private ResultVO<PrivateMessage> doChatToPersonal(Message message, String friendId, User thisUser) {

        // 1、检查传入的好友id是否为空
        if (StringUtil.isEmpty(friendId)) {
            log.warn("消息发送失败，发送消息的好友对象不能为空！");
            return Result.failed("消息发送失败，发送消息的好友对象不能为空！");
        }

        // 2、查找该用户id是否存在
        User friendUser = userService.doGetUserById(Long.valueOf(friendId));
        if (null == friendUser) {
            log.warn("消息发送失败，发送消息的用户对象不存在！");
            return Result.failed("消息发送失败，发送消息的用户对象不存在！");
        }

        // 3、检查本人与该用户id好友 的友谊状况（即双方是否处于好友阶段）
        FriendShip friendShip = friendService.doCheckIsFriend(thisUser, friendUser, StatusCode.getIsFriendStatusCode());
        if (null == friendShip) {
            log.warn("您已不是id为：{} 用户 {} 的好友，无法向对方发送消息！", friendId, friendUser.getNickname());
            return Result.failed("您已不是对方的好友，无法向对方发送消息！");
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
        if (1 != result) {
            log.warn("消息发送失败！");
            return Result.failed("消息发送失败！");
        }

        log.info("消息发送成功！");
        return Result.ok("消息发送成功！", privateMessage);
    }

    /**
     * 处理群聊消息
     *
     * @param message
     * @param gCode
     * @param thisUser
     * @return
     */
    private ResultVO<PublicMessage> doChatToGroup(Message message, String gCode, User thisUser) {

        // 1、检查传入的gCode是否为空
        if (StringUtil.isEmpty(gCode)) {
            log.warn("消息发送失败，发送消息的群组对象不能为空！");
            return Result.failed("消息发送失败，发送消息的群组对象不能为空！");
        }

        // 2、查询出消息需要发送至哪个群组
        Group receiveGroup = groupService.doGetGroupByGCode(gCode);
        if (null == receiveGroup) {
            log.warn("消息发送失败，发送消息的群组对象不存在！");
            return Result.failed("消息发送失败，发送消息的群组对象不存在！");
        }

        // 3、检查此用户是否还在该群组中
        GroupUser groupUser = groupService.doCheckUserIsInGroup(receiveGroup.getGId(), thisUser.getUId());
        if (null == groupUser) {
            log.warn("您已不在群组：{} 中了，无法发送消息！", receiveGroup.getGName());
            return Result.failed("您已不在群组：" + receiveGroup.getGName() + " 中了，无法发送消息！");
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
        if (1 != result) {
            log.warn("消息发送失败！");
            return Result.failed("消息发送失败！");
        }

        log.info("消息发送成功！");
        return Result.ok("消息发送成功！", publicMessage);
    }

    /**
     * 管理员用户发布系统广播信息
     *
     * @param message
     * @param thisUser
     * @return
     */
    private ResultVO<Message> doChatToEveryone(Message message, User thisUser, HttpServletRequest request) {

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(ADMIN_USER_NAME)) {
            log.warn("用户权限不够，无法操作此功能！");
            return Result.failed("用户权限不够，无法操作此功能！");
        }

        HttpSession session = request.getSession();

        int result = 0;
        String thisMsgType = message.getMessageType();
        // 根据消息类别分别操作
        if (thisMsgType.equals(MessageType.getSystemMessageType())) {
            // ·系统广播公告信息
            BroadcastMessage broadcastMessage = (BroadcastMessage) doDispatchMessage(message);
            // 设置系统公告发布人
            broadcastMessage.setPublisher(thisUser);

            // 2、发布系统公告
            result = messageService.doChat(broadcastMessage);
            if (1 != result) {
                return Result.failed("系统公告发布失败！");

            } else {
                // 检查系统广播消息是否为空，若true则放入session中
                boolean isEmpty = false;
                Object broadcastsSessionMessages = session.getAttribute(BROADCAST_MESSAGE_NAME);
                if (null != broadcastsSessionMessages && broadcastsSessionMessages instanceof List) {
                    List messages = (List) broadcastsSessionMessages;
                    if (messages.isEmpty())
                        isEmpty = true;
                    else {
                        // 不为空，则添加至系统广播消息列表中
                        List list = Arrays.asList(MoreUtil.mergeArrays(messages.toArray(),
                                new BroadcastMessage[]{broadcastMessage}));
                        session.setAttribute(BROADCAST_MESSAGE_NAME, list);
                    }
                } else
                    isEmpty = true;

                if (isEmpty)
                    session.setAttribute(BROADCAST_MESSAGE_NAME, List.of(broadcastMessage));
                return Result.ok("系统公告发布成功！", broadcastMessage);
            }

        } else if (thisMsgType.equals(MessageType.getAbstractMessageType())) {
            // ·优文摘要信息
            ArticleMessage articleMessage = (ArticleMessage) doDispatchMessage(message);
            // 设置优文摘要发表人
            articleMessage.setPublisher(thisUser);

            // 2、发表优文摘要
            result = messageService.doChat(articleMessage);
            if (1 != result) {
                return Result.failed("优文摘要信息发表失败！");

            } else {
                // 检查优文摘要消息是否为空，若true则放入session中
                boolean isEmpty = false;
                Object articleSessionMessages = session.getAttribute(ARTICLE_MESSAGE_NAME);
                if (null != articleSessionMessages && articleSessionMessages instanceof List) {
                    List messages = (List) articleSessionMessages;
                    if (messages.isEmpty())
                        isEmpty = true;
                    else {
                        // 不为空，则添加至优文摘要消息列表中
                        List list = Arrays.asList(MoreUtil.mergeArrays(messages.toArray(),
                                new ArticleMessage[]{articleMessage}));
                        session.setAttribute(ARTICLE_MESSAGE_NAME, list);
                    }
                } else
                    isEmpty = true;

                if (isEmpty)
                    session.setAttribute(ARTICLE_MESSAGE_NAME, List.of(articleMessage));

                return Result.ok("优文摘要信息发表成功！", articleMessage);
            }
        }

        return Result.failed("管理员发布广播信息过程中出现未知错误！");
    }

    private ResultVO<? extends Message> doChatToSystem(Message message, User thisUser) {

        String thisMsgType = message.getMessageType();
        // 根据消息类别分别操作
        if (thisMsgType.equals(MessageType.getFeedbackMessageType())) {
            // ·意见反馈信息
            FeedbackMessage feedbackMessage = (FeedbackMessage) doDispatchMessage(message);
            // 设置意见反馈 反馈人
            feedbackMessage.setPublisher(thisUser);

            int result = messageService.doChat(feedbackMessage);
            if (1 != result) {
                return Result.failed("意见反馈失败！");

            } else {
                return Result.ok("意见反馈成功！", feedbackMessage);
            }
        }

        return Result.failed("用户操作意见反馈过程中出现未知错误！");
    }

    /**
     * 获取本人与好友间的 历史私聊消息
     *
     * @param friendId
     * @return
     */
    @GetMapping("/private-history-msg/{friendId}")
    public ResultVO<List<? extends Message>> doGetPrivateMessage(
            @PathVariable("friendId") String friendId,
            HttpServletRequest request
    ) {

        PrivateMessage privateMessage = new PrivateMessage();

        // 1、判断用户Id是否为空，若为空则报错
        if (StringUtil.isEmpty(friendId)) {
            log.warn("需要查看私聊历史消息的用户Id不能为空！");
            return Result.failed("需要查看私聊历史消息的用户Id不能为空！");
        }

        // 2、检查是否存在该uId的用户
        User friendUser = userService.doGetUserById(Long.valueOf(friendId));
        if (null == friendUser) {
            log.warn("friendId为：{} 的用户不存在，无法获取聊天信息！", friendId);
            return Result.failed("friendId为：" + friendId + " 的用户不存在，无法获取聊天信息！");
        }

        // 3、设置消息发送者为本人
        final User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        privateMessage.setSendUser(thisUser);
        // 4、设置消息接收者为好友（对方）
        privateMessage.setReceiveUser(friendUser);
        // 5、设置消息类型为：private-message
        privateMessage.setMessageType(MessageType.getPrivateMessageType());

        // 6、查询二人间的 历史私聊消息
        List<? extends Message> privateMessages = messageService.doGetChatMessage(privateMessage);
        /*System.out.println("doGetPrivateMessage: ");
        privateMessages.forEach(message -> System.out.println(message));*/

        log.info("已查询出本人：{} 与用户：{} 间的历史私聊消息！", thisUser.getNickname(), friendUser.getNickname());
        return Result.ok(privateMessages);
    }


    /**
     * 根据gCode查询指定群组的 历史群聊消息
     *
     * @param gCode
     * @return
     */
    @GetMapping("/public-history-msg/{gCode}")
    public ResultVO<List<? extends Message>> doGetPublicMessage(
            @PathVariable("gCode") String gCode,
            HttpServletRequest request
    ) {

        PublicMessage publicMessage = new PublicMessage();

        // 1、判断群唯一码gCode是否为空，若为空则报错
        if (StringUtil.isEmpty(gCode)) {
            log.warn("需要查看群聊消息的群聊唯一码不能为空！");
            return Result.failed("需要查看群聊消息的群聊唯一码不能为空！");
        }

        // 2、检查是否存在该gCode的群组
        Group group = groupService.doGetGroupByGCode(gCode);
        if (null == group) {
            log.warn("群聊唯一码gCode为：{} 的用户不存在，无法获取聊天信息！", gCode);
            return Result.failed("群聊唯一码为：" + gCode + " 的群组不存在，无法获取聊天信息！");
        }

        // 3、设置消息接收者为该群组（对方）
        publicMessage.setReceiveGroup(group);
        // 4、设置消息类型为：public-message
        publicMessage.setMessageType(MessageType.getPublicMessageType());

        // 5、查询指定群组的历史群聊消息（获取该用户加入该群组的时间后的群聊消息）
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        publicMessage.setSendUser(thisUser);
        List<? extends Message> publicMessages = messageService.doGetChatMessage(publicMessage);
        /*System.out.println("doGetPublicMessage: ");
        publicMessages.forEach(message -> System.out.println(message));*/

        log.info("已查询出群组：{} 的历史群聊消息！", group.getGName());
        return Result.ok(publicMessages);
    }

    /**
     * 获取用户管理员发布过的所有系统广播信息
     * @param request
     * @return
     */
    @GetMapping("/admin-published-broadcasts")
    public ResultVO<? extends Message> doGetPublishedBroadcasts(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(ADMIN_USER_NAME)) {
            log.warn("用户权限不够，无法访问！");
            return Result.failed("用户权限不够，无法访问！");
        }

        // 2、查询此管理员用户发布过的系统广播
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setMessageType(MessageType.getSystemMessageType());
        broadcastMessage.setPublisher(thisUser);
        List<? extends Message> messages = messageService.doGetChatMessage(broadcastMessage);
        log.info("已查询出管理员用户：{} 发布过的所有广播信息！", thisUser.getNickname());

        return Result.ok(messages);
    }

    /**
     * 获取用户管理员发布过的所有优文摘要信息
     * @param request
     * @return
     */
    @GetMapping("/admin-published-articles")
    public ResultVO<? extends Message> doGetPublishedArticles(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(ADMIN_USER_NAME)) {
            log.warn("用户权限不够，无法访问！");
            return Result.failed("用户权限不够，无法访问！");
        }

        // 2、查询此管理员用户发布过的优文摘要
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setMessageType(MessageType.getAbstractMessageType());
        articleMessage.setPublisher(thisUser);
        List<? extends Message> messages = messageService.doGetChatMessage(articleMessage);
        log.info("已查询出管理员用户：{} 发布过的所有优文摘要信息！", thisUser.getNickname());

        return Result.ok(messages);
    }

    /**
     * 获取系统中所有的意见反馈信息
     * @return
     */
    @GetMapping("/feedback-history-msg")
    public List<? extends Message> doGetFeedbackMessage() {

        FeedbackMessage feedbackMessage = new FeedbackMessage();
        feedbackMessage.setMessageType(MessageType.getFeedbackMessageType());
        List<? extends Message> feedbackMessages = messageService.doGetChatMessage(feedbackMessage);

        /*System.out.println("doGetFeedbackMessage: ");
        feedbackMessages.forEach(message -> System.out.println(message));*/

        log.info("已查询出系统所有的意见反馈信息！");
        return feedbackMessages;
    }
}
