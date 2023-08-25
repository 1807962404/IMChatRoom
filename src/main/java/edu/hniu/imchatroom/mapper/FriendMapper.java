package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.Friend;
import edu.hniu.imchatroom.model.bean.FriendShip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友、友谊处理 映射器
 */
@Mapper
public interface FriendMapper {

    /**
     * 查询 友谊信息
     * @param friendShip
     * @return
     */
    FriendShip selectFriendShip(@Param("friendShip") FriendShip friendShip);

    /**
     * 根据fsId查询 友谊信息
     * @param fsId
     * @return
     */
    FriendShip selectFriendShipByFsId(@Param("fsId") Integer fsId);

    /**
     * 根据uId查询他所有的 好友信息
     */
    List<Friend> selectFriendsByUId(@Param("uId") Integer uId);

    /**
     * 新增 友谊信息
     * @param friendShip
     * @return
     */
    Integer insertFriendShip(@Param("friendShip") FriendShip friendShip);

    /**
     * 更新友谊状态
     * @param friendShip
     * @return
     */
    int updateFriendShip(@Param("friendShip") FriendShip friendShip);

    /**
     * 新增 好友信息
     * @param newFriend
     * @return
     */
    int insertFriend(@Param("newFriend") Friend newFriend);

    /**
     * 更新好友表状态
     * @param friend
     * @return
     */
    int updateFriend(@Param("friend") Friend friend);

    List<FriendShip> selectOwnFriendship(@Param("uId") Integer uId);
}
