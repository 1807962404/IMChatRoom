package edu.hniu.imchatroom.service;

import jakarta.mail.MessagingException;

public interface EmailService {

    /**
     * 发送邮件
     * @param to        接收者
     * @param subject   邮件主题
     * @param content   邮件正文
     */
    void sendEmail(String to, String subject, String content) throws MessagingException;

    /**
     * 发送邮件
     * @param to            接收者
     * @param subject       邮件主题
     * @param content       邮件正文
     * @param attachment    携带附件
     * @throws MessagingException
     */
    void sendEmail(String to, String subject, String content, String attachment) throws MessagingException;
}
