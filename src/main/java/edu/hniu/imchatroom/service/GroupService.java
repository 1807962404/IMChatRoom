package edu.hniu.imchatroom.service;

import edu.hniu.imchatroom.model.bean.Group;
import edu.hniu.imchatroom.model.bean.GroupUser;
import edu.hniu.imchatroom.model.bean.User;

import java.util.List;

public interface GroupService {

    /**
     * 处理 查询指定hostId（群主uId）所创建的群组、以及该群组下所有的用户信息 的业务逻辑
     * @param hostId
     * @return
     */
    List<Group> doGetMyCreatedGroups(Long hostId);

    /**
     * 处理 新增群组信息 的业务逻辑
     * @param newGroup
     * @return
     */
    Integer doAddGroup(Group newGroup);

    /**
     * 处理 新增群聊成员 的业务逻辑（如申请加入群聊）
     * @param group
     * @param user
     * @param guStatus
     * @return
     */
    Integer doAddGroupUser(Group group, User user, String guStatus);

    /**
     * 处理 查询指定uId用户所加入的所有群组列表信息 的业务逻辑
     * @param uId
     * @return
     */
    List<Group> doGetMyEnteredGroups(Long uId);

    /**
     * 处理 模糊查询群聊信息(gCode, gName) 的业务逻辑
     * @param data
     * @return
     */
    List<Group> doGetGroupsByFuzzyQuery(String data);

    /**
     * 处理 通过gCode获取指定的群组信息 的业务逻辑
     * @param gCode
     * @return
     */
    Group doGetGroupByGCode(String gCode);

    /**
     * 处理 检查uId用户是否存在于gId群组中 的业务逻辑
     * @param gId
     * @param uId
     * @return
     */
    GroupUser doCheckUserIsInGroup(Long gId, Long uId);

    /**
     * gId+uId：查询uId用户存在于gId群组的相关信息
     * @param gId：查询指定gId群组内所有的成员信息
     * @param uId：查询指定uId用户加入的所有群聊信息
     * @return
     */
    List<GroupUser> doGetGroupUserById(Long gId, Long uId);

    /**
     * 处理 查询指定uId和入群状态获取 用户的所有群组用户信息 的业务逻辑
     * @param uId
     * @param guStatus
     * @return
     */
    List<GroupUser> doGetGroupUserByIdAndGuStatus(Long uId, String guStatus);

    /**
     * 根据标识：更新 groupUser 的状态
     * isEnterGroup：处理 用户进入我的群组 的业务逻辑
     * !isEnterGroup：处理 用户退出群组 的业务逻辑
     * @param groupUser
     * @return
     */
    Integer doUpdateUserInGroup(GroupUser groupUser, boolean isEnterGroup);

    /**
     * 更新群组信息
     *  如：解散群聊
     * @param group
     * @return
     */
    Integer doUpdateGroup(Group group);

    /**
     * 解散群组
     * @param dissolveGroup
     * @return
     */
    Integer doDissolveGroup(Group dissolveGroup);
}
