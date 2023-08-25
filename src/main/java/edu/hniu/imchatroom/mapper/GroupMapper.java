package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.Group;
import edu.hniu.imchatroom.model.bean.GroupUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMapper {
    /**
     * 查询我创建的所有群组
     * @param hostId
     * @return
     */
    List<Group> selectMyGroups(@Param("hostId") Integer hostId);

    /**
     * 根据gId查询指定群组
     * @param gId
     * @return
     */
    Group selectGroupById(@Param("gId") Integer gId);

    /**
     * 新增群组信息
     * @param newGroup
     * @return
     */
    Integer insertGroup(@Param("newGroup") Group newGroup);

    /**
     * 新增群组成员状态信息
     * @param groupUser
     * @return
     */
    Integer insertGroupUser(@Param("groupUser") GroupUser groupUser);

    /**
     * 查询指定uId用户所加入的所有群组列表信息
     * @param uId
     * @return
     */
    List<Group> selectMyEnteredGroups(@Param("uId") Integer uId);

    /**
     * 查询data与gCode或gName相似的群聊信息
     * @param data
     * @return
     */
    List<Group> selectGroupsByFuzzyQuery(@Param("data") String data);

    /**
     * 通过gCode查询群组信息
     * @param gCode
     * @return
     */
    Group selectGroupByGCode(@Param("gCode") String gCode);

    /**
     * gId+uId：查询uId用户存在于gId群组的相关信息
     * @param gId：查询指定gId群组内所有的成员信息
     * @param uId：查询指定uId用户加入的所有群聊信息
     * @return
     */
    List<GroupUser> selectGroupsUsersById(@Param("gId") Integer gId, @Param("uId") Integer uId);

    /**
     * 更新groupUser 的状态（用户在群组中的状态）
     * @param groupUser
     * @return
     */
    Integer updateGroupUserStatus(@Param("groupUser") GroupUser groupUser);

    /**
     * 更新group的信息
     * @param group
     * @return
     */
    Integer updateGroup(@Param("group") Group group);
}
