package ru.plotnikov.tinkoff.invest.service;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import ru.tinkoff.piapi.core.InvestApi;

@Singleton

public class InvestApiService {
    private final InvestApi api;

    public InvestApiService(
            @Value("${trading.tinkoff.token}") String apiToken,
            @Value("${trading.tinkoff.sandbox:false}") boolean sandbox
    ) {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalArgumentException("API token is empty");
        }

        if (sandbox) {
            api = InvestApi.createSandbox(apiToken);
        } else {
            api = InvestApi.create(apiToken);
        }
    }

    public InvestApi getApi() {
        return api;
    }
}
