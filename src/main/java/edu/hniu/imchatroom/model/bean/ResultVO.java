package edu.hniu.imchatroom.model.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于封装结果集返回给前端对象
 */
@Data
public class ResultVO<V> implements Serializable {
    private Integer code;       // 后端返回结果的状态码
    private V data;             // 后端返回结果
    private String msg;         // 附带信息

    //无参构造方法
    public ResultVO() {
    }
    public ResultVO(int code) {
        this.code = code;
    }
    /**
     * 有参构造方法
     * @param code
     * @param msg
     */
    public ResultVO(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    /**
     * 有参构造方法
     * @param code
     * @param data
     * @param msg
     */
    public ResultVO(int code, V data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }
}