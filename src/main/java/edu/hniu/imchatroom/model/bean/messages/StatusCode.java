package edu.hniu.imchatroom.model.bean.messages;


import edu.hniu.imchatroom.model.enums.StatusCodeEnum;

import java.io.Serializable;

public class StatusCode implements Serializable {
    private static final long serialVersionUID = 382574354323423894L;

    private static String getStatusCode(StatusCodeEnum statusCodeEnum) {
        return StatusCodeEnum.getStatusCode(statusCodeEnum);
    }

    public static String getOnlineStatusCode() {
        return getStatusCode(StatusCodeEnum.ONLINE);
    }

    public static String getOfflineStatusCode() {
        return getStatusCode(StatusCodeEnum.OFFLINE);
    }

    public static String getActivatedStatusCode() {
        return getStatusCode(StatusCodeEnum.ACTIVATED);
    }

    public static String getInActiveStatusCode() {
        return getStatusCode(StatusCodeEnum.INACTIVE);
    }

    public static String getInvalidStatusCode() {
        return getStatusCode(StatusCodeEnum.INVALID);
    }

    public static String getIsFriendStatusCode() {
        return getStatusCode(StatusCodeEnum.ISFRIEND);
    }

    public static String getNotFriendStatusCode() {
        return getStatusCode(StatusCodeEnum.NOTFRIEND);
    }

    public static String getConfirmingStatusCode() {
        return getStatusCode(StatusCodeEnum.CONFIRMING);
    }

    public static String getNormalStatusCode() {
        return getStatusCode(StatusCodeEnum.NORMAL);
    }

    public static String getAbnormalStatusCode() {
        return getStatusCode(StatusCodeEnum.ABNORMAL);
    }

    public static String getInGroupStatusCode() {
        return getStatusCode(StatusCodeEnum.INGROUP);
    }

    public static String getNotInGroupStatusCode() {
        return getStatusCode(StatusCodeEnum.NOTINGROUP);
    }
}
