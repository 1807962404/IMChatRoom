package edu.hniu.imchatroom.model.bean.messages;

import edu.hniu.imchatroom.model.bean.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员发布广播消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessage extends Message implements Serializable {

    private static final long serialVersionUID = 123123123324241L;
    private Long bId;
    private User publisher;
}