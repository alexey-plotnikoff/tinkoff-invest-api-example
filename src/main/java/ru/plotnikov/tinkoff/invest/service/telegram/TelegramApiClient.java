package ru.plotnikov.tinkoff.invest.service.telegram;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.obj.Message;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.TelegramResponse;
import ru.plotnikov.tinkoff.invest.service.telegram.dto.obj.Update;

@Client("https://api.telegram.org/bot${trading.telegram.access_token:}")
public interface TelegramApiClient {

    @Post("/sendMessage")
    TelegramResponse<Message> sendMessage(@QueryValue(value = "chat_id") String chatId, @QueryValue String text);

    @Get("/getUpdates")
    TelegramResponse<Update[]> getUpdates();

}
