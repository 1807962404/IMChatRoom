package edu.hniu.imchatroom.model.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于封装结果集返回给前端对象
 */
@Data
public final class ResultVO<T> implements Serializable {
    private static final long serialVersionUID = 584729382493853L;
    private Integer code;       // 后端返回结果的状态码
    private T data;             // 后端返回结果
    private String msg;         // 附带信息
}