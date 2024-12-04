package com.jiangzhiyan.clash.config.mail.sender.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author JiangZhiyan
 */

@Getter
@RequiredArgsConstructor
public enum ConfigType {
    CLASH(1, "clash"),
    SHADOW_ROCKET(2, "shadowrocket");

    private final int code;
    private final String name;

    public static ConfigType toConfigType(String name) {
        for (ConfigType configType : ConfigType.values()) {
            if (configType.name.equalsIgnoreCase(name)) {
                return configType;
            }
        }
        return null;
    }
}
