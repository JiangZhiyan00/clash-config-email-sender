package com.jiangzhiyan.clash.config.mail.sender.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author JiangZhiyan
 */

@Getter
@RequiredArgsConstructor
public enum ConfigType {
    CLASH(1, "clash", "CLASH_CONFIG_FILE_PATH", ".yml"),
    SHADOW_ROCKET(2, "shadowrocket", "SHADOW_ROCKET_CONFIG_FILE_PATH", ".json");

    private final int code;
    private final String name;
    private final String env;
    private final String suffix;

    public static ConfigType toConfigType(String name) {
        for (ConfigType configType : ConfigType.values()) {
            if (configType.name.equalsIgnoreCase(name)) {
                return configType;
            }
        }
        return null;
    }
}
