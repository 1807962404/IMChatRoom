package edu.hniu.imchatroom.model.enums;

/**
 * 消息类型枚举类
 */
public enum MessageTypeEnum {

    // 用户登陆、在线人数 消息
    SIGN_IN_MSG("sign-in-message"), ONLINE_COUNT_MSG("online-count-message"),

    // 私聊、群聊、系统、优文摘要、意见反馈 消息
    PRI_MSG("private-message"), PUB_MSG("public-message"),
    SYSTEM_MSG("system-message"), ABSTRACT_MSG("abstract-message"),
    FEEDBACK_MSG("feedback-message"),

    // 群聊消息：申请加入群聊、同意用户入群申请、将用户移出群聊、退出群聊、解散群聊 消息
    GROUP_MSG("group-message"),
    ENTER_GROUP_MSG("enter-group-message"), AGREE_ENTER_GROUP_MSG("agree-enter-group-message"),
    DROP_MEMBER_FROM_GROUP_MSG("drop-member-from-group-message"),
    EXIT_GROUP_MSG("exit-group-message"), DISSOLVE_GROUP_MSG("dissolve-group-message"),

    // 朋友消息：申请添加好友、同意好友申请 消息
    FRIEND_MSG("friend-message"),
    ADD_FRIEND_MSG("add-friend-message"), AGREE_ADD_FRIEND_MSG("agree-add-friend-message");

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
