package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.MessageMapper;
import edu.hniu.imchatroom.model.bean.messages.*;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private MessageMapper messageMapper;

    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }


    /**
     * 处理 获取历史消息记录（私聊消息、群聊消息、系统公告通知消息）的业务逻辑
     *  1、查询私聊消息（会根据消息发送者id和消息接收者id进行查询）
     * @param message
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<? extends Message> doGetChatMessage(Message message) {

        // 根据消息类型进行分类处理
        String msgType = message.getMessageType();
        List<? extends Message> messages = null;
        if (msgType.equals(MessageType.getPrivateMessageType())) {
            // 私聊消息（会根据消息发送者uId和消息接收者uId进行查询）
            messages = messageMapper.selectPrivateMessages((PrivateMessage) message);

        } else if (msgType.equals(MessageType.getPublicMessageType())) {
            // 群聊消息（会根据群gId进行查询），查询的群聊消息是在该用户加入此群聊之后的消息
            messages = messageMapper.selectPublicMessage((PublicMessage) message);

        } else if (msgType.equals(MessageType.getSystemMessageType())) {
            // 系统广播消息（会根据系统广播消息发布者uId进行查询）
            messages = messageMapper.selectBroadcastMessage((BroadcastMessage) message);

        } else if (msgType.equals(MessageType.getAbstractMessageType())) {
            // 优文摘要消息（会根据系优文摘要消息发表者uId进行查询）
            messages = messageMapper.selectArticleMessage((ArticleMessage) message);

        } else if (msgType.equals(MessageType.getFeedbackMessageType())) {
            // 意见反馈消息
            messages = messageMapper.selectFeedbackMessage((FeedbackMessage) message);
        }

        // 解密数据
        if (!decryptMessages(messages)) {
            return null;
        }
        return messages;
    }

    /**
     * 处理 通信 业务逻辑
     * @param message
     * @return
     */
    @Override
    public Integer doChat(Message message) {

        int result = -1;
        // 加密消息
        if (!encryptMessage(message))
            return result;

        // 根据消息类型进行分类处理
        String msgType = message.getMessageType();
        message.setDisplayStatus(StatusCode.getNormalStatusCode());

        if (msgType.equals(MessageType.getPrivateMessageType())) {
            // 发送私聊消息
            PrivateMessage privateMessage = (PrivateMessage) message;
            result = messageMapper.insertPriMsg(privateMessage);

        } else if (msgType.equals(MessageType.getPublicMessageType())) {
            // 发送群聊消息
            PublicMessage publicMessage = (PublicMessage) message;
            result = messageMapper.insertPubMsg(publicMessage);

        } else if (msgType.equals(MessageType.getSystemMessageType())) {
            // 发送系统消息
            BroadcastMessage broadcastMessage = (BroadcastMessage) message;
            result = messageMapper.insertSystemMsg(broadcastMessage);

        } else if (msgType.equals(MessageType.getAbstractMessageType())) {
            // 发送优文摘要消息
            ArticleMessage articleMessage = (ArticleMessage) message;
            result = messageMapper.insertArticleMsg(articleMessage);

        } else if (msgType.equals(MessageType.getFeedbackMessageType())) {
            // 发送意见反馈消息
            FeedbackMessage feedbackMessage = (FeedbackMessage) message;
            result = messageMapper.insertFeedbackMsg(feedbackMessage);
        }

        // 解密消息
        if (!decryptMessage(message))
            return -1;

        return result;
    }

    /**
     * 销毁消息
     * @param msgType
     * @param messages
     * @return
     */
    @Override
    public Integer doDestroyMessage(String msgType, List<? extends Message> messages) {

        int result = 0;
        // 根据消息类型进行分类销毁（删除）处理
        if (msgType.equals(MessageType.getPrivateMessageType())) {
            // 私聊消息
            List<PrivateMessage> privateMessages = (List<PrivateMessage>) messages;
            result += messageMapper.deletePriMsg(privateMessages);

        } else if (msgType.equals(MessageType.getPublicMessageType())) {
            // 群聊消息
            List<PublicMessage> publicMessages = (List<PublicMessage>) messages;
            result += messageMapper.deletePubMsg(publicMessages);

        } else if (msgType.equals(MessageType.getSystemMessageType())) {
            // 系统广播消息
            List<BroadcastMessage> broadcastMessages = (List<BroadcastMessage>) messages;
            result += messageMapper.deleteBroMsg(broadcastMessages);

        } else if (msgType.equals(MessageType.getAbstractMessageType())) {
            // 优文摘要消息
            List<ArticleMessage> articleMessages = (List<ArticleMessage>) messages;
            result += messageMapper.deleteArtMsg(articleMessages);
        }

        return result;
    }

    /**
     * 消息加密
     * @param message
     */
    @Override
    public boolean encryptMessage(Message message) {
        if (null != message) {
            String encryptedText = null;
            try {
                encryptedText = EncryptUtil.encryptText(String.valueOf(message.getContent()));
                message.setContent(encryptedText);
            } catch (Exception e) {
                System.out.println("Encrypt processing failed: " + e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }
    /**
     * 消息加密
     * @param messages
     */
    @Override
    public boolean encryptMessages(List<? extends Message> messages) {
        if (null != messages) {
            if (!messages.isEmpty()) {
                for (Message message : messages) {
                    boolean isSuccess = encryptMessage(message);    // 加密消息
                    if (!isSuccess)     // 若在加密过程中有一次不成功则返回false
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * 消息解密
     * @param message
     */
    @Override
    public boolean decryptMessage(Message message) {
        if (null != message) {
            String decryptedText = null;
            try {
                decryptedText = EncryptUtil.decryptText(String.valueOf(message.getContent()));
                message.setContent(decryptedText);
            } catch (Exception e) {
                System.out.println("Decrypt processing failed: " + e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }
    /**
     * 消息解密
     * @param messages
     */
    @Override
    public boolean decryptMessages(List<? extends Message> messages) {
        if (null != messages) {
            if (!messages.isEmpty()) {
                for (Message message : messages) {
                    boolean isSuccess = decryptMessage(message);    // 解密消息
                    if (!isSuccess)     // 若在解密过程中有一次不成功则返回false
                        return false;
                }
            }
        }
        return true;
    }
}
