package com.jiangzhiyan.clash.config.mail.sender;

import com.jiangzhiyan.clash.config.mail.sender.common.Const;
import com.jiangzhiyan.clash.config.mail.sender.common.utils.MailUtil;

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
        System.out.println("clash配置更新邮件通知任务开始...");
        try {
            // 1.获取订阅了改动分支的邮箱地址集合
            Set<String> validEmails = getBranchValidEmails();
            // 2.如果配置了邮箱地址,则发送邮件报告
            if (!validEmails.isEmpty()) {
                MailUtil.sendEmail(getConfigContent(), validEmails);
            }
        } catch (Exception e) {
            System.out.println("clash配置更新邮件通知任务发生异常:" + e.getMessage());
            System.exit(1);
        } finally {
            System.out.println("clash配置更新邮件通知任务结束...");
            System.exit(0);
        }
    }

    /**
     * 获取clash配置文件内容
     *
     * @return clash配置文件内容
     */
    private static String getConfigContent() {
        return getStripStr(System.getenv("CLASH_CONFIG_CONTENT")) + Const.StrPool.LF;
    }

    /**
     * 获取订阅了分支的有效的邮箱地址集合
     *
     * @return 订阅了分支的有效的邮箱地址集合
     */
    private static Set<String> getBranchValidEmails() {
        // 当前改动的分支名
        String branchName = System.getenv("BRANCH_NAME").toLowerCase();
        // 从环境变量中获取email和分支信息,格式xxx@yyy.com:branch1,branch2
        String emails = getStripStr(System.getenv("EMAIL_BRANCHES"));
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