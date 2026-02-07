package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHelper extends BaseHelper {

    private final JavaMailSender mailSender;

    @Override
    protected String getServiceName() {
        return "email";
    }

    public void sendTextEmail(String to, String subject, String body) {
        executeWithProtection("sendTextEmail", () -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        });
    }
}
