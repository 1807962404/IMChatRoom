package edu.hniu.imchatroom.model.enums;

/**
 * 服务端响应状态
 */
public enum ResponseCodeEnum {

    SUCCESS(0, "成功"), WARNING(1, "警告"), FAILED(-1, "失败");
    private final Integer code;
    private final String desc;
    ResponseCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Integer getCode(ResponseCodeEnum responseCodeEnum) {
        ResponseCodeEnum[] values = ResponseCodeEnum.values();
        for (ResponseCodeEnum value : values) {
            if (value.equals(responseCodeEnum))
                return value.code;
        }
        return ResponseCodeEnum.getCode(ResponseCodeEnum.FAILED);  // 默认失败
    }

    public static String getDesc(ResponseCodeEnum responseCodeEnum) {
        ResponseCodeEnum[] values = ResponseCodeEnum.values();
        for (ResponseCodeEnum value : values) {
            if (value.equals(responseCodeEnum))
                return value.desc;
        }
        return ResponseCodeEnum.FAILED.desc;  // 默认失败
    }

    @Override
    public String toString() {
        return "ResponseCodeEnum{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }
}
