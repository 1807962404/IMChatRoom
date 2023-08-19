package edu.hniu.imchatroom.service;

import edu.hniu.imchatroom.model.bean.Friend;
import edu.hniu.imchatroom.model.bean.FriendShip;
import edu.hniu.imchatroom.model.bean.User;

import java.util.List;

public interface FriendService {

    /**
     * [是否需要根据fsStatus好友状态进行查询]处理 检查二者是否为好友关系 的业务逻辑
     * @param hostUser
     * @param friendUser
     * @param fsStatus
     * @return
     */
    FriendShip doCheckIsFriend(User hostUser, User friendUser, String fsStatus);

    /**
     * 添加一个好友关系：即处理好友申请的业务逻辑
     * @return
     */
    Integer doAddFriendShip(User hostUser, User friendUser);

    /**
     * 处理 结交好友 的业务逻辑
     * @param fsId
     * @return
     */
    Integer doMakeFriend(Integer fsId);

    /**
     * 通过uId获取他所有的好友信息
     * @param uId
     * @return
     */
    List<Friend> doGetFriendsByUId(Integer uId);

    /**
     * 删除好友关系 friendShip
     * @param friendShip
     * @return
     */
    Integer doDelMyFriend(FriendShip friendShip);
}
