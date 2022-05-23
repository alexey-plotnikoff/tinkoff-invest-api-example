package ru.plotnikov.tinkoff.invest.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import ru.plotnikov.tinkoff.invest.service.InvestApiService;

@Controller("/private")
public class PrivateController {

    private final InvestApiService investApiService;

    public PrivateController(InvestApiService investApiService) {
        this.investApiService = investApiService;
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/accounts")
    public String accounts() {
        return String.valueOf(investApiService.getApi().getUserService().getAccountsSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/info")
    public String info() {
        return String.valueOf(investApiService.getApi().getUserService().getInfoSync());
    }
}
