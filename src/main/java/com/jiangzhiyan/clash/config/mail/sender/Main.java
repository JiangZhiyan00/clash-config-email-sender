package com.jiangzhiyan.clash.config.mail.sender;

import com.jiangzhiyan.clash.config.mail.sender.common.ConfigType;
import com.jiangzhiyan.clash.config.mail.sender.common.Const;
import com.jiangzhiyan.clash.config.mail.sender.common.utils.MailUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author JiangZhiyan
 */
public class Main {
    public static void main(String[] args) {
        ConfigType configType = getConfigType();
        System.out.println(configType.getName() + "配置更新邮件通知任务开始...");
        try {
            // 1.获取订阅了改动分支的邮箱地址集合
            Set<String> validEmails = getBranchValidEmails();
            // 2.如果配置了邮箱地址,则发送邮件报告
            if (!validEmails.isEmpty()) {
                MailUtil.sendEmail(configType, getConfigContent(configType), validEmails, getCommitAuthor(), getCommitMessage());
            }
        } catch (Exception e) {
            System.err.println(configType.getName() + "配置更新邮件通知任务发生异常:" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.out.println(configType.getName() + "配置更新邮件通知任务结束...");
            System.exit(0);
        }
    }

    /**
     * 获取配置类型,目前有clash和shadowrocket
     *
     * @return 获取配置类型
     */
    private static ConfigType getConfigType() {
        String configTypeStr = getStripStr(System.getenv("CONFIG_TYPE"));
        ConfigType configType = ConfigType.toConfigType(configTypeStr);
        if (configType == null) {
            throw new RuntimeException("配置类型不存在: " + configTypeStr);
        }

        return configType;
    }

    /**
     * 获取本次提交者信息
     *
     * @return 本次提交者信息
     */
    private static String getCommitAuthor() {
        return getStripStr(System.getenv("COMMIT_AUTHOR"));
    }

    /**
     * 获取本次提交描述信息
     *
     * @return 本次提交描述信息
     */
    private static String getCommitMessage() {
        return getStripStr(System.getenv("COMMIT_MESSAGE"));
    }

    /**
     * 获取clash配置文件内容
     *
     * @return clash配置文件内容
     */
    private static String getConfigContent(ConfigType configType) throws IOException {
        return getFileContent(getStripStr(System.getenv(configType.getEnv())));
    }

    /**
     * 获取订阅了分支的有效的邮箱地址集合
     *
     * @return 订阅了分支的有效的邮箱地址集合
     */
    private static Set<String> getBranchValidEmails() throws IOException {
        // 当前改动的分支名
        String branchName = System.getenv("BRANCH_NAME").toLowerCase();
        // 从环境变量中获取email和分支信息,格式xxx@yyy.com:branch1,branch2
        String emails = getFileContent(getStripStr(System.getenv("EMAILS_FILE_PATH")));
        if (emails.isBlank()) {
            return Collections.emptySet();
        }
        Set<String> lines = Arrays.stream(emails.split(Const.StrPool.LF))
                .filter(line -> line != null && !line.strip().isBlank())
                .map(String::strip)
                .collect(Collectors.toSet());

        Set<String> emailSet = new HashSet<>(lines.size());
        lines.forEach(line -> {
            String[] split = line.split(Const.StrPool.COLON);
            if (split.length == 2) {
                String email = split[0].strip();
                if (isValidEmail(email)) {
                    Set<String> branches = Arrays.stream(split[1].split(Const.StrPool.COMMA))
                            .filter(b -> b != null && !b.strip().isBlank())
                            .collect(Collectors.toSet());
                    if (branches.contains(branchName)) {
                        emailSet.add(email);
                    }
                }
            }
        });
        return emailSet;
    }

    /**
     * 从指定路径读取文件内容
     *
     * @param path 路径
     * @return 文件内容
     * @throws IOException IO异常
     */
    private static String getFileContent(String path) throws IOException {
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }

    /**
     * 验证邮箱格式是否正确
     *
     * @param email 邮箱地址
     * @return 邮箱格式是否正确
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.strip().isBlank()) {
            return false;
        }
        return Const.EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 获取去除首尾空格的字符串
     *
     * @param str 原字符串
     * @return 去除首尾空格后的字符串
     */
    private static String getStripStr(String str) {
        if (str == null) {
            return Const.StrPool.EMPTY;
        }
        str = str.strip();
        if (str.isBlank()) {
            return Const.StrPool.EMPTY;
        }
        return str;
    }
}
