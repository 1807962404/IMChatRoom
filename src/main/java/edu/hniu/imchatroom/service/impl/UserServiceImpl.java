package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.mapper.MessageMapper;
import edu.hniu.imchatroom.mapper.UserMapper;
import edu.hniu.imchatroom.model.bean.PrivateMessage;
import edu.hniu.imchatroom.model.bean.User;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.MailUtil;
import edu.hniu.imchatroom.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.CHATROOM_NAME;

@Service
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;
    private MessageMapper messageMapper;
    private MailUtil mailUtil;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Autowired
    public void setMailUtils(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    /**
     * 模糊查询用户信息
     * @param data
     * @return
     */
    @Override
    public List<User> doGetUsersByFuzzyQuery(String data) {
        return userMapper.selectUsersByFuzzyQuery(data);
    }

    /**
     * 处理【根据账号状态】查询所有用户信息 的业务逻辑
     * @return
     */
    public List<User> doGetAllUsers(String accountStatus) {
        return userMapper.selectAllUsers(accountStatus);
    }

    /**
     * 根据uId查询用户信息
     */
    @Override
    public User doGetUserById(Integer uId) {
        return userMapper.selectUserById(uId);
    }

    /**
     * 处理 根据账号(account/email)+密码形式 检查user是否存在 的业务逻辑
     * @param user
     * @param hasPwd
     * @return
     */
    public User doCheckUserExists(User user, boolean hasPwd) {
        // 查询用户信息是否存在
        User userByIdentified = userMapper.selectUserToVerify(user, hasPwd);
        return userByIdentified;
    }

    /**
     * 处理 注册用户信息 的业务逻辑
     * @return
     */
    @Override
    public Integer doSignUp(User user) {

        // 1、为该用户创建一个唯一的 账号Account
        user.setAccount(StringUtil.getRandomCode(8));

        // 2、为该用户创建一个唯一的 账号激活码
        String activeCode = StringUtil.getRandomCode(true);
        user.setActiveCode(activeCode);

        // 3、修改时间设置为当前时间
        user.setModifiedTime(new Date(System.currentTimeMillis()));

        // 4、发送邮件
        String content = "<h1 style='color: #f00'>欢迎注册“" + CHATROOM_NAME + "”账户，点击以下链接激活账户，以便获取更多惊喜体验！</h1>" +
                "<a href='http://localhost:8080/chatroom/user/active-user-account/" +  activeCode
                + "'>点击激活【" + CHATROOM_NAME +"】账户！</a>";
        try {
            mailUtil.sendEmail(user.getEmail(), content, CHATROOM_NAME + "激活邮件");
        } catch (MessagingException e) {
            System.out.println("发送邮件失败：" + e.getMessage());
            return 0;
        }

        // 5、新增用户
        int result = userMapper.insertUser(user);

        return result;
    }

    /**
     * 处理 用户登陆 的业务逻辑
     * @param user
     * @return
     */
    @Override
    public User doSignIn(User user) {

        // 1、查询用户信息是否存在
        User userByIdentified = doCheckUserExists(user, true);
//        System.out.println("====>>" + userByIdentified);
        if (null == userByIdentified)
            return null;

        // 2、修改该用户为在线状态，并修改其 上次登陆时间 以及 最近修改时间
        Date nowTime = new Date(System.currentTimeMillis());
        userByIdentified.setLastSigninTime(nowTime);
        userByIdentified.setModifiedTime(nowTime);
        userByIdentified.setOnlineStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ONLINE));

        // 3、用户登陆（需要修改其在线状态）
        int result = userMapper.updateUser(userByIdentified);
        return result == 1 ? userByIdentified : null;
    }

    /**
     * 激活账户操作
     * @param activeCode
     * @return
     */
    @Override
    public Integer doActiveUserAccount(String activeCode) {
        User activeUser = new User();

        // 1、设置激活码
        activeUser.setActiveCode(activeCode);
        // 2、设置账号激活状态为：已激活
        activeUser.setAccountStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED));
        // 3、设置账号激活时间
        activeUser.setActiveTime(new Date(System.currentTimeMillis()));
        // 4、设置账号修改时间
        activeUser.setModifiedTime(new Date(System.currentTimeMillis()));

        return userMapper.activeUserAccount(activeUser);
    }

    /**
     * 更新账号信息
     *  1、处理 用户会话注销 的业务逻辑
     *  2、处理 注销用户账号 的业务逻辑
     * @param user
     * @return
     */
    public Integer doUpdateUser(User user) {

        // 2、设置账号修改时间
        user.setModifiedTime(new Date(System.currentTimeMillis()));
        // 3、不设置上次登陆时间
        user.setLastSigninTime(null);

        return userMapper.updateUser(user);
    }
}
