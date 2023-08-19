package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 群组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Serializable {

    private static final long serialVersionUID = 345465734234645L;
    private Integer gId;
//    群聊码
    private String gCode;
    // 群主id
    private Integer glId;
    private String gName;
    // 群聊状态，0：健在，1：群聊已解散
    private String status;
    private Date createTime;
    private Date modifiedTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
    // 群聊成员
    private Set<User> userSet;
}
