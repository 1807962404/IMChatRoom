package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.MessageMapper;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.service.MessageService;
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
        if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 私聊消息（会根据消息发送者uId和消息接收者uId进行查询）
            messages = messageMapper.selectPrivateMessages((PrivateMessage) message);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息（会根据群gId进行查询），查询的群聊消息是在该用户加入此群聊之后的消息
            messages = messageMapper.selectPublicMessage((PublicMessage) message);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 系统广播消息（会根据系统广播消息发布者uId进行查询）
            messages = messageMapper.selectBroadcastMessage((BroadcastMessage) message);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // 优文摘要消息（会根据系优文摘要消息发表者uId进行查询）
            messages = messageMapper.selectArticleMessage((ArticleMessage) message);
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

        // 根据消息类型进行分类处理
        String msgType = message.getMessageType();
        if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 发送私聊消息
            PrivateMessage privateMessage = (PrivateMessage) message;
            return messageMapper.insertPriMsg(privateMessage);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 发送群聊消息
            PublicMessage publicMessage = (PublicMessage) message;
            return messageMapper.insertPubMsg(publicMessage);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 发送系统消息
            BroadcastMessage broadcastMessage = (BroadcastMessage) message;
            return messageMapper.insertSystemMsg(broadcastMessage);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // 发送优文摘要消息
            ArticleMessage articleMessage = (ArticleMessage) message;
            return messageMapper.insertArticleMsg(articleMessage);
        }

        return -1;
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
        if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 私聊消息
            List<PrivateMessage> privateMessages = (List<PrivateMessage>) messages;
            result += messageMapper.deletePriMsg(privateMessages);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息
            List<PublicMessage> publicMessages = (List<PublicMessage>) messages;
            result += messageMapper.deletePubMsg(publicMessages);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 系统广播消息
            List<BroadcastMessage> broadcastMessages = (List<BroadcastMessage>) messages;
            result += messageMapper.deleteBroMsg(broadcastMessages);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG))) {
            // 优文摘要消息
            List<ArticleMessage> articleMessages = (List<ArticleMessage>) messages;
            result += messageMapper.deleteArtMsg(articleMessages);
        }

        return result;
    }
}
