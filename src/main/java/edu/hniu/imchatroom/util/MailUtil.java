package edu.hniu.imchatroom.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送工具类
 */
@Service
public final class MailUtil {

    @Value("${lux.customized.email.smtp.host}")
    private String host;
    @Value("${lux.customized.email.smtp.auth}")
    private String auth;
    @Value("${lux.customized.email.user}")
    private String user;
    @Value("${lux.customized.email.password}")
    private String password;

    /**
     * 发送邮件
     * @param to        接收者
     * @param content   邮件正文
     * @param subject   邮件标题
     */
    public boolean sendEmail(String to, String content, String subject) throws MessagingException {
        final Properties properties = new Properties();
        // 设置发件人邮箱的主机账号密码
        properties.put("mail.smtp.auth", auth);
        properties.put("mail.smtp.host", host);
        properties.put("mail.user", user);
        properties.put("mail.password", password);

        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(properties, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);

        // 设置发件人
        InternetAddress fromAddress = new InternetAddress(user);
        message.setFrom(fromAddress);

        // 设置收件人
        InternetAddress toAddress = new InternetAddress(to);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        // 设置邮件标题
        message.setSubject(subject);

        // 设置邮件正文
        message.setContent(content, "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
        return true;
    }
}
