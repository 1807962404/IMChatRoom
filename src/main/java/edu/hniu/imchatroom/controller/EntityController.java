package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.User;
import edu.hniu.imchatroom.model.bean.messages.ArticleMessage;
import edu.hniu.imchatroom.model.bean.messages.BroadcastMessage;
import edu.hniu.imchatroom.model.bean.messages.Message;
import edu.hniu.imchatroom.model.bean.messages.MessageType;
import edu.hniu.imchatroom.service.MessageService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.StringUtil;
import edu.hniu.imchatroom.util.VariableUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static edu.hniu.imchatroom.util.VariableUtil.*;

@Slf4j
@RestController
@RequestMapping("/entity")
public class EntityController {

    /**
     * 在内存中创建一个长80，宽30的图片，默认黑色背景 的验证码框
     */
    private final int width = 80;
    private final int height = 30;
    /**
     * 字体大小、偏移量、验证码个数
     */
    private final int fontSize = 24;
    private final int offsetX = 10;
    private final int offsetY = 25;
    private final int verifyCodeLength = 4;

    // 用于存放相应时间戳对应的 验证码
    private final Map<String, String> verifyCodeMap = new ConcurrentHashMap<>();

    @RequestMapping("/verify-code/{timestamp}")
    public void doGetCheckCode(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("timestamp") String timestamp
    ) throws IOException {
        //服务器通知浏览器不要缓存
        response.setHeader("pragma","no-cache");
        response.setHeader("cache-control","no-cache");
        response.setHeader("expires","0");

        // 长、高、颜色
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //获取画笔
        Graphics g = image.getGraphics();
        //设置画笔颜色为灰色
        g.setColor(Color.GRAY);
        //填充图片
        g.fillRect(0,0, width, height);

        //产生4个随机验证码，并将其放入到Map集合中
        String checkCode = StringUtil.getRandomCode(verifyCodeLength);
        verifyCodeMap.put(timestamp, checkCode);
        log.info("请求时间戳：{}，响应验证码：{}", timestamp, checkCode);
        log.info("验证码Map：{}", verifyCodeMap);

        //将验证码集合放入HttpSession中
        request.getSession().setAttribute(VariableUtil.CHECK_CODE, verifyCodeMap);

        //设置画笔颜色为黄色
        g.setColor(Color.YELLOW);
        //设置字体的小大
        g.setFont(new Font("黑体",Font.BOLD,fontSize));
        //向图片上写入验证码
        g.drawString(checkCode, offsetX, offsetY);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }

    private UserService userService;
    private MessageService messageService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/update-session-resources")
    public void updateSessionResources(HttpServletRequest request) {
        log.info("更新HttpSession资源中...");
        HttpSession session = request.getSession();

        // 1、更新用户信息
        User user = (User) session.getAttribute(SIGNINED_USER);
        User userToUse = userService.doGetUserById(user.getUId());
        userService.doSetUserToUse(userToUse);
        session.setAttribute(SIGNINED_USER, userToUse);

        // 2、设置系统通告供所有用户查看
        Message broadcastMessage = new BroadcastMessage();
        broadcastMessage.setMessageType(MessageType.getSystemMessageType());
        List<? extends Message> messages = messageService.doGetChatMessage(broadcastMessage);
        session.setAttribute(BROADCAST_MESSAGE_NAME, messages);

        // 3、设置优文摘要供所有用户查看
        Message articleMessage = new ArticleMessage();
        articleMessage.setMessageType(MessageType.getAbstractMessageType());
        messages = messageService.doGetChatMessage(articleMessage);
        session.setAttribute(ARTICLE_MESSAGE_NAME, messages);

        log.info("已更新HttpSession资源！");
    }
}
