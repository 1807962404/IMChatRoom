package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.BroadcastMessage;
import edu.hniu.imchatroom.model.bean.Message;
import edu.hniu.imchatroom.model.bean.PrivateMessage;
import edu.hniu.imchatroom.model.bean.PublicMessage;
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
     * 查询群聊消息（会根据群gId进行查询）
     * @param publicMessage
     * @return
     */
    List<PublicMessage> selectPublicMessage(@Param("publicMessage") PublicMessage publicMessage);

    /**
     * 插入私聊消息
     * @param privateMessage
     * @return
     */
    Integer insertPriMsg(@Param("privateMessage") PrivateMessage privateMessage);

    /**
     * 插入系统公告消息
     * @param publicMessage
     * @return
     */
    Integer insertSystemMsg(@Param("broadcastMessage") BroadcastMessage broadcastMessage);

    /**
     * 插入群聊消息
     * @param publicMessage
     * @return
     */
    Integer insertPubMsg(@Param("publicMessage") PublicMessage publicMessage);
}
