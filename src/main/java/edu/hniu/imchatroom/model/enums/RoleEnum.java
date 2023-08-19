package edu.hniu.imchatroom.model.enums;

public enum RoleEnum {
    ADMIN("Admin"), USER("User");

    private final String role;
    RoleEnum(String role) {
        this.role = role;
    }

    public static String getRoleName(RoleEnum roleEnum) {
        RoleEnum[] values = RoleEnum.values();
        for (RoleEnum value : values) {
            if (value.equals(roleEnum))
                return value.role;
        }

        return RoleEnum.getRoleName(RoleEnum.USER);
    }

    @Override
    public String toString() {
        return this.role;
    }
}
