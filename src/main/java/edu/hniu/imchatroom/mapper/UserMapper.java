package edu.hniu.imchatroom.mapper;

import edu.hniu.imchatroom.model.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户处理 映射器
 */
@Mapper
public interface UserMapper {

    /**
     * 【根据账号状态】查询所有用户信息
     * @return
     */
    List<User> selectAllUsers(@Param("accountStatus") String accountStatus);

    /**
     * 根据uId查询用户信息
     */
    User selectUserById(@Param("uId") Integer uId);

    /**
     * 根据指定条件查询用户信息
     * 是否有密码：即是否只检查账号，或账号密码都有验证
     * @param user
     * @param hasPwd
     * @return
     */
    User selectUserToVerify(@Param("user") User user, @Param("hasPwd") boolean hasPwd);

    /**
     * 修改该用户为在线状态，并修改其 上次登陆时间 以及 最近修改时间
     * @param user
     * @return
     */
    Integer updateUser(@Param("user") User user);

    /**
     * 激活账户
     * @param activeUser
     * @return
     */
    Integer activeUserAccount(@Param("activeUser") User activeUser);

    /**
     * 插入用户
     * @param user
     * @return
     */
    Integer insertUser(@Param("user") User user);

    /**
     * 根据data模糊查询出用户中 account、email或nickname中包含的内容
     * @param data
     * @return
     */
    List<User> selectUsersByFuzzyQuery(@Param("data") String data);

    /**
     * 根据activeCode用户账号激活码查询指定用户
     * @param activeCode
     * @return
     */
    User selectUserByActiveCode(String activeCode);
}
