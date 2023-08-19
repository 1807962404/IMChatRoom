package edu.hniu.imchatroom.model.server.ws.entity;

import edu.hniu.imchatroom.model.bean.User;
import lombok.Data;

/**
 * 用来封装服务器端 给浏览器端 发送的消息数据
 * 如 系统公告：由管理员发出
 */
@Data
public class ResultMessage {

    private User fromAdmin;
    private String message;
}
