package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.RoleEnum;
import edu.hniu.imchatroom.service.EntityService;
import edu.hniu.imchatroom.util.StringUtil;
import edu.hniu.imchatroom.util.VariableUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@Controller
@RequestMapping("/entity")
public class EntityController {

    /**
     * 在内存中创建一个长80，宽30的图片，默认黑色背景 的验证码框
     */
    private final int width = 80;
    private final int height = 30;
    private final int verifyCodeLength = 4;
    // 用于存放相应时间戳对应的 验证码
    private final Map<String, String> verifyCodeMap = new ConcurrentHashMap<>();
    private EntityService entityService;
    @Autowired
    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

    @RequestMapping("/verify-code/{timestamp}")
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

    /**
     * 查询所有意见反馈内容
     * @return
     */
    @ResponseBody
    @GetMapping("/all-feedbacks")
    public List<Feedback> getAllFeedbacks() {

        List<Feedback> feedbacks = entityService.doGetAllFeedbacks(null);
        log.info("All Feedbacks: " + feedbacks);
        return feedbacks;
    }

    /**
     * 反馈意见
     * @param feedback
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/send-feedback")
    public ResultVO<Feedback> sendFeedback(Feedback feedback, HttpServletRequest request) {

        ResultVO<Feedback> resultVO = new ResultVO<>();
        // 1、检查传入的feedback（即需要反馈的意见内容）是否为空
        if (null == feedback || StringUtil.isEmpty(feedback.getFbContent())) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("反馈内容不能为空！");
            log.warn("反馈内容不能为空！");
            return resultVO;
        }

        // 新增意见反馈信息
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        feedback.setPublisher(thisUser);
        int result = entityService.doAddFeedback(feedback);

        if (result != 1) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("意见反馈失败！");
            log.warn("意见反馈失败！");
            return resultVO;
        }

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        // 设置data为新增的意见反馈内容
        resultVO.setData(entityService.doGetFeedback(feedback.getFbId()));
        resultVO.setMsg("您已操作成功，感谢您的反馈！");
        log.warn("用户 {} 反馈意见成功！", thisUser.getNickname());
        return resultVO;
    }

    /**
     * 获取用户管理员发布过的所有系统广播信息
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/admin-published-broadcasts")
    public ResultVO<List<BroadcastMessage>> getPublishedBroadcasts(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        ResultVO<List<BroadcastMessage>> resultVO = new ResultVO<>();

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(RoleEnum.getRoleName(RoleEnum.ADMIN))) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("用户权限不够，无法访问！");
            log.warn("用户权限不够，无法访问！");
            return resultVO;
        }

        // 2、查询此管理员用户发布过的系统广播
        List<BroadcastMessage> myPublishedBroadcasts = entityService.doGetBroadcasts(thisUser.getUId());
        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(myPublishedBroadcasts);
        log.info("已查询出管理员用户：{} 发布过的所有广播信息！", thisUser.getNickname());

        return resultVO;
    }

    /**
     * 获取用户管理员发布过的所有优文摘要信息
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/admin-published-articles")
    public ResultVO<List<ArticleMessage>> getPublishedArticles(HttpServletRequest request) {
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        ResultVO<List<ArticleMessage>> resultVO = new ResultVO<>();

        // 1、检查此用户是否为管理员
        if (!thisUser.getRole().equals(RoleEnum.getRoleName(RoleEnum.ADMIN))) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("用户权限不够，无法访问！");
            log.warn("用户权限不够，无法访问！");
            return resultVO;
        }

        // 2、查询此管理员用户发布过的优文摘要
        List<ArticleMessage> myPublishedArticles = entityService.doGetArticles(thisUser.getUId());
        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(myPublishedArticles);
        log.info("已查询出管理员用户：{} 发布过的所有优文摘要信息！", thisUser.getNickname());

        return resultVO;
    }
}
