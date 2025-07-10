package com.pingsocial.controller;

import com.pingsocial.models.ChatMessage;
import com.pingsocial.repository.ChatMessageRepository;
import com.pingsocial.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate template;
    private final ChatMessageRepository chatMessageRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public ChatController(SimpMessagingTemplate template, ChatMessageRepository chatMessageRepository, EmailService emailService) {
        this.template = Objects.requireNonNull(template, "SimpMessagingTemplate não pode ser null");
        this.chatMessageRepository = Objects.requireNonNull(chatMessageRepository, "ChatMessageRepository não pode ser null");
        this.emailService = Objects.requireNonNull(emailService, "EmailService não pode ser null");
    }

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message, Principal principal) {
        if (message == null || principal == null) {
            logger.warn("Mensagem ou usuário principal nulo recebido, abortando processamento.");
            return;
        }

        String sender = principal.getName();
        message.setSender(sender);


        String timestamp = TIME_FORMATTER.format(Instant.now());
        message.setTimestamp(timestamp);

        logger.info("Enviando mensagem de {} para {}: {}", sender, message.getRecipient(), message.getText());


        try {
            chatMessageRepository.save(message);
            logger.info("Mensagem salva no banco de dados: {}", message);
        } catch (Exception ex) {
            logger.error("Erro ao persistir a mensagem no banco de dados: {}", ex.getMessage(), ex);
        }


        try {
            template.convertAndSendToUser(
                    message.getRecipient(),
                    "/queue/messages",
                    message
            );
        } catch (Exception ex) {
            logger.error("Erro ao enviar mensagem para o usuário {}: {}", message.getRecipient(), ex.getMessage(), ex);
        }


        try {
            String recipientEmail = message.getRecipient();

            if (recipientEmail != null) {
                String subject = "Você recebeu uma nova mensagem!";
                String body = "<html>"
                        + "<head>"
                        + "<style>"
                        + "body { font-family: 'Arial', sans-serif; color: #333333; margin: 0; padding: 0; background-color: #f4f7f6; }"
                        + ".container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }"
                        + ".header { text-align: center; margin-bottom: 20px; }"
                        + ".header h2 { color: #3b3b3b; font-size: 24px; margin: 0; }"
                        + ".message { font-size: 16px; line-height: 1.6; color: #555555; margin-bottom: 20px; }"
                        + ".footer { text-align: center; font-size: 12px; color: #aaa; padding: 20px 0; border-top: 1px solid #f1f1f1; }"
                        + ".button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: #fff; text-decoration: none; border-radius: 4px; font-size: 16px; margin-top: 15px; }"
                        + ".button:hover { background-color: #45a049; }"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div class='container'>"
                        + "<div class='header'>"
                        + "<h2>Você recebeu uma nova mensagem!</h2>"
                        + "</div>"
                        + "<div class='message'>"
                        + "<p><strong>De:</strong> " + sender + "</p>"
                        + "<p><strong>Mensagem:</strong> " + message.getText() + "</p>"
                        + "</div>"
                        + "<div class='footer'>"
                        + "<p>Para responder, acesse o <a href='https://ping-social-front-ym8d.vercel.app/#/auth/login' class='button'>Chat</a></p>"
                        + "<p>Obrigado por usar nosso serviço!</p>"
                        + "</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";


                emailService.sendEmail(recipientEmail, subject, body);
                logger.info("Notificação por e-mail enviada para {}", message.getRecipient());
            } else {
                logger.warn("E-mail do destinatário não encontrado para o usuário {}", message.getRecipient());
            }
        } catch (Exception ex) {
            logger.error("Erro ao enviar e-mail para o usuário {}: {}", message.getRecipient(), ex.getMessage(), ex);
        }
    }

}
