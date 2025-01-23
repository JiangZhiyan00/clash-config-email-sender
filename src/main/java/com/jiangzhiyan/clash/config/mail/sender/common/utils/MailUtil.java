package com.jiangzhiyan.clash.config.mail.sender.common.utils;

import com.jiangzhiyan.clash.config.mail.sender.common.ConfigType;
import com.jiangzhiyan.clash.config.mail.sender.common.Const;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author JiangZhiyan
 */
@UtilityClass
public class MailUtil {
    private static final String SENDER_EMAIL = "chatgpt6robot@gmail.com";
    private static final String SENDER_EMAIL_PASSWORD = "ygytdltldlszuarf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Properties PROPS;
    private static final String CLASH_CONFIG_URL_TEMPLATE = "https://proxy.jzy88.top/https%3A%2F%2Fraw.githubusercontent.com%2FJiangZhiyan00%2Fclash_config%2Frefs%2Fheads%2F{0}%2Fconfig.yml";

    static {
        PROPS = new Properties(4);
        PROPS.put("mail.smtp.host", "smtp.gmail.com");
        PROPS.put("mail.smtp.port", "587");
        PROPS.put("mail.smtp.auth", "true");
        PROPS.put("mail.smtp.starttls.enable", "true");
    }

    public static void sendEmail(String branchName, ConfigType configType, Set<String> toEmails, String commitAuthor, String commitMessage) {
        if (configType == null || toEmails == null || toEmails.isEmpty()) {
            return;
        }

        // 邮件正文内容
        String emailTextContent = String.join("<br/><br/>",
                "<strong>作者:</strong> " + commitAuthor,
                "<strong>更新描述:</strong> " + commitMessage,
                "<strong>文档:</strong> <a href='https://clash.opendoc.us.kg' target='_blank'>查看教程</a>",
                "<strong>订阅链接:</strong> <a href='" + MessageFormat.format(CLASH_CONFIG_URL_TEMPLATE, branchName) + "' target='_blank'><i style='color: #228B22;'><u>打开然后复制链接url</u></i></a>",
                "<i style='color: #999; font-size: smaller;'>此邮件由机器人自动发出，无需回复。</i>");

        Session session = Session.getInstance(PROPS, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_EMAIL_PASSWORD);
            }
        });

        // 创建CompletableFuture任务列表
        List<CompletableFuture<Void>> futures = new ArrayList<>(toEmails.size());

        for (String toEmail : toEmails) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(SENDER_EMAIL));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject(LocalDateTime.now().format(DATE_FORMATTER).concat("_" + configType.getName() + "配置更新通知"));
                    message.setContent(emailTextContent, "text/html; charset=UTF-8");

                    // Send the email
                    Transport.send(message);
                    System.out.println(toEmail + Const.StrPool.COMMA + configType.getName() + "配置更新邮件通知发送成功.");
                } catch (MessagingException e) {
                    System.err.println(toEmail + Const.StrPool.COMMA + configType.getName() + "配置更新邮件通知发送失败.");
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        // 创建一个包含所有任务的CompletableFuture
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        // 等待所有任务完成
        try {
            allOf.get();
            System.out.println("汇总: [" + String.join(Const.StrPool.COMMA, toEmails) + "]" + configType.getName() + "配置更新邮件通知发送成功.");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
