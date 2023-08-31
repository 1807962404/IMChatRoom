package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 意见反馈
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1231242342341231L;
    private Integer fbId;
    private User publisher;
    private String fbContent;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date publishTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
}
