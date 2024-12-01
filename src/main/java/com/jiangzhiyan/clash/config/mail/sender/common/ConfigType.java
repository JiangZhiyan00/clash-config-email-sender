package com.jiangzhiyan.clash.config.mail.sender.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author JiangZhiyan
 */

@Getter
@RequiredArgsConstructor
public enum ConfigType {
    CLASH(1, "clash", "CLASH_CONFIG_FILE_PATH", ".yml", "https://proxy.jzy88.top/https://jiangzhiyan00.github.io/clash_config/docs/Clash%E9%85%8D%E7%BD%AE%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.html"),
    SHADOW_ROCKET(2, "shadowrocket", "SHADOW_ROCKET_CONFIG_FILE_PATH", ".json", "https://proxy.jzy88.top/https://jiangzhiyan00.github.io/clash_config/docs/Shadowrocket%E9%85%8D%E7%BD%AE%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.html");

    private final int code;
    private final String name;
    private final String env;
    private final String suffix;
    private final String docUrl;

    public static ConfigType toConfigType(String name) {
        for (ConfigType configType : ConfigType.values()) {
            if (configType.name.equalsIgnoreCase(name)) {
                return configType;
            }
        }
        return null;
    }
}
