package edu.hniu.imchatroom.model.enums;

public enum StatusCodeEnum {

    // 用户在线状态：在线、离线
    ONLINE('0'), OFFLINE('1'),
    // 用户账号状态：已激活、未激活、已注销
    ACTIVATED('0'), INACTIVE('1'), INVALID('2'),
    // 好友关系状态：已是好友关系、好友关系确认中、非好友
    ISFRIEND('0'), CONFRIMFRIEND('1'), NOTFRIEND('2'),
    // 展示状态。0：显示，1：隐藏
    NORMAL('0'), ABNORMAL('1');


    private final char status;

    StatusCodeEnum(char status) {
        this.status = status;
    }

    /**
     * 获取相应状态码
     * @param statusCodeEnum
     * @return
     */
    public static String getStatusCode(StatusCodeEnum statusCodeEnum) {
        StatusCodeEnum[] values = StatusCodeEnum.values();
        for (StatusCodeEnum tempValue : values) {
            if (tempValue.equals(statusCodeEnum))
                return String.valueOf(statusCodeEnum.status);
        }
        return null;
    }

    @Override
    public String toString() {
        String str = String.valueOf(status).toString();
        return str;
    }

}
