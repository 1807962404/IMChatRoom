package edu.hniu.imchatroom.model.bean.messages;

import edu.hniu.imchatroom.model.bean.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 私聊消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage extends Message implements Serializable {

    private static final long serialVersionUID = 35657656345345345L;
    private Long prId;
    private User sendUser;
    private User receiveUser;
}
