package edu.hniu.imchatroom;

import edu.hniu.imchatroom.model.bean.Friend;
import edu.hniu.imchatroom.model.bean.Message;
import edu.hniu.imchatroom.model.bean.PrivateMessage;
import edu.hniu.imchatroom.service.FriendService;
import edu.hniu.imchatroom.service.impl.UserServiceImpl;
import edu.hniu.imchatroom.util.MailUtil;
import edu.hniu.imchatroom.util.StringUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import edu.hniu.imchatroom.model.bean.User;

import javax.mail.MessagingException;
import java.util.List;

import static edu.hniu.imchatroom.util.VariableUtil.CHATROOM_NAME;

@SpringBootTest
class ImChatRoomApplicationTests {

    @Test
    void contextLoads() {
    }

    private UserServiceImpl userService;

    @Autowired
    public void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Test
    void testSelectAllUsers() {
        List<User> userList = userService.doGetAllUsers(null);
        userList.forEach(user -> System.out.println(user));
    }

    @Test
    void testSelectUser() {
        User user = new User();
        user.setPassword("e10adc3949ba59abbe56e057f20f883e");
        user.setEmail("loveislonging@163.com");
        System.out.println(userService.doSignIn(user));
    }

    private FriendService friendService;
    @Autowired
    public void setFriendService(FriendService friendService) {
        this.friendService = friendService;
    }
    @Test
    void testGetUserFriends() {
        for (Friend friend : friendService.doGetFriendsByUId(1)) {
            System.out.println(friend);
        }
    }

    private MailUtil mailUtil;
    @Autowired
    public void setMailUtils(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    @Test
    void testRegisterUser() {

        String targetEmail = "1359599193@qq.com";
        // 发送邮件
        String content = "<a href='http://localhost:8080/chatroom/user/active-user-account?code=" +
                StringUtil.getRandomCode(true) + "'>点击激活【" + CHATROOM_NAME + "】账户！</a>";
        try {
            System.out.println(mailUtil.sendEmail(targetEmail, content, CHATROOM_NAME + "激活邮件") ? "发送成功！" : "发送失败");
        } catch (MessagingException e) {
            System.out.println("邮件发送失败！" + e);
        }
    }

    @Test
    void testMessage() {
        Message message = new PrivateMessage();
        User user = new User();
        user.setUId(1);
        ((PrivateMessage) message).setSendUser(user);
        printMessage(message);
    }

    static void printMessage(Message message) {
        System.out.println(message);
    }
}
