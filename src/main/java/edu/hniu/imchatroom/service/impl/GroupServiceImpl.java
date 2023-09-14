package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.GroupMapper;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.bean.messages.Message;
import edu.hniu.imchatroom.model.bean.messages.MessageType;
import edu.hniu.imchatroom.model.bean.messages.PublicMessage;
import edu.hniu.imchatroom.model.bean.messages.StatusCode;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private GroupMapper groupMapper;
    private MessageService messageService;

    @Autowired
    public void setGroupMapper(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 处理 查询指定hostId（群主uId）所创建的群组、以及该群组下所有的用户信息 的业务逻辑
     * @param hostId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<Group> doGetMyCreatedGroups(Long hostId) {

        // 1、先查询出该用户所创建的所有群组信息
        List<Group> myGroups = groupMapper.selectMyCreatedGroups(hostId);

        // 2、然后再挨个查询出该用户创建的所有群组中所对应的所有群组成员（包括发送了入群申请的用户）
        for (Group myGroup : myGroups) {
            List<GroupUser> myGroupUsers = doGetGroupUserById(myGroup.getGId(), null);
            // 最终将每个群组中所有的成员都挨个设置进该群组中
            myGroup.setMembers(myGroupUsers);
        }

        return myGroups;
    }

    /**
     * 处理 新增群组信息 的业务逻辑
     * @param newGroup
     * @return
     */
    @Transactional
    @Override
    public Integer doAddGroup(Group newGroup) {

        // 1、为新建群聊设置初始值
        // 1.1、设置群聊唯一码
        newGroup.setGCode(StringUtil.getRandomCode(20));
        // 1.2、设置群聊创建时间
        newGroup.setCreateTime(new Date(System.currentTimeMillis()));
        // 1.3、设置群聊修改时间
        newGroup.setModifiedTime(new Date(System.currentTimeMillis()));
        // 1.4、设置群聊显示状态
        newGroup.setDisplayStatus(StatusCode.getNormalStatusCode());

        // 2、新增群聊
        int result = groupMapper.insertGroup(newGroup);

        // 3、为新增的群聊设置默认用户（即群主）
        result += doAddGroupUser(newGroup, newGroup.getHostUser(), StatusCode.getInGroupStatusCode());

        return result;
    }

    /**
     * 处理 新增群聊成员 的业务逻辑（如申请加入群聊）
     * @param group
     * @param user
     * @param guStatus
     * @return
     */
    @Override
    public Integer doAddGroupUser(Group group, User user, String guStatus) {

        GroupUser groupUser = new GroupUser();
        // 1、设置群聊
        groupUser.setGroup(group);
        // 2、设置群聊成员
        groupUser.setMember(user);
        // 3、设置成员在群聊中的状态
        groupUser.setGuStatus(guStatus);

        Date nowTime = new Date(System.currentTimeMillis());
        // 4、设置申请入群时间
        groupUser.setApplyTime(nowTime);
        if (guStatus.equals(StatusCode.getInGroupStatusCode())) {
            // 排除是新建群聊时，添加的默认用户（群主）
            // 5、设置成员加入群聊时间
            groupUser.setJoinTime(nowTime);

        } else if (guStatus.equals(StatusCode.getConfirmingStatusCode())){
            // 5、否则是用户发送入群申请，不用设置成员加入群聊时间
            groupUser.setJoinTime(null);
        }
        // 6、设置群聊显示状态
        groupUser.setDisplayStatus(StatusCode.getNormalStatusCode());

        return groupMapper.insertGroupUser(groupUser);
    }

    /**
     * 处理 查询指定uId和入群状态获取 用户的所有群组用户信息 的业务逻辑
     * @param uId
     * @param guStatus
     * @return
     */
    @Transactional(readOnly = true)
    public List<GroupUser> doGetGroupUserByIdAndGuStatus(Long uId, String guStatus) {

        return groupMapper.selectGroupUserByIdAndGuStatus(uId, guStatus);
    }

    /**
     * 处理 查询指定uId用户所加入的所有群组列表信息 的业务逻辑
     * @param uId
     * @return
     */
    @Override
    public List<Group> doGetMyEnteredGroups(Long uId) {
        // 1、根据uId和在群状态获取本人的群组用户信息
        List<GroupUser> groupUsers = doGetGroupUserByIdAndGuStatus(uId, StatusCode.getInGroupStatusCode());

        List<Group> groupsToUse = new ArrayList<>();
        Iterator<GroupUser> iterator = groupUsers.iterator();
        while (iterator.hasNext()) {
            GroupUser next = iterator.next();
            Group group = next.getGroup();
            // 2、获取此群组中所有的成员
            group.setMembers(doGetGroupUserById(group.getGId(), null));
            // 3、将这些群组添加使用
            groupsToUse.add(group);
        }

        return groupsToUse;
    }

    /**
     * 处理 模糊查询群聊信息(gCode, gName) 的业务逻辑
     * @param data
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<Group> doGetGroupsByFuzzyQuery(String data) {
        return groupMapper.selectGroupsByFuzzyQuery(data);
    }

    /**
     * 处理 通过gCode获取指定的群组信息 的业务逻辑
     * @param gCode
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public Group doGetGroupByGCode(String gCode) {
        return groupMapper.selectGroupByGCode(gCode);
    }

    /**
     * 处理 检查uId用户是否存在于gId群组中 的业务逻辑
     * @param gId
     * @param uId
     * @return
     */

    @Override
    public GroupUser doCheckUserIsInGroup(Long gId, Long uId) {
        List<GroupUser> groupUsers = doGetGroupUserById(gId, uId);
        if (null != groupUsers && !groupUsers.isEmpty())
            return groupUsers.get(0);

        return null;
    }

    /**
     * gId+uId：查询uId用户存在于gId群组的相关信息
     * @param gId：查询指定gId群组内所有的成员信息
     * @param uId：查询指定uId用户加入的所有群聊信息
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<GroupUser> doGetGroupUserById(Long gId, Long uId) {
        return groupMapper.selectGroupUserById(gId, uId);
    }

    /**
     * 根据标识：更新 groupUser 的状态
     * isEnterGroup：处理 用户进入我的群组 的业务逻辑
     * !isEnterGroup：处理 用户退出群组 的业务逻辑
     * @param groupUser
     * @return
     */
    @Override
    public Integer doUpdateUserInGroup(GroupUser groupUser, boolean isEnterGroup) {
        if (isEnterGroup) {
            // 同意用户进入群组
            groupUser.setGuStatus(StatusCode.getInGroupStatusCode());
            groupUser.setJoinTime(new Date(System.currentTimeMillis()));
            groupUser.setDisplayStatus(StatusCode.getNormalStatusCode());

        } else {
            // 用户退出群组
            groupUser.setGuStatus(StatusCode.getNotInGroupStatusCode());
            groupUser.setDisplayStatus(StatusCode.getAbnormalStatusCode());
        }

        return groupMapper.updateGroupUserStatus(groupUser);
    }
    /**
     * 更新群组信息
     * @param group
     * @return
     */
    @Override
    public Integer doUpdateGroup(Group group) {

        return groupMapper.updateGroup(group);
    }

    /**
     * 解散群组
     * @param dissolveGroup
     * @return
     */
    @Transactional
    @Override
    public Integer doDissolveGroup(Group dissolveGroup) {

        int result = 0;
        // 1、删除群组的群聊消息记录
        final String pubMsgType = MessageType.getPublicMessageType();
        PublicMessage publicMessage = new PublicMessage();
        publicMessage.setMessageType(pubMsgType);
        publicMessage.setReceiveGroup(dissolveGroup);

        // 1.1、获取群组的群聊记录信息
        List<? extends Message> messages = messageService.doGetChatMessage(publicMessage);
        // 1.2、删除群组的群聊消息记录信息
        if (null != messages && !messages.isEmpty())
            result = messageService.doDestroyMessage(pubMsgType, messages);

        // 2、将该群中所有的用户都先移出群聊

        // 2.1、获取到该群所有用户的信息
        List<GroupUser> groupUsers = doGetGroupUserById(dissolveGroup.getGId(), null);
        for (GroupUser groupUser : groupUsers) {
            // 2.2、依次将用户踢出该群聊
            result += doUpdateUserInGroup(groupUser, false);
        }
        dissolveGroup.setMembers(groupUsers);

        // 3、解散此群聊
        dissolveGroup.setModifiedTime(new Date(System.currentTimeMillis()));
        dissolveGroup.setDisplayStatus(StatusCode.getAbnormalStatusCode());
        result += doUpdateGroup(dissolveGroup);

        return result;
    }
}
