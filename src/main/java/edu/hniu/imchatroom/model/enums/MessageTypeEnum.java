package edu.hniu.imchatroom.model.enums;

/**
 * 消息类型枚举类
 */
public enum MessageTypeEnum {
    // 私聊、群聊、系统、优文摘要、在线人数 消息
    PRI_MSG("private-message"), PUB_MSG("public-message"),
    SYSTEM_MSG("system-message"), ABSTRACT_MSG("abstract-message"),
    ONLINE_COUNT_MSG("online-count-message");

    private final String msgType;
    MessageTypeEnum(String msgType) {
        this.msgType = msgType;
    }

    public static String getMessageType(MessageTypeEnum messageTypeEnum) {
        MessageTypeEnum[] values = MessageTypeEnum.values();
        for (MessageTypeEnum value : values) {
            if (value.equals(messageTypeEnum))
                return value.msgType;
        }
        return null;
    }
}
