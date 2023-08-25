package edu.hniu.imchatroom.model.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 友谊
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendShip {

    private static final long serialVersionUID = 5362452342353454L;
    @JsonProperty("fsId")
    private Integer fsId;
    /*private Integer hostId;
    private Integer friendId;*/
    private User hostUser;
    private User friendUser;
    private String remark;
    private String desc;
    // 好友关系状态。0：正处于好友状态，1：好友关系确认中，2：非好友状态
    private String fsStatus;
    // 申请添加好友时间
    private Date applyTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
}
