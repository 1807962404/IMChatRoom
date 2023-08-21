package edu.hniu.imchatroom.service;


import edu.hniu.imchatroom.model.bean.PrivateMessage;
import edu.hniu.imchatroom.model.bean.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserService {

    /**
     * 模糊查询用户信息
     * @param data
     * @return
     */
    List<User> doGetUsersByFuzzyQuery(String data);

    /**
     * 【根据账号状态】查询所有用户信息
     * @return
     */
    List<User> doGetAllUsers(String accountStatus);

    /**
     * 根据uId查询用户信息
     */
    User doGetUserById(Integer uId);

    /**
     * 注册用户信息
     * @return
     */
    Integer doSignUp(User user);

    /**
     * 用户登陆操作
     * @param user
     * @return
     */
    User doSignIn(User user);

    /**
     * 激活账户操作
     * @param activeCode
     * @return
     */
    Integer doActiveUserAccount(String activeCode);

    /**
     * 是否根据账号(account/email)+密码的形式 检查user是否存在
     * @param user
     * @param hasPwd
     * @return
     */
    User doCheckUserExists(User user, boolean hasPwd);

    /**
     * 更新账号信息
     *  1、处理 用户会话注销 的业务逻辑
     *  2、处理 注销用户账号 的业务逻辑
     * @param user
     * @return
     */
    Integer doUpdateUser(User user);
}