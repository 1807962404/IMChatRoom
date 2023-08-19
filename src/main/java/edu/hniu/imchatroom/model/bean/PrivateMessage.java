package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 私聊消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage extends Message implements Serializable {

    private static final long serialVersionUID = 35657656345345345L;
    private Integer prId;
    private User sendUser;
    private User receiveUser;
}
