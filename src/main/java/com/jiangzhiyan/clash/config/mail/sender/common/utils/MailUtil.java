package com.jiangzhiyan.clash.config.mail.sender.common.utils;

import com.jiangzhiyan.clash.config.mail.sender.common.ConfigType;
import com.jiangzhiyan.clash.config.mail.sender.common.Const;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final DateTimeFormatter DATE_FORMATTER2 = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Properties PROPS;
    private static final String CLASH_CONFIG_URL_TEMPLATE = "https://proxy.jzy88.top/https%3A%2F%2Fraw.githubusercontent.com%2FJiangZhiyan00%2Fclash_config%2Frefs%2Fheads%2F{0}%2Fclash%2Fconfig.yml";

    static {
        PROPS = new Properties(4);
        PROPS.put("mail.smtp.host", "smtp.gmail.com");
        PROPS.put("mail.smtp.port", "587");
        PROPS.put("mail.smtp.auth", "true");
        PROPS.put("mail.smtp.starttls.enable", "true");
    }

    public static void sendEmail(String branchName, ConfigType configType, String fileContent, Set<String> toEmails, String commitAuthor, String commitMessage) throws IOException, MessagingException {
        if (configType == null || fileContent == null || fileContent.isBlank() || toEmails == null || toEmails.isEmpty()) {
            return;
        }

        // 邮件文字内容
        MimeBodyPart textBodyPart = getEmailContent(branchName, configType, commitAuthor, commitMessage);

        // 文件内容,clash使用订阅链接,不使用配置文件
        File tempFile = null;
        MimeBodyPart attachmentBodyPart;
        if (configType == ConfigType.CLASH) {
            // 创建临时文件
            String fileName = LocalDateTime.now().format(DATE_FORMATTER2) + "_config" + configType.getSuffix();
            tempFile = File.createTempFile(LocalDateTime.now().format(DATE_FORMATTER2) + "_config", configType.getSuffix());
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(fileContent);
            }
            attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(tempFile);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(fileName);
        } else {
            attachmentBodyPart = null;
        }

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

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(textBodyPart);
                    if (attachmentBodyPart != null) {
                        multipart.addBodyPart(attachmentBodyPart);
                    }

                    message.setContent(multipart);

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
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 获取邮件正文内容
     *
     * @param branchName    分支名
     * @param configType    配置类型
     * @param commitAuthor  提交者
     * @param commitMessage 提交信息
     * @return 邮件正文内容
     * @throws MessagingException 正文内容获取异常
     */
    private static MimeBodyPart getEmailContent(String branchName, ConfigType configType, String commitAuthor, String commitMessage) throws MessagingException {
        List<String> emailContent = configType == ConfigType.CLASH
                ? List.of("<strong>作者:</strong> " + commitAuthor,
                "<strong>更新描述:</strong> " + commitMessage,
                "<strong>文档:</strong> <a href='" + configType.getDocUrl() + "' target='_blank'>查看教程</a>",
                "<strong>订阅链接:</strong> <a href='" + MessageFormat.format(CLASH_CONFIG_URL_TEMPLATE, branchName) + "' target='_blank'><i style='color: #228B22;'><u>打开然后复制链接url</u></i></a>",
                "<i style='color: #999; font-size: smaller;'>此邮件由机器人自动发出，无需回复。</i>")
                : List.of("<strong>作者:</strong> " + commitAuthor,
                "<strong>更新描述:</strong> " + commitMessage,
                "<strong>文档:</strong> <a href='" + configType.getDocUrl() + "' target='_blank'>查看教程</a>",
                "<i style='color: #999; font-size: smaller;'>此邮件由机器人自动发出，无需回复。</i>");

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(String.join("<br/><br/>", emailContent), "text/html; charset=utf-8");
        return textBodyPart;
    }
}
