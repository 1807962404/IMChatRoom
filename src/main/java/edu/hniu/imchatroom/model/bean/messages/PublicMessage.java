package edu.hniu.imchatroom.model.bean.messages;

import edu.hniu.imchatroom.model.bean.Group;
import edu.hniu.imchatroom.model.bean.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 群聊消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicMessage extends Message implements Serializable {

    private static final long serialVersionUID = 5676787645345345L;
    private Long puId;
    private User sendUser;
    private Group receiveGroup;
}
