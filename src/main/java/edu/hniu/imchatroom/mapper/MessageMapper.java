package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.Message;
import edu.hniu.imchatroom.model.bean.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息处理 映射器
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询私聊消息（会根据消息发送者id和消息接收者id进行查询）
     * @param privateMessage
     * @return
     */
    List<PrivateMessage> selectPrivateMessages(@Param("privateMessage") PrivateMessage privateMessage);

    /**
     * 插入私聊消息
     * @param privateMessage
     * @return
     */
    Integer insertPriMsg(@Param("privateMessage") PrivateMessage privateMessage);

    /**
     * 查询私聊消息（会根据消息发送者id和消息接收者id进行查询）
     * @param message
     * @return
     */
//    List<Message> selectMessages(@Param("message") Message message);
}
