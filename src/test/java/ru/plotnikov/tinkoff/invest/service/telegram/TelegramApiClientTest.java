package ru.plotnikov.tinkoff.invest.service.telegram;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.TelegramResponse;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.obj.Message;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.obj.Update;

@MicronautTest
@Disabled
public class TelegramApiClientTest {

    @Inject TelegramApiClient telegramApiClient;

    @Test
    void getUpdates() {
        TelegramResponse<Update[]> updates = telegramApiClient.getUpdates();
    }

    @Test
    void sendMessage() {
        TelegramResponse<Message> hello = telegramApiClient.sendMessage("", "hello");
    }

}
