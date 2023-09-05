package edu.hniu.imchatroom.service.impl;

import edu.hniu.imchatroom.service.EmailService;
import edu.hniu.imchatroom.util.StringUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    // 发件人
    @Value("${spring.mail.username}")
    private String username;
    private JavaMailSender mailSender;
    @Autowired
    public void setJavaMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送邮件之前做的操作
     * @param message
     * @param to
     * @param subject
     * @param content
     * @throws MessagingException
     */
    private void beforeSendMail(MimeMessage message, String to,
                                String subject, String content, String attachment
    ) throws MessagingException {

        // 1、设置邮件发送者
        MimeMessageHelper helper;
        if (StringUtil.isNotEmpty(attachment)) {
            // true：表示开启传文件的功能
            helper = new MimeMessageHelper(message, true);
            // 添加附件
            FileSystemResource file = new FileSystemResource(attachment);
            helper.addAttachment(file.getFilename(), file);

        } else
            helper = new MimeMessageHelper(message);
        helper.setFrom(username);

        // 2、设置邮件接收者
        helper.setTo(to);

        // 3、设置邮件主题
        helper.setSubject(subject);

        // 4、设置邮件内容，并设置内容格式为html
        helper.setText(content, true);
    }
    /**
     * 发送邮件
     * @param to        接收者
     * @param subject   邮件主题
     * @param content   邮件正文
     */
    @Override
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        // 1、创建邮件消息
        MimeMessage message = mailSender.createMimeMessage();

        beforeSendMail(message, to, subject, content, null);

        // 3、发送邮件
        mailSender.send(message);
    }

    /**
     * 发送邮件
     * @param to            接收者
     * @param subject       邮件主题
     * @param content       邮件正文
     * @param attachment    携带附件
     * @throws MessagingException
     */
    @Override
    public void sendEmail(String to, String subject, String content, String attachment) throws MessagingException {
        // 1、创建邮件消息
        MimeMessage message = mailSender.createMimeMessage();

        beforeSendMail(message, to, subject, content, attachment);

        // 3、发送邮件
        mailSender.send(message);
    }
}
