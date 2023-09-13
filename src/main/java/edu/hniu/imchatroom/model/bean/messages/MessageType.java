package edu.hniu.imchatroom.model.bean.messages;

import edu.hniu.imchatroom.model.enums.MessageTypeEnum;

import java.io.Serializable;

/**
 * 获取消息类型中间件类
 */
public final class MessageType implements Serializable {
    private static final long serialVersionUID = 573834759834932923L;

    private static String getMessageType(MessageTypeEnum messageTypeEnum) {
        return MessageTypeEnum.getMessageType(messageTypeEnum);
    }

    public static String[] getAllMessageTypes() {
        MessageTypeEnum[] values = MessageTypeEnum.values();

        String[] allMessageTypes = new String[values.length];
        for (int i = 0; i < values.length; i++)
            allMessageTypes[i] = getMessageType(values[i]);

        return allMessageTypes;
    }

    public static String getSignInMessageType() { return getMessageType(MessageTypeEnum.SIGN_IN_MSG); }

    public static String getOnlineCountMessageType() {
        return getMessageType(MessageTypeEnum.ONLINE_COUNT_MSG);
    }

    public static String getPrivateMessageType() {
        return getMessageType(MessageTypeEnum.PRI_MSG);
    }

    public static String getPublicMessageType() {
        return getMessageType(MessageTypeEnum.PUB_MSG);
    }

    public static String getSystemMessageType() {
        return getMessageType(MessageTypeEnum.SYSTEM_MSG);
    }

    public static String getAbstractMessageType() {
        return getMessageType(MessageTypeEnum.ABSTRACT_MSG);
    }

    public static String getFeedbackMessageType() {
        return getMessageType(MessageTypeEnum.FEEDBACK_MSG);
    }

    public static String getGroupMessageType() {
        return getMessageType(MessageTypeEnum.GROUP_MSG);
    }

    public static String getEnterGroupMessageType() {
        return getMessageType(MessageTypeEnum.ENTER_GROUP_MSG);
    }

    public static String getAgreeEnterGroupMessageType() { return getMessageType(MessageTypeEnum.AGREE_ENTER_GROUP_MSG); }

    public static String getDropMemberFromGroupMessageType() {
        return getMessageType(MessageTypeEnum.DROP_MEMBER_FROM_GROUP_MSG);
    }

    public static String getExitGroupMessageType() {
        return getMessageType(MessageTypeEnum.EXIT_GROUP_MSG);
    }

    public static String getDissolveGroupMessageType() {
        return getMessageType(MessageTypeEnum.DISSOLVE_GROUP_MSG);
    }

    public static String getFriendMessageType() {
        return getMessageType(MessageTypeEnum.FRIEND_MSG);
    }

    public static String getAddFriendMessageType() {
        return getMessageType(MessageTypeEnum.ADD_FRIEND_MSG);
    }

    public static String getAgreeAddFriendMessageType() {
        return getMessageType(MessageTypeEnum.AGREE_ADD_FRIEND_MSG);
    }
}
