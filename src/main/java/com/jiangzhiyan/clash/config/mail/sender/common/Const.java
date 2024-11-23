package com.jiangzhiyan.clash.config.mail.sender.common;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * @author JiangZhiyan
 */
@UtilityClass
public class Const {
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");

    @UtilityClass
    public final static class StrPool {
        public static final String EMPTY = "";
        public static final String COLON = ":";
        public static final String COMMA = ",";
        public static final String LF = "\n";
    }
}
