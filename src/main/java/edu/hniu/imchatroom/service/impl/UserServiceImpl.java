package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.controller.UserController;
import edu.hniu.imchatroom.mapper.UserMapper;
import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.MessageTypeEnum;
import edu.hniu.imchatroom.model.enums.RoleEnum;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.GroupService;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.MailUtil;
import edu.hniu.imchatroom.util.EncryptUtil;
import edu.hniu.imchatroom.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.CHATROOM_NAME;
import static edu.hniu.imchatroom.util.VariableUtil.DEFAULT_PASSWORD;

@Service
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;
    private MessageService messageService;
    private GroupService groupService;
    private MailUtil mailUtil;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    @Autowired
    public void setMailUtil(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    /**
     * 检查表单发送过来的验证码是否 与session中存储的verifycode相等
     *
     * @param verifyCode
     * @param checkCode
     * @return
     */
    @Override
    public boolean doCheckVerifyCode(String checkCode, String verifyCode) {

        return StringUtil.isNotEmpty(verifyCode) && checkCode.equalsIgnoreCase(verifyCode);
    }

    /**
     * 设置可用 用户
     * 一个唯一的uniqueUserCode 对应一个用户（不区分在线或否）
     *
     * @param user
     * @param uniqueUserCode
     */
    @Override
    public void doSetUserToUse(User user, String uniqueUserCode) {
        if (null != user) {
            // 将用户的密码和激活码设为空
            user.setPassword(null);
            user.setActiveCode(null);

            if (null != uniqueUserCode)
                // 唯一用户码对应在线用户
                UserController.onlineUserToUseMap.put(uniqueUserCode, user);
        }
    }

    /**
     * 模糊查询用户信息
     *
     * @param data
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> doGetUsersByFuzzyQuery(String data) {
        return userMapper.selectUsersByFuzzyQuery(data);
    }

    /**
     * 处理【根据账号状态】查询所有用户信息 的业务逻辑
     *
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> doGetAllUsers(String accountStatus) {
        return userMapper.selectAllUsers(accountStatus);
    }

    /**
     * 根据uId查询用户信息
     */
    @Transactional(readOnly = true)
    @Override
    public User doGetUserById(Integer uId) {
        return userMapper.selectUserById(uId);
    }

    /**
     * 处理 根据账号(account/email)+密码形式 检查user是否存在 的业务逻辑
     *
     * @param user
     * @param hasPwd
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public User doCheckUserExists(User user, boolean hasPwd) {

        if (hasPwd) {   // 若需要验证密码，则先进行加密
            String password = user.getPassword();
            if (StringUtil.isNotEmpty(password)) {
                // 对该用户输入的密码进行 MD5加密 操作
                try {
                    user.setPassword(EncryptUtil.encodeByMd5(password));
                } catch (Exception e) {
                    System.out.println("MD5加密过程中发生错误: " + e.getMessage());
                }
            }
        }

        // 查询用户信息是否存在
        User userByIdentified = userMapper.selectUserToVerify(user, hasPwd);
        if (!hasPwd) {
            // 不需要密码
            doSetUserToUse(userByIdentified, null);
        }
        return userByIdentified;
    }

    /**
     * 处理 注册用户信息 的业务逻辑
     *
     * @return
     */
    @Override
    public Integer doSignUp(User user) {

        // 1、为该用户创建一个唯一的 账号Account
        user.setAccount(StringUtil.getRandomCode(8));

        // 2、对该用户输入的密码进行 MD5加密 操作
        try {
            user.setPassword(EncryptUtil.encodeByMd5(user.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 3、为该用户创建一个唯一的 账号激活码
        String activeCode = StringUtil.getRandomCode(true);
        user.setActiveCode(activeCode);

        // 4、修改时间设置为当前时间
        user.setModifiedTime(new Date(System.currentTimeMillis()));

        // 5、发送邮件
        String content = "<h1 style='color: #f00'>欢迎注册“" + CHATROOM_NAME + "”账户，点击下面链接激活账户，以便获取更多惊喜体验！</h1>" +
                "<a href='http://localhost:8080/chatroom/user/active-user-account/" + activeCode
                + "'>点击此链接激活【" + CHATROOM_NAME + "】账户！</a>";
        try {
            mailUtil.sendEmail(user.getEmail(), content, CHATROOM_NAME + "激活邮件");
        } catch (MessagingException e) {
            System.out.println("发送邮件失败：" + e.getMessage());
            return 0;
        }

        // 6、新增用户
        int result = userMapper.insertUser(user);

        return result;
    }

    /**
     * 处理 用户登陆 的业务逻辑
     *
     * @param user
     * @return
     */
    @Override
    public Integer doSignIn(User user) {

        // 1、修改该用户为在线状态，并修改其 上次登陆时间 以及 最近修改时间
        Date nowTime = new Date(System.currentTimeMillis());
        user.setOnlineStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ONLINE));
        user.setLastSigninTime(nowTime);
        user.setModifiedTime(nowTime);

        // 2、用户登陆（需要修改其在线状态）
        return userMapper.updateUser(user);
    }

    /**
     * 激活账户操作
     *
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
     * 1、处理 用户会话注销 的业务逻辑
     * 2、处理 注销用户账号 的业务逻辑
     *
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

    /**
     * 按步骤处理 用户忘记密码 的业务逻辑
     *
     * @param user
     * @param step
     * @return
     */
    public Integer doForgetPassword(User user, int step) {

        boolean success = false;
        if (1 == step) {
            // 第一步：发送邮件
            String content = "<h1 style='color: #f00'>" + CHATROOM_NAME + "邮箱通知，点击下面链接重置账户密码！</h1>" +
                    "<a href='http://localhost:8080/chatroom/user/reset-password/" + user.getActiveCode()
                    + "'>点击此链接重置【" + CHATROOM_NAME + "】账户密码为默认密码（" + DEFAULT_PASSWORD + "）！</a>";
            try {
                success = mailUtil.sendEmail(user.getEmail(), content, CHATROOM_NAME + "重置密码邮件");
            } catch (MessagingException e) {
                System.out.println("发送邮件失败：" + e.getMessage());
            }

        } else if (2 == step) {
            // 第二步：重置该用户的密码为 默认密码
            try {
                user.setPassword(EncryptUtil.encodeByMd5(DEFAULT_PASSWORD));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            success = doUpdateUser(user) == 1;
        }

        return success ? 0 : 1;
    }

    /**
     * 处理 根据activeCode用户账号激活码查询指定用户 的业务逻辑
     *
     * @param activeCode
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public User doGetUserByActiveCode(String activeCode) {
        return userMapper.selectUserByActiveCode(activeCode);
    }

    /**
     * 注销用户账号
     * @param loggoutUser
     * @return
     */
    @Transactional
    @Override
    public Integer doLogoutAccount(User loggoutUser) {

        int result = 0;
        // 1、判断需要注销的用户账号是否为管理员账号
        if (loggoutUser.getRole().equals(RoleEnum.getRoleName(RoleEnum.ADMIN))) {
            // 2、删除此管理员账户发表的所有 优文摘要 信息
            final String artMsgType = MessageTypeEnum.getMessageType(MessageTypeEnum.ABSTRACT_MSG);
            ArticleMessage articleMessage = new ArticleMessage();
            articleMessage.setMessageType(artMsgType);
            articleMessage.setPublisher(loggoutUser);

            // 2.1、获取此管理员账户发表的所有 优文摘要 信息
            List<? extends Message> messages = messageService.doGetChatMessage(articleMessage);
            // 2.2、删除此管理员账户发表的所有 优文摘要 记录信息
            if (null != messages)
                if (!messages.isEmpty())
                    result += messageService.doDestroyMessage(artMsgType, messages);

            // 3、删除此管理员账户发布的所有 系统广播 信息
            final String broMsgType = MessageTypeEnum.getMessageType(MessageTypeEnum.SYSTEM_MSG);
            BroadcastMessage broadcastMessage = new BroadcastMessage();
            broadcastMessage.setMessageType(broMsgType);
            broadcastMessage.setPublisher(loggoutUser);

            // 3.1、获取此管理员账户发布的所有 系统广播 信息
            messages = messageService.doGetChatMessage(broadcastMessage);
            // 3.2、删除此管理员账户发布的所有 系统广播 记录信息
            if (null != messages)
                if (!messages.isEmpty())
                    result += messageService.doDestroyMessage(broMsgType, messages);
        }

        // 4、检查下需要注销的用户是否 创建或加入了群组
        List<Group> myCreatedGroups = groupService.doGetMyGroups(loggoutUser.getUId());
        if (!myCreatedGroups.isEmpty()) {
            // 如果此用户创建了群组，则需将这些群组解散
            for (Group myCreatedGroup : myCreatedGroups) {
                result += groupService.doDissolveGroup(myCreatedGroup);
            }
        }

        List<Group> groups = groupService.doGetMyEnteredGroups(loggoutUser.getUId());
        if (!groups.isEmpty()) {
            for (Group group : groups) {
                // 如果是此用户创建了群组，则需将这些群组解散
                if (group.getHostUser().equals(loggoutUser))
                    result += groupService.doDissolveGroup(group);

                else {
                    // 如果仅仅是此用户加入的群组，则直接退出群组即可
                    List<GroupUser> groupUsers = groupService.doGetGroupsUsersById(group.getGId(), loggoutUser.getUId());
                    if (null == groupUsers || groupUsers.isEmpty())
                        continue;

                    result += groupService.doUpdateUserInGroup(groupUsers.get(0), false);
                }
            }
        }

        // 5、设置该用户的账号为：已注销状态、隐藏状态
        loggoutUser.setAccountStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.INVALID));
        loggoutUser.setOnlineStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.OFFLINE));
        loggoutUser.setDisplayStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL));
        // 注销账户
        result += doUpdateUser(loggoutUser);

        return result;
    }
}
