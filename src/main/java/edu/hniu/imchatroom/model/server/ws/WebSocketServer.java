package edu.hniu.imchatroom.model.server.ws;

import com.alibaba.fastjson2.JSON;
import edu.hniu.imchatroom.controller.UserController;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.bean.messages.*;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @ServerEndpoint：该注解的功能就是将当前类定义成一个 WebSocket服务器端，
 * 注解的value值将被用于监听用户连接的终端访问URL地址。
 * 客户端可以通过该URL连接至WebSocket服务器端
 */
@Slf4j
@Component
@ServerEndpoint("/api/websocket/{uniqueUserCode}")
public class WebSocketServer {

    // CopyOnWriteArraySet是concurrent包下线程安全的Set，用于存放各个客户端对应的 MyWebSocket 对象
    private static Set<WebSocketServer> webSocketServerSet = new CopyOnWriteArraySet<>();
    // 与某个客户端的WebSocket连接会话，需要通过它给客户端发送数据
    private Session session;
    // 接收 uniqueUserCode
    private String uniqueUserCode;
    private User thisUser;

    /**
     * WebSocket 连接建立成功调用的方法
     * @param session
     * @param uniqueUserCode
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uniqueUserCode") String uniqueUserCode) {

        log.info("onOpen() uniqueUserCode: {}", uniqueUserCode);
        if (StringUtil.isEmpty(uniqueUserCode)) {
            log.warn("用户唯一码错误！");
            return;
        }

        this.session = session;
        thisUser = UserController.doGetUserToWebSocket(uniqueUserCode);        // 根据唯一的用户码获取到该用户
        this.uniqueUserCode = uniqueUserCode;
        webSocketServerSet.add(this);       // 添加至Set集合中

        try {
            log.info("服务器onOpen()推送消息：成功建立WebSocket连接！");
            sendMessage("{\"msg\": \"成功建立WebSocket连接！\"}");
        } catch (IOException e) {
            log.error("WebSocket IO Exception");
        }

        // 实时更新用户人数
        realTimeUpdateOnlineUserCount();

        noticeMyFriends(thisUser);
    }

    // 实时更新在线用户人数
    private static void realTimeUpdateOnlineUserCount() {

        Message message = new Message();
        // 1、设置消息类型为：在线人数消息类型
        message.setMessageType(MessageType.getOnlineCountMessageType());
        // 2、设置内容为：在线用户人数数量
        message.setContent(UserController.getOnlineUserCount());

        sendInfo(JSON.toJSONString(message), null);
    }

    // 通知本人的好友：我一上线
    private static void noticeMyFriends(User thisUser) {
        // 1、判断WebSocket服务连接 是否已连接过
        if (!thisUser.isHasSignIn()) {
            List<Friend> myFriends = thisUser.getMyFriends();
            if (null != myFriends && !myFriends.isEmpty()) {
                for (Friend friend : myFriends) {

                    // 2、挨个通知登陆用户的所有好友：该用户上线了
                    User friendUser = friend.getFriendShip().getFriendUser();
                    User hostUser = friend.getFriendShip().getHostUser();

                    if (friend.getFStatus().equals(StatusCode.getNotFriendStatusCode()) ||
                            null == friendUser || null == hostUser)
                        continue;

                    // 3、设置响应消息
                    Message messageToUse = new Message();
                    messageToUse.setMessageType(MessageType.getSignInMessageType());
                    messageToUse.setContent(thisUser);

                    // 4、若登陆用户是该友谊的 好友关系被申请者，就发送用户上线消息给 好友关系申请者
                    if (thisUser.equals(friendUser))
                        sendInfo(JSON.toJSONString(messageToUse), hostUser);

                    // 4、若登陆用户是该友谊的 好友关系申请者，就发送用户上线消息给 好友关系被申请者
                    else if (thisUser.equals(hostUser))
                        sendInfo(JSON.toJSONString(messageToUse), friendUser);
                }
            }
            thisUser.setHasSignIn(true);
        }
    }

    /**
     * WebSocket 连接关闭时调用的方法
     */
    @OnClose
    public void onClose() {
        // 写释放时需要处理的业务逻辑

        // 1、从WebSocketServerSet中移除掉此 WebSocketServer
        webSocketServerSet.remove(this);

        // 2、实时更新用户人数
        realTimeUpdateOnlineUserCount();

        log.info("有一连接关闭(可能会是页面刷新)！当前用户在线总数量：{}", UserController.getOnlineUserCount());
    }

    /**
     * 收到客户端消息后会调用的方法
     * @param session
     * @param message
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        // 获取消息类型
        Message messageJson = JSON.parseObject(message, Message.class);
        log.info("收到来自窗口：{}，用户：{} 的消息：{}", uniqueUserCode, thisUser.getNickname(), messageJson.getContent());
        final String thisMsgType = messageJson.getMessageType();

        if (thisMsgType.endsWith(MessageType.getGroupMessageType())) {
            // 群组消息
            if (thisMsgType.equals(MessageType.getEnterGroupMessageType())) {
                // 加入群组消息
                GroupUser enterGroupUser = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), GroupUser.class);
                sendInfo(JSON.toJSONString(messageJson), enterGroupUser.getGroup().getHostUser());  // 给群主发送消息

            } else if (thisMsgType.equals(MessageType.getAgreeEnterGroupMessageType())) {
                // 同意用户入群申请消息
                GroupUser agreeEnterGroupUser = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), GroupUser.class);
                sendInfo(JSON.toJSONString(messageJson), agreeEnterGroupUser.getMember());  // 给发送此入群申请的用户发送消息

            } else if (thisMsgType.equals(MessageType.getDropMemberFromGroupMessageType())) {
                // 将用户踢出群组消息
                GroupUser dropMemberFromGroupUser = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), GroupUser.class);
                sendInfo(JSON.toJSONString(messageJson), dropMemberFromGroupUser.getMember());  // 给被移出群组的用户发送消息

            } else if (thisMsgType.equals(MessageType.getExitGroupMessageType())) {
                // 退出群组消息
                GroupUser enterGroupUser = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), GroupUser.class);
                sendInfo(JSON.toJSONString(messageJson), enterGroupUser.getGroup().getHostUser());  // 给群主发送消息

            } else if (thisMsgType.equals(MessageType.getDissolveGroupMessageType())) {
                // 解散群组消息
                Group dissolveGroup = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), Group.class);
                for (GroupUser dissolveGroupUser : dissolveGroup.getMembers()) {
                    if (!dissolveGroup.getHostUser().equals(dissolveGroupUser.getMember()))
                        // 依次通知解散群组内的所有成员
                        sendInfo(JSON.toJSONString(messageJson), dissolveGroupUser.getMember());
                }
            }

        } else if (thisMsgType.endsWith(MessageType.getFriendMessageType())) {
            // 朋友消息
            FriendShip friendShip = JSON.parseObject(JSON.toJSONString(messageJson.getContent()), FriendShip.class);
            if (thisMsgType.equals(MessageType.getAddFriendMessageType())) {
                // 申请添加好友 消息
                // 给对方（需要添加的好友）发送 好友申请 消息
                sendInfo(JSON.toJSONString(messageJson), friendShip.getFriendUser());

            } else if (thisMsgType.equals(MessageType.getAgreeAddFriendMessageType())) {
                // 同意好友申请 消息
                // 给好友请求发送者发送 已同意好友申请 消息
                sendInfo(JSON.toJSONString(messageJson), friendShip.getHostUser());
            }

        } else {
            if (thisMsgType.equals(MessageType.getPrivateMessageType())) {
                // 私聊消息
                PrivateMessage messageToUse = JSON.parseObject(message, PrivateMessage.class);
                // 推送给指定用户消息
                sendInfo(JSON.toJSONString(messageToUse), messageToUse.getSendUser());
                sendInfo(JSON.toJSONString(messageToUse), messageToUse.getReceiveUser());

            } else if (thisMsgType.equals(MessageType.getPublicMessageType())) {
                // 群聊消息
                PublicMessage messageToUse = JSON.parseObject(message, PublicMessage.class);
                // 将消息推送给所有在群组内的用户
                List<GroupUser> groupUsers = messageToUse.getReceiveGroup().getMembers();
                for (GroupUser groupUser : groupUsers) {
                    User reveiveUser = groupUser.getMember();
                    sendInfo(JSON.toJSONString(messageToUse), reveiveUser);
                }

            } else if (thisMsgType.equals(MessageType.getSystemMessageType())) {
                // 系统公告消息
                BroadcastMessage messageToUse = JSON.parseObject(message, BroadcastMessage.class);
                sendInfo(JSON.toJSONString(messageToUse), null);        // 系统消息需要推送给所有在线用户

            } else if (thisMsgType.equals(MessageType.getAbstractMessageType())) {
                // 优文摘要消息
                ArticleMessage messageToUse = JSON.parseObject(message, ArticleMessage.class);
                sendInfo(JSON.toJSONString(messageToUse), null);        // 优文摘要消息需要推送给所有在线用户
            }
        }
    }

    /**
     * WebSocket 服务过程中发生错误时调用的方法
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket连接中发生错误！");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
//        log.info("服务器主动推送内容：{}", message);
        this.session.getBasicRemote().sendText(message);        // 会调用前端WebSocket连接的 onmessage()方法
    }

    /**
     * 群发自定义消息
     * @param message
     * @param user
     */
    public static void sendInfo(String message, User user) {
        if (null == user)
            log.info("推送消息至所有用户窗口，推送内容为：{}", message);
        else
            log.info("推送消息至指定用户窗口：{}，推送内容为：{}", user.getNickname(), message);

        Iterator<WebSocketServer> iterator = webSocketServerSet.iterator();
        while (iterator.hasNext()) {
            WebSocketServer next = iterator.next();
            try {
                // 此处可设定之推送给某哦个具体的user客户端，user为null则表示全部推送
                if (null == user) {
                    next.sendMessage(message);

                } else if (next.thisUser.equals(user)) {
                    next.sendMessage(message);
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
