package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityMapper {

    /**
     * 获取所有的意见反馈内容
     * @return
     */
    List<Feedback> selectAllFeedbacks(@Param("keyword") String keyword);

    /**
     * 新增意见反馈内容
     * @param feedback
     */
    Integer insertFeedback(@Param("feedback") Feedback feedback);

    /**
     * 查询指定的 意见反馈内容
     * @param fbId
     * @return
     */
    Feedback selectFeedback(@Param("fbId") Integer fbId);
}
