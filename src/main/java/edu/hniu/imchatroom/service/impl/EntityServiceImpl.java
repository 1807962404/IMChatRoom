package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.EntityMapper;
import edu.hniu.imchatroom.model.bean.BroadcastMessage;
import edu.hniu.imchatroom.model.bean.Feedback;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityServiceImpl implements EntityService {

    private EntityMapper entityMapper;
    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    /**
     * 处理 获取所有的意见反馈内容 的业务逻辑
     * @return
     */
    @Override
    public List<Feedback> doGetAllFeedbacks(String keyword) {
        return entityMapper.selectAllFeedbacks(keyword);
    }

    /**
     * 处理 新增意见反馈内容 的业务逻辑
     * @param feedback
     * @return
     */
    @Override
    public Integer doAddFeedback(Feedback feedback) {
        feedback.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL));
        return entityMapper.insertFeedback(feedback);
    }

    /**
     * 处理 根据fbId获取到指定的意见反馈内容 的业务逻辑
     * @param fbId
     * @return
     */
    @Override
    public Feedback doGetFeedback(Integer fbId) {
        if (null == fbId || 0 >= fbId)
            fbId = null;
        return entityMapper.selectFeedback(fbId);
    }

    /**
     * 处理 【根据用户id（管理员）】获取【其发布的】所有系统广播信息 的业务逻辑
     * @return
     */
    @Override
    public List<BroadcastMessage> doGetBroadcasts(Integer uId) {
        if (null == uId || 0 >= uId)
            uId = null;

        return entityMapper.selectBroadcasts(uId);
    }
}
