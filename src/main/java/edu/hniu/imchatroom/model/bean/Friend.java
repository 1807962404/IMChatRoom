package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend implements Serializable {

    private static final long serialVersionUID = 46552423436453244L;
    private Integer fId;
    private FriendShip friendShip;
    // 友谊状态，0：正常，1：非好友关系
    private String fStatus;
    private Date makeTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
}
