package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.*;
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
     * @param broadcastMessage
     * @return
     */
    Integer insertSystemMsg(@Param("broadcastMessage") BroadcastMessage broadcastMessage);

    /**
     * 插入群聊消息
     * @param publicMessage
     * @return
     */
    Integer insertPubMsg(@Param("publicMessage") PublicMessage publicMessage);

    /**
     * 插入优文摘要消息
     * @param articleMessage
     * @return
     */
    Integer insertArticleMsg(@Param("articleMessage") ArticleMessage articleMessage);

    /**
     * 根据prId批量删除私聊信息
     * @param privateMessages
     * @return
     */
    Integer deletePriMsg(@Param("privateMessages") List<PrivateMessage> privateMessages);

}
