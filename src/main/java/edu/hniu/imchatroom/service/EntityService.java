package edu.hniu.imchatroom.service;

import edu.hniu.imchatroom.model.bean.BroadcastMessage;
import edu.hniu.imchatroom.model.bean.Feedback;

import java.util.List;

public interface EntityService {

    /**
     * 处理 获取所有的意见反馈内容 的业务逻辑
     * @return
     */
    List<Feedback> doGetAllFeedbacks(String keyword);

    /**
     * 处理 根据fbId获取到指定的意见反馈内容 的业务逻辑
     * @param fbId
     * @return
     */
    Feedback doGetFeedback(Integer fbId);

    /**
     * 处理 新增意见反馈内容 的业务逻辑
     * @param feedback
     * @return
     */
    Integer doAddFeedback(Feedback feedback);

    /**
     * 处理 【根据用户id（管理员）】获取【其发布的】所有系统广播信息 的业务逻辑
     * @return
     */
    List<BroadcastMessage> doGetBroadcasts(Integer uId);
}
