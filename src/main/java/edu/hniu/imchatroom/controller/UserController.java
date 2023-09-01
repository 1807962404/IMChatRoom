package edu.hniu.imchatroom.controller;

import edu.hniu.imchatroom.model.bean.*;
import edu.hniu.imchatroom.model.enums.StatusCodeEnum;
import edu.hniu.imchatroom.service.EntityService;
import edu.hniu.imchatroom.service.UserService;
import edu.hniu.imchatroom.util.EncryptUtil;
import edu.hniu.imchatroom.util.StringUtil;
import edu.hniu.imchatroom.util.VariableUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static edu.hniu.imchatroom.util.VariableUtil.*;
import static edu.hniu.imchatroom.util.VariableUtil.SIGNINED_USER;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private EntityService entityService;

    // 记录在线用户
    public static final Map<String, User> onlineUserToUseMap = new ConcurrentHashMap<>();
    // 静态变量，用于记录当前在线连接数
    private static int onlineCount = 0;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }


    /**
     * 用户注册
     * @param user
     * @param verifyCode
     * @param identify：时间戳，唯一标识
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/signup")
    public ResultVO doSignUp(User user,
                             String verifyCode,
                             String identify,
                             HttpServletRequest request) {

        // 封装结果集
        ResultVO resultVO = new ResultVO();

        if (StringUtil.isEmpty(identify)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("身份识别错误！");
            log.warn("身份识别错误！（传入的identify：" + identify + " 错误）");
            return resultVO;
        }

        // 1、获取服务端中对应时间戳的验证码的验证码，并将其与提交的表单中输入的验证码内容 进行忽略大小写比对
        Map<String, String> verifyCodeMap = (Map<String, String>)request.getSession().getAttribute(VariableUtil.CHECK_CODE);
        log.info("时间戳：{}，时间戳对应验证码：{}，验证码：{}", identify, verifyCodeMap.get(identify), verifyCode);
        boolean flag = userService.doCheckVerifyCode(verifyCodeMap.get(identify), verifyCode);
        if (!flag) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("验证码有误！");
            log.warn("验证码有误！");
            return resultVO;
        }

        log.info("用户注册中输入的用户信息：{}", user);

        // 2、检查用户信息是否存在
        User userByIdentified = userService.doCheckUserExists(user, false);
        log.info("账号信息为：{}", userByIdentified);

        boolean notExists = false;
        if (null == userByIdentified) {
            notExists = true;

        } else {
            if (!(userByIdentified.getEmail().equals(user.getEmail()))) {
                // 不存在使用该邮箱地址的用户
                notExists = true;

            } else {
                // 如果账号是已激活状态
                if (userByIdentified.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("账号已存在，请直接登陆即可！");
                    log.warn("账号已存在，请直接登陆即可！");

                } else if (userByIdentified.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.INACTIVE))) {
                    // 如果账号是未激活状态
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("该账号处于未激活状态，请先激活账号再享受更多体验！");
                    log.warn("该账号处于未激活状态，请先激活账号再享受更多体验！");
                }
                return resultVO;
            }
        }

        // 如果拥有该邮箱的用户已存在，则无法进行注册
        if (!notExists) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("该邮箱地址已被注册，请直接登陆即可！");
            log.warn("该邮箱地址已被注册，请直接登陆即可！");
            return resultVO;
        }

        // 4、注册一个新用户
        int result = userService.doSignUp(user);
        if (1 == result) {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("账号激活已发送至您的邮箱，请检查邮箱并点击链接激活账号！");
            log.info("账号激活已发送至您的邮箱，请检查邮箱并点击链接激活账号！");

        } else {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("账号注册失败，请检查邮箱地址是否有误！");
            log.warn("账号注册失败，请检查邮箱地址是否有误！");
        }

        return resultVO;
    }

    /**
     * 激活账号
     * @param activeCode
     * @return
     */
    @GetMapping("/active-user-account/{activeCode}")
    public void doActiveUserAccount(
            @PathVariable("activeCode") String activeCode,
            HttpServletResponse response
    ) throws IOException {

        int result = userService.doActiveUserAccount(activeCode);
        String content = "";
        if (1 != result) {
            content = "账号激活失败！";
            log.warn("账号激活失败！");
        }
        else {
            content = "账号激活成功！";
            log.info("账号激活成功！");
        }

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<h1 style='color: #f00'>" + content +
                        "</h1><a href='http://localhost:8080/chatroom/login'>点击跳转至登陆页面</a>");
    }

    /**
     * 用户登陆
     * @param user
     * @param verifyCode
     * @param identify：时间戳，唯一标识
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping(value = {"/signin", "/login"})
    public ResultVO doSignIn(User user,
                             String verifyCode,
                             String identify,
                             HttpServletRequest request) {

        ResultVO resultVO = new ResultVO();

        if (StringUtil.isEmpty(identify)) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("身份识别错误！");
            log.warn("身份识别错误！（传入的identify：" + identify + " 错误）");
            return resultVO;
        }

        // 1、获取服务端中对应时间戳的验证码的验证码，并将其与提交的表单中输入的验证码内容 进行忽略大小写比对
        Map<String, String> verifyCodeMap = (Map<String, String>)request.getSession().getAttribute(VariableUtil.CHECK_CODE);
        boolean flag = userService.doCheckVerifyCode(verifyCodeMap.get(identify), verifyCode);
        if (!flag) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("验证码有误！");
            log.warn("验证码有误！");
            return resultVO;
        }

        log.info("用户输入的登陆信息：{}", user);

        // 2、对该用户输入的密码进行 MD5加密 操作
        try {
            user.setPassword(EncryptUtil.encodeByMd5(user.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 3、检查用户是否存在
        User userByIdentified = userService.doCheckUserExists(user, true);
        if (null == userByIdentified) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("账号信息有误，请检查后重新登陆！");
            log.warn("账号信息有误，请检查后重新登陆！");
            return resultVO;

        }else if (userByIdentified.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.INACTIVE))) {
            // 如果账号是未激活状态
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("该账号处于未激活状态，请先激活账号再享受更多体验！");
            log.warn("该账号处于未激活状态，请先激活账号再享受更多体验！");
            return resultVO;
        }

        // 4、用户登陆
        userByIdentified = userService.doSignIn(user);

        // 5、用户登陆之后初始化资源操作
        doLoginInitResources(request, userByIdentified);

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        // 每个用户登陆成功后都将推送：在线用户人数 消息给所有用户
//        resultVO.setData(request.getSession().getAttribute(SIGNINED_USER_WS_CODE));
        resultVO.setMsg("登陆成功，欢迎回来：" + userByIdentified.getNickname() + "！");
        log.info("登陆成功！");
        return resultVO;
    }

    private void doLoginInitResources(HttpServletRequest request, User userByIdentified) {

        log.info("用户信息：{}", userByIdentified);

        // 1、设置资源-->
        HttpSession session = request.getSession();

        // 1.1、设置系统通告供所有用户查看
        List<BroadcastMessage> broadcastMessages = entityService.doGetBroadcasts(null);
        session.setAttribute(BROADCAST_MESSAGE_NAME, broadcastMessages);

        // 1.2、设置优文摘要供所有用户查看
        List<ArticleMessage> articleMessages = entityService.doGetArticles(null);
        session.setAttribute(ARTICLE_MESSAGE_NAME, articleMessages);

        // 1.3、设置用户唯一标识码：建立WebSocket连接时需要使用
        String uniqueUserCode = StringUtil.getRandomCode(false);
        log.info("doSignin() uniqueUserCode: {}", uniqueUserCode);
        session.setAttribute(SIGNINED_USER_WS_CODE, uniqueUserCode);       // 用户的唯一标识码
        userService.doSetUserToUse(userByIdentified, uniqueUserCode);     // 给本次登陆的用户设置唯一标识码

        // 1.4、设置管理员和普通用户名称，用于前端校验
        if (null == session.getAttribute(ADMIN_USER_NAME)) {
            session.setAttribute("ADMIN_USER_NAME", ADMIN_USER_NAME);
        }
        if (null == session.getAttribute(COMMON_USER_NAME)) {
            session.setAttribute("COMMON_USER_NAME", COMMON_USER_NAME);
        }

        // 2、将登陆的用户信息存入至session中，以及存入在线人数中
        session.setAttribute(SIGNINED_USER, userByIdentified);

        // 3、记录在线人数 +1
        addOnlineCount();       // 在线人数 +1
        log.info("当前在线总人数为：{}", getOnlineCount());
    }

    /**
     * 建立WebSocket连接时需要使用，获取uniqueUserCode对应的用户信息
     * @param uniqueUserCode
     * @return
     */
    public static User doGetUserToWebSocket(String uniqueUserCode) {
        User user = onlineUserToUseMap.get(uniqueUserCode);
        log.info("doGetUserToWebSocket() uniqueUserCode: {}, user: {}, {}",
                uniqueUserCode, user.getUId(), user.getNickname());
        return user;
    }
    /*@GetMapping("/user/get-user-ws/{uniqueUserCode}")
    public User doGetUserToWebSocket(@PathVariable("uniqueUserCode") String uniqueCode) {
        User user = userToUseMap.get(uniqueCode);
        return user;
    }*/

    /**
     * 获取所有 “账号已激活” 的用户信息
     * @return
     */
    @ResponseBody
    @GetMapping("/all-users")
    public ResultVO<List<User>> doGetAllUsers() {

        ResultVO<List<User>> resultVO = new ResultVO<>();
        // 获取所有已激活账号的用户信息
        List<User> allUsers = userService.doGetAllUsers(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED));
        for (User user : allUsers)
            userService.doSetUserToUse(user, null);

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setData(allUsers);

        return resultVO;
    }

    /**
     * 获取所有的在线用户信息
     * @return
     */
    /*@GetMapping("/online-user-count")
    public Set<User> doGetOnlineUsers() { return onlineUsers; }*/

    /**
     * 修改用户个人信息
     * @param user
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/edit-profile")
    public ResultVO doEditProfile(User user, HttpServletRequest request) {

        ResultVO resultVO = new ResultVO();
        User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);

        // 1、检查输入的昵称是否与原先昵称一致，并且检查输入的密码是否为空
        if ((user.getNickname().equals(thisUser.getNickname()) || StringUtil.isEmpty(user.getNickname())) &&
                StringUtil.isEmpty(user.getPassword())) {
            resultVO.setCode(RESPONSE_WARNING_CODE);
            resultVO.setMsg("昵称不能与先前设置的昵称保持一致！");
            log.warn("昵称不能与先前设置的昵称保持一致！");
            return resultVO;
        }

        String newNickname = user.getNickname();
        if (StringUtil.isNotEmpty(newNickname))
            thisUser.setNickname(newNickname);
        try {
            String newPassword = user.getPassword();
            if (StringUtil.isNotEmpty(newPassword))
                thisUser.setPassword(EncryptUtil.encodeByMd5(newPassword));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2、修改个人信息
        int result = userService.doUpdateUser(thisUser);
        if (1 != result) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("修改个人信息失败！");
            log.warn("修改个人信息失败！");

        } else {
            resultVO.setCode(RESPONSE_SUCCESS_CODE);
            resultVO.setMsg("成功修改个人信息！");
            log.info("成功修改个人信息！");

            // 更新此用户信息
            thisUser = userService.doGetUserById(thisUser.getUId());
            request.getSession().setAttribute(SIGNINED_USER, thisUser);
        }

        return resultVO;
    }

    @ResponseBody
    @PostMapping("/upload-avatar")
    public ResultVO doChangeAvatar(
            @RequestPart("avatar") MultipartFile avatarFile,
            HttpServletRequest request
    ) {

        ResultVO resultVO = new ResultVO();
        if (avatarFile.isEmpty()) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("上传头像失败！");
            log.warn("上传头像失败！");

        } else {

            String staticPath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();
            log.info("文件上传保存至静态资源目录路径：{}", staticPath);

            // 获取用户上传过来的头像文件名
            String avatarFileName = avatarFile.getOriginalFilename();
            // 1、设置头像文件保存路径
            String avatarFilePath = "/avatar/" + StringUtil.getRandomCode(false) + "-" + avatarFileName;

            String realAvatarFilePath = staticPath + "/images" + avatarFilePath;
            log.info("头像文件保存路径：{}", realAvatarFilePath);

            // 2、保存头像文件至指定路径下
            File saveFile = new File(realAvatarFilePath);
            if (!saveFile.exists()) {
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // 检查文件的宽高是否符合标准（暂未能实现）
            /*try {
                BufferedImage uploadAvatar = ImageIO.read(saveFile);
                log.info("上传头像的宽：{}， 高：{}", uploadAvatar.getWidth(), uploadAvatar.getHeight());
                if (uploadAvatar.getWidth() != 42 || uploadAvatar.getHeight() != 42) {
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("请上传42*42的头像图片！");
                    log.warn("请上传42*42的头像图片！");
                    return resultVO;
                }

            } catch (IOException e) {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("上传头像失败！");
                log.error("无法保存头像文件：{}", e.getMessage());
                return resultVO;
            }*/
            try {
                avatarFile.transferTo(saveFile);    // 实现文件下载（本质上是字节流输入，即文件复制）
            } catch (IOException e) {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("上传头像失败！");
                log.error("无法保存头像文件：{}", e.getMessage());
                return resultVO;
            }

            // 3、设置本人的头像路径
            User thisUser = (User) request.getSession().getAttribute(SIGNINED_USER);
            thisUser.setAvatarUrl(avatarFilePath);
            int result = userService.doUpdateUser(thisUser);

            if (1 == result) {
                resultVO.setCode(RESPONSE_SUCCESS_CODE);
                resultVO.setMsg("已成功上传头像！");
                log.info("已成功上传头像！");
                // 更新该登陆用户的资源
                request.getSession().setAttribute(SIGNINED_USER, thisUser);

            } else {
                resultVO.setCode(RESPONSE_FAILED_CODE);
                resultVO.setMsg("上传头像失败！");
                log.warn("上传头像失败！");
            }
            return resultVO;
        }

        return resultVO;
    }

    /**
     * 重置密码操作
     * @param data
     * @return
     */
    @ResponseBody
    @PostMapping("/reset-password")
    public ResultVO doResetPassword(String data) {

        // 封装结果集
        ResultVO resultVO = new ResultVO();

        // 检查用户信息是否存在
        User resetUser = new User();
        resetUser.setEmail(data);
        User userByIdentified = userService.doCheckUserExists(resetUser, false);
        log.info("账号信息为：{}", userByIdentified);

        boolean notExists = false;
        if (null == userByIdentified) {
            notExists = true;

        } else {
            if (!userByIdentified.getEmail().equals(data)) {
                // 不存在使用该邮箱地址的用户
                notExists = true;

            } else {
                // 如果账号是已激活状态
                if (userByIdentified.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ACTIVATED))) {
                    // 处理忘记密码的逻辑（使用用户激活码作为凭证）
                    int result = userService.doForgetPassword(userByIdentified, 1);
                    if (0 == result) {
                        // 成功发送 重置账户密码 邮件
                        resultVO.setCode(RESPONSE_SUCCESS_CODE);
                        resultVO.setMsg("已成功发送重置密码邮件，请检查您的邮箱！");
                        log.info("已成功发送重置密码邮件，请检查您的邮箱！");

                    } else {
                        resultVO.setCode(RESPONSE_FAILED_CODE);
                        resultVO.setMsg("未能成功发送重置密码邮件，请稍后再试！");
                        log.warn("未能成功发送重置密码邮件，请稍后再试！");
                    }

                } else if (userByIdentified.getAccountStatus().equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.INACTIVE))) {
                    // 如果账号是未激活状态
                    resultVO.setCode(RESPONSE_WARNING_CODE);
                    resultVO.setMsg("该账号处于未激活状态，请先激活账号再享受更多体验！");
                    log.warn("该账号处于未激活状态，请先激活账号再享受更多体验！");
                }
                return resultVO;
            }
        }

        if (notExists) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("不存在使用该邮箱地址的用户！");
            log.warn("不存在使用该邮箱地址的用户！");
        }

        return resultVO;
    }

    /**
     * 重置密码最终操作
     * @param code
     * @return
     */
    @GetMapping("/reset-password/{code}")
    public void doFinalResetPassword(
            @PathVariable("code") String code,
            HttpServletResponse response
    ) throws IOException {

        // 1、检查code是否为空
        if (StringUtil.isEmpty(code)) {
            log.warn("用户重置的激活码为空！");
            response.sendRedirect("/login");
            return;
        }

        // 2、【根据激活码code】查询此用户是否存在
        User resetUser = userService.doGetUserByActiveCode(code);
        if (null == resetUser) {
            log.warn("用户不存在！");
            response.sendRedirect("/login");
            return;
        }

        // 3、重置该用户的密码
        int result = userService.doForgetPassword(resetUser, 2);
        String content = "";
        if (0 == result) {
            content = "账号重置密码成功！为确保安全，请尽快在个人信息栏中修改密码。";
            log.info("账号重置密码成功！为确保安全，请尽快在个人信息栏中修改密码。");

        } else {
            content = "账号重置密码失败！";
            log.warn("账号重置密码失败！");
        }

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<h1 style='color: #f00'>" + content +
                        "</h1><a href='http://localhost:8080/chatroom/login'>点击跳转至登陆页面</a>");
    }

    /**
     * 用户会话注销，退出登陆
     * 后期使用ResultInfo封装结果集，并会有前端响应
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/sign-out")
    public ResultVO doSignOut(HttpServletRequest request) {

        User logoutUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        // 1、修改该用户为离线状态
        logoutUser.setOnlineStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.OFFLINE));
        int result = userService.doUpdateUser(logoutUser);

        ResultVO resultVO = new ResultVO();
        log.info("用户 {} 注销会话情况：{}", logoutUser.getNickname(), result == 1 ? "已成功退出登陆！" : "注销会话失败！");
        if (result != 1) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("注销会话失败！");
            return resultVO;
        }

        onlineUserToUseMap.remove(logoutUser);     // 在线用户数量-1
        subOnlineCount();
        request.getSession().invalidate();

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setMsg("已成功退出登陆！");
        log.info("当前在线总人数为：{}", getOnlineCount());
        return resultVO;
    }

    /**
     * 用户账号注销
     * @return
     */
    @ResponseBody
    @GetMapping("/logout-account")
    public ResultVO doLogoutAccount(HttpServletRequest request) {
        User logoutAccountUser = (User) request.getSession().getAttribute(SIGNINED_USER);
        // 设置该用户的账号为：已注销状态
        logoutAccountUser.setAccountStatus(StatusCodeEnum.getStatusCode(StatusCodeEnum.INVALID));
        int result = userService.doUpdateUser(logoutAccountUser);

        ResultVO resultVO = new ResultVO();
        log.info("用户 {} 注销账号情况：{}", logoutAccountUser.getNickname(), result == 1 ? "已成功注销账号！" : "注销账号失败！");
        if (result != 1) {
            resultVO.setCode(RESPONSE_FAILED_CODE);
            resultVO.setMsg("注销账号失败！");
            return resultVO;

        }

        onlineUserToUseMap.remove(logoutAccountUser);     // 在线用户数量-1
        subOnlineCount();
        request.getSession().invalidate();

        resultVO.setCode(RESPONSE_SUCCESS_CODE);
        resultVO.setMsg("已成功注销账号！");
        log.info("当前在线总人数为：{}", getOnlineCount());
        return resultVO;
    }

    /**
     * 获取在线人数
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 在线人数 +1
     */
    public static synchronized void addOnlineCount() {
        onlineCount++;
    }

    /**
     * 在线人数 -1
     */
    public static synchronized void subOnlineCount() {
        onlineCount--;
    }
}
