package edu.hniu.imchatroom.model.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 234783249212320L;
    @JsonProperty("uId")
    private Long uId;
    private String account;
    private String password;
    private String nickname;
    private String email;
    private String role;
    // 头像路径
    private String avatarUrl;
    // 用户在线状态。0：在线，1：离线
    private String onlineStatus;
    // 账号状态。0：已激活，1：未激活，2：已注销
    private String accountStatus;
    private String activeCode;
    private Date activeTime;
    private Date lastSigninTime;
    private Date modifiedTime;
    // 展示状态。0：显示，1：隐藏
    private String displayStatus;
    // 用户登陆唯一码
    private String uniqueUserCode;
    // 我的好友列表
    private List<Friend> myFriends;
    private boolean hasSignIn = false;

    /**
     * 若uid、account 以及 email 都相等则看作是同一个对象
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uId, user.uId) && Objects.equals(account, user.account) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uId, account, email);
    }
}
