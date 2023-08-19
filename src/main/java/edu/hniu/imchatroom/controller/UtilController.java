package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.util.StringUtil;
import edu.hniu.imchatroom.util.VariableUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class UtilController {

    /**
     * 在内存中创建一个长80，宽30的图片，默认黑色背景 的验证码框
     */
    private final int width = 80;
    private final int height = 30;
    private final int verifyCodeLength = 4;
    // 用于存放相应时间戳对应的 验证码
    private final Map<String, String> verifyCodeMap = new ConcurrentHashMap<>();

    @RequestMapping("/verifyCode/{timestamp}")
    public void checkCode(
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
        g.setFont(new Font("黑体",Font.BOLD,24));
        //向图片上写入验证码
        g.drawString(checkCode,15,25);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }
}
