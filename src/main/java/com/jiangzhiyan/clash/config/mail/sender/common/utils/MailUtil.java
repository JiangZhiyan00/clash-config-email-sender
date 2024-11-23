package com.jiangzhiyan.clash.config.mail.sender.common.utils;

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

    static {
        PROPS = new Properties(4);
        PROPS.put("mail.smtp.host", "smtp.gmail.com");
        PROPS.put("mail.smtp.port", "587");
        PROPS.put("mail.smtp.auth", "true");
        PROPS.put("mail.smtp.starttls.enable", "true");
    }

    public static void sendEmail(String content, Set<String> toEmails) throws IOException {
        if (content == null || content.isBlank() || toEmails == null || toEmails.isEmpty()) {
            return;
        }
        // 创建临时文件
        String fileName = LocalDateTime.now().format(DATE_FORMATTER2) + "_config.yml";
        File tempFile = File.createTempFile(LocalDateTime.now().format(DATE_FORMATTER2) + "_config", ".yml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
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
                    message.setSubject(LocalDateTime.now().format(DATE_FORMATTER).concat("_clash配置更新通知"));

                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(tempFile);
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                    attachmentBodyPart.setFileName(fileName);

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(attachmentBodyPart);

                    message.setContent(multipart);

                    // Send the email
                    Transport.send(message);
                    System.out.println('[' + toEmail + "]clash配置更新邮件通知发送成功.");
                } catch (MessagingException e) {
                    System.out.println('[' + toEmail + "]clash配置更新邮件通知发送失败.");
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
            System.out.println('[' + String.join(Const.StrPool.COMMA, toEmails) + "]clash配置更新邮件通知发送成功.");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
