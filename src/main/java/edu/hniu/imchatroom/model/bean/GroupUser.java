package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 中间表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUser implements Serializable {
    private static final long serialVersionUID = 843723723498238L;
    private Integer guId;
    // 0：仍在群聊，1：尚待群主确认，2：已退出群聊
    private String guStatus;
    // 入群申请时间
    private Date applyTime;
    // 入群时间
    private Date joinTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
    // 群组
    private Group group;
    // 群聊成员
    private User member;
}
