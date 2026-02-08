package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Component
public class TelegramHelper extends BaseHelper {

    private TelegramBot bot;

    @Value("${telegram.bot.token:}")
    private String token;

    @Override
    protected String getServiceName() {
        return "telegram";
    }

    @PostConstruct
    public void init() {
        if (token != null && !token.isBlank()) {
            this.bot = new TelegramBot(token);
            log.info("TelegramHelper initialized successfully.");
        } else {
            log.warn("Telegram bot token is missing. TelegramHelper will not be functional.");
        }
    }

    public void sendMessage(String chatId, String text) {
        if (bot == null)
            return;
        executeWithProtection("sendMessage", () -> {
            SendMessage request = new SendMessage(chatId, text);
            SendResponse response = bot.execute(request);
            if (!response.isOk()) {
                throw new RuntimeException("Telegram API error: " + response.description());
            }
        });
    }

    public void sendMessageWithInlineMenu(String chatId, String text, Map<String, String> buttons) {
        if (bot == null)
            return;
        executeWithProtection("sendMessageWithInlineMenu", () -> {
            InlineKeyboardButton[] row = buttons.entrySet().stream()
                    .map(entry -> new InlineKeyboardButton(entry.getKey()).callbackData(entry.getValue()))
                    .toArray(InlineKeyboardButton[]::new);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(row);
            SendMessage request = new SendMessage(chatId, text).replyMarkup(markup);
            bot.execute(request);
        });
    }

    public void editMessage(String chatId, String messageId, String newText) {
        if (bot == null)
            return;
        executeWithProtection("editMessage", () -> {
            EditMessageText request = new EditMessageText(chatId, Integer.parseInt(messageId), newText);
            bot.execute(request);
        });
    }

    public void sendNotification(String chatId, String text) {
        sendMessage(chatId, "🔔 " + text);
    }
}
