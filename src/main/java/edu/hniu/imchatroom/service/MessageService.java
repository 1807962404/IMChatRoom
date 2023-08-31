package edu.hniu.imchatroom.service;

import edu.hniu.imchatroom.model.bean.Message;

import java.util.List;

public interface MessageService {

    /**
     * 处理 获取历史消息记录（私聊消息、群聊消息、系统公告通知消息）的业务逻辑
     *  1、查询私聊消息（会根据消息发送者id和消息接收者id进行查询）
     * @param message
     * @return
     */
    List<? extends Message> doGetChatMessage(Message message);

    /**
     * 处理 通信 业务逻辑
     * @param message
     * @return
     */
    Integer doChat(Message message);

    /**
     * 销毁消息
     * @param msgType
     * @param messages
     * @return
     */
    Integer doDestroyMessage(String msgType, List<? extends Message> messages);
}
