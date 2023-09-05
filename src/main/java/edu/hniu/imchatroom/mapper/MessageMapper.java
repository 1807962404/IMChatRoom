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
     * 查询的群聊消息是在该用户加入此群聊之后的消息
     * @param publicMessage
     * @return
     */
    List<PublicMessage> selectPublicMessage(@Param("publicMessage") PublicMessage publicMessage);

    /**
     * 获取所有系统广播信息
     * @param broadcastMessage
     * @return
     */
    List<BroadcastMessage> selectBroadcastMessage(@Param("broadcastMessage") BroadcastMessage broadcastMessage);

    /**
     * 获取所有优文摘要信息
     * @param articleMessage
     * @return
     */
    List<ArticleMessage> selectArticleMessage(@Param("articleMessage") ArticleMessage articleMessage);

    /**
     * 获取所有的意见反馈信息
     * @return
     */
    List<FeedbackMessage> selectFeedbackMessage(@Param("feedbackMessage") FeedbackMessage feedbackMessage);

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
     * 新增意见反馈内容
     * @param feedbackMessage
     */
    Integer insertFeedbackMsg(@Param("feedbackMessage") FeedbackMessage feedbackMessage);

    /**
     * 根据prId批量删除私聊信息
     * @param privateMessages
     * @return
     */
    Integer deletePriMsg(@Param("privateMessages") List<PrivateMessage> privateMessages);

    /**
     * 根据puId批量删除群聊信息
     * @param publicMessages
     * @return
     */
    Integer deletePubMsg(@Param("publicMessages") List<PublicMessage> publicMessages);

    /**
     * 根据bId删除系统广播信息
     * @param broadcastMessages
     * @return
     */
    int deleteBroMsg(@Param("broadcastMessages") List<BroadcastMessage> broadcastMessages);

    /**
     * 根据aId删除优文摘要信息
     * @param articleMessages
     * @return
     */
    int deleteArtMsg(@Param("articleMessages") List<ArticleMessage> articleMessages);
}
