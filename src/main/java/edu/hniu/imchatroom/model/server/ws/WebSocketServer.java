package edu.hniu.imchatroom.model.server.ws;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import edu.hniu.imchatroom.controller.UserController;
import edu.hniu.imchatroom.model.bean.Message;
import edu.hniu.imchatroom.model.bean.PrivateMessage;
import edu.hniu.imchatroom.model.bean.User;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
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
    private User user;

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
        this.uniqueUserCode = uniqueUserCode;
        this.user = UserController.doGetUserToWebSocket(uniqueUserCode);        // 根据唯一的用户码获取到该用户
        webSocketServerSet.add(this);       // 添加至Set集合中

        try {
            sendMessage("成功建立WebSocket连接！");
            /*log.info("开始监听：{}，用户名：{}，当前用户在线总数量为：{}",
                    uniqueUserCode, user.getNickname(), UserController.getOnlineCount());*/
        } catch (IOException e) {
            log.error("WebSocket IO Exception");
        }
    }

    /**
     * WebSocket 连接关闭时调用的方法
     */
    @OnClose
    public void onClose() {

//        Map<String, User> onlineUserToUseMap = UserController.onlineUserToUseMap;
        // 在断开连接的情况下，更新主板占用情况为释放
//        log.info("释放的id为{}，用户名为：{}", uniqueUserCode, user.getNickname());
        // 写释放时需要处理的业务逻辑
        webSocketServerSet.remove(this);    // 从WebSocketServerSet中移除掉此 WebSocketServer
        log.info("有一连接关闭(可能会是页面刷新)！当前用户在线总数量：{}", UserController.getOnlineCount());
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
        log.info("收到来自窗口：{}，用户：{} 的消息：{}", uniqueUserCode, user.getNickname(), messageJson.getContent());
        final String thisMsgType = messageJson.getMessageType();

        // 群发消息
        Iterator<WebSocketServer> iterator = webSocketServerSet.iterator();
        while (iterator.hasNext()) {

            WebSocketServer next = iterator.next();
            User curUser = next.user;
            try {
                // 匹配消息类型
                if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {  // 私聊消息
                    PrivateMessage messageToUse = JSON.parseObject(message, PrivateMessage.class);
                    if (curUser.equals(messageToUse.getSendUser()) || curUser.equals(messageToUse.getReceiveUser())) {
                        next.sendMessage(JSON.toJSONString(messageToUse));  // 推送消息
//                        this.session.getAsyncRemote().sendText(message);  // 推送消息
                    }

                } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
                    // 群聊消息

                } else if (thisMsgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
                    // 系统公告消息
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        log.info("服务器主动推送内容：{}", message);
        this.session.getBasicRemote().sendText(message);        // 会调用前端WebSocket连接的 onmessage()方法
    }

    /**
     * 群发自定义消息
     * @param message
     * @param user
     */
    /*public static void sendInfo(String message, @PathParam("user") Object user) {
        log.info("推送消息至窗口：{}，推送内容为：{}", user, message);

        Iterator<WebSocketServer> iterator = webSocketServerSet.iterator();
        while (iterator.hasNext()) {
            WebSocketServer next = iterator.next();
            try {
                // 此处可设定之推送给某哦个具体的sid客户端，sid为null则表示全部推送
                if (null == user) {

                } else if (next.user.equals(user)) {
                    next.sendMessage(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    /**
     * 获取所有的WebSocket连接
     * @return
     */
    public static Set<WebSocketServer> getWebSocketServerSet() {
        return webSocketServerSet;
    }

}
