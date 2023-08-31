package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.MessageMapper;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Override
    public List<? extends Message> doGetChatMessage(Message message) {

        // 根据消息类型进行分类处理
        String msgType = message.getMessageType();
        if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 私聊消息（会根据消息发送者id和消息接收者id进行查询）
            List<PrivateMessage> privateMessages = messageMapper.selectPrivateMessages((PrivateMessage) message);
            return privateMessages;

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息（会根据群gId进行查询）
            List<PublicMessage> publicMessages = messageMapper.selectPublicMessage((PublicMessage) message);
            return publicMessages;

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 系统消息
        }
        return null;
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
        // 根据消息类型进行分类处理
        if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG))) {
            // 私聊消息
            List<PrivateMessage> privateMessages = (List<PrivateMessage>) messages;
            result += messageMapper.deletePriMsg(privateMessages);

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.PUB_MSG))) {
            // 群聊消息

        } else if (msgType.equals(MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG))) {
            // 系统消息
        }

        return result;
    }
}
