package com.taingy.expensetracker.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.taingy.expensetracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class SendGridEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmailServiceImpl.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.name}")
    private String appName;

    @Value("${otp.expire-in-minute}")
    private int OTP_EXPIRY_MINUTES;

    @Override
    @Async
    public void sendOtpEmail(String email, String otpCode, String firstName) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email, firstName);
            String subject = "Email Verification";

            String htmlContent = buildOtpEmailTemplate(email, otpCode, firstName);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("OTP email sent via SendGrid to {}", email);
            } else {
                logger.error("Failed to send OTP email via SendGrid. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Failed to send OTP email via SendGrid", e);
        }
    }

   private String buildOtpEmailTemplate(String email, String otpCode, String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .otp-box {
                            background-color: white;
                            padding: 20px;
                            margin: 20px 0;
                            border: 2px solid #4CAF50;
                            border-radius: 8px;
                            text-align: center;
                        }
                        .otp-code {
                            font-size: 32px;
                            font-weight: bold;
                            color: #4CAF50;
                            letter-spacing: 8px;
                            font-family: 'Courier New', monospace;
                        }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Email Verification</h1>
                        </div>
                        <div class="content">
                            <p>Dear %s,</p>
                            <p>Thank you for registering with %s. To complete your registration, please verify your email address using the OTP code below:</p>

                            <div class="otp-box">
                                <p style="margin: 0; font-size: 14px; color: #666;">Your verification code is:</p>
                                <div class="otp-code">%s</div>
                                <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">This code will expire in %s minutes.</p>
                            </div>

                            <p>Enter this code in the verification page to activate your account.</p>
                        </div>
                        <div class="footer">
                            <p>This email was intended for %s</p>
                            <p>Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                firstName,
                appName,
                otpCode,
                OTP_EXPIRY_MINUTES,
                email
        );
    }
}
