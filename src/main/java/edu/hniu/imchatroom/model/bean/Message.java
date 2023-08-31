package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1289437882371283L;
    // 记录消息类型（私聊消息、群聊消息、系统公告通知消息）
    private String messageType;
    private String content;
    // 该注解自动会解析处理,会把字符串类型 按照pattern格式：yyyy-MM-dd HH:mm:ss 转换成时间类型
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sendTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
}
