package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 群聊消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicMessage extends Message implements Serializable {

    private static final long serialVersionUID = 5676787645345345L;
    private Integer puId;
    private User sendUser;
    private Group receiveGroup;
}
