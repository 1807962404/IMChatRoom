package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员发布广播消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessage extends Message implements Serializable {

    private static final long serialVersionUID = 123123123324241L;
    private Integer bId;
    private User user;
}