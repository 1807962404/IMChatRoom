package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.FriendMapper;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    private FriendMapper friendMapper;
    private MessageService messageService;

    @Autowired
    public void setFriendMapper(FriendMapper friendMapper) {
        this.friendMapper = friendMapper;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * [是否需要根据fsStatus好友状态进行查询]处理 检查二者是否为好友关系 的业务逻辑
     * @param hostUser
     * @param friendUser
     * @param fsStatus
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public FriendShip doCheckIsFriend(User hostUser, User friendUser, String fsStatus) {
        FriendShip friendShip = new FriendShip();
        friendShip.setHostUser(hostUser);
        friendShip.setFriendUser(friendUser);
        friendShip.setFsStatus(fsStatus);

        return friendMapper.selectFriendShip(friendShip);
    }

    /**
     * 添加一个好友关系：即处理好友申请的业务逻辑
     * @param hostUser
     * @param friendUser
     * @return
     */
    @Override
    public Integer doAddFriendShip(User hostUser, User friendUser) {
        FriendShip friendShip = new FriendShip();
        // 设置申请人
        friendShip.setHostUser(hostUser);
        // 设置朋友
        friendShip.setFriendUser(friendUser);
        // 设置好友关系状态为：正待确认中
        friendShip.setFsStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.CONFIRMING));
        // 设置申请时间
        friendShip.setApplyTime(new Date(System.currentTimeMillis()));
        // 设置展示状态
        friendShip.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL));

        return friendMapper.insertFriendShip(friendShip);
    }

    @Transactional
    @Override
    public Integer doMakeFriend(Integer fsId) {

        FriendShip friendShip = new FriendShip();
        friendShip.setFsId(fsId);
        // 1.1、修改友谊表中好友状态为：正常状态
        friendShip.setFsStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ISFRIEND));
        // 1.2、更新友谊表数据的状态
        int result = friendMapper.updateFriendShip(friendShip);

        // 2.1、新增好友表数据
        Friend newFriend = new Friend();
        // 设置好友友谊
        newFriend.setFriendShip(friendShip);
        // 设置好友状态
        newFriend.setFStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL));
        // 设置友情结交时间
        newFriend.setMakeTime(new Date(System.currentTimeMillis()));
        // 设置展示状态
        newFriend.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL));
        // 2.2、插入好友表数据
        result += friendMapper.insertFriend(newFriend);

        return result;
    }

    /**
     * 通过uId获取他所有的好友信息
     * @param uId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<Friend> doGetFriendsByUId(Integer uId) {
        return friendMapper.selectFriendsByUId(uId);
    }

    /**
     * 删除好友关系 friendShip
     * @param friendShip
     * @return
     */
    @Transactional
    @Override
    public Integer doDelMyFriend(FriendShip friendShip) {

        int result = 0;

        // 1、删除双方的聊天记录
        final String priMsgType = MessageTypeEnum.getMessageType(MessageTypeEnum.PRI_MSG);
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setMessageType(priMsgType);
        privateMessage.setSendUser(friendShip.getHostUser());
        privateMessage.setReceiveUser(friendShip.getFriendUser());

        // 1.1、获取双方的聊天记录信息
        List<? extends Message> messages = messageService.doGetChatMessage(privateMessage);
        // 1.2、删除双方的私聊消息记录信息
        if (!messages.isEmpty())
            result = messageService.doDestroyMessage(priMsgType, messages);

        // 2、设置友谊记录
        // 2.1、设置友谊状态为：非好友状态
        friendShip.setFsStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.NOTFRIEND));
        // 2.2、设置显示状态为隐藏
        friendShip.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL));
        // 2.3、更新友谊表记录
        result += friendMapper.updateFriendShip(friendShip);

        // 3、设置好友表状态
        Friend delFriend = new Friend();
        // 3.1、设置fsId
        delFriend.setFriendShip(friendShip);
        // 3.2、设置好友状态为：非好友状态
        delFriend.setFStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL));
        // 3.3、设置显示状态为隐藏
        delFriend.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL));
        // 3.4、更新好友表记录
        result += friendMapper.updateFriend(delFriend);

        return result;
    }

    /**
     * 根据uId获取其他用户给我发送过的好友请求
     * @param uId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<FriendShip> doGetOwnFriendship(Integer uId) {
        return friendMapper.selectOwnFriendship(uId);
    }
}
