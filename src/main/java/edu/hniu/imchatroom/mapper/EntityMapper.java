package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.FeedbackMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityMapper {

    /**
     * 获取所有的意见反馈内容
     * @return
     */
    List<FeedbackMessage> selectAllFeedbackMessages(@Param("keyword") String keyword);

    /**
     * 新增意见反馈内容
     * @param feedbackMessage
     */
    Integer insertFeedbackMessage(@Param("feedbackMessage") FeedbackMessage feedbackMessage);

    /**
     * 查询指定的 意见反馈内容
     * @param fbId
     * @return
     */
    FeedbackMessage selectFeedbackMessage(@Param("fbId") Integer fbId);
}
