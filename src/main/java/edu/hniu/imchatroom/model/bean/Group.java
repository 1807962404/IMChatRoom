package edu.hniu.imchatroom.model.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 群组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Serializable {

    private static final long serialVersionUID = 345465734234645L;
    @JsonProperty("gId")
    private Integer gId;
//    群聊码
    @JsonProperty("gCode")
    private String gCode;
    // 群主
    @JsonProperty("gName")
    private String gName;
    private Date createTime;
    private Date modifiedTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
    private User hostUser;
    // 群组所有成员
    private List<GroupUser> members;
}
