package ru.plotnikov.tinkoff.invest.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import ru.plotnikov.tinkoff.invest.service.InvestApiService;

@Controller("/info")
public class InfoController {

    private final InvestApiService investApiService;

    public InfoController(InvestApiService investApiService) {
        this.investApiService = investApiService;
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/all-bonds")
    public String allBonds() {
        return String.valueOf(investApiService.getApi().getInstrumentsService().getAllBondsSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/all-shares")
    public String allShares() {
        return String.valueOf(investApiService.getApi().getInstrumentsService().getAllSharesSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/all-currencies")
    public String allCurrencies() {
        return String.valueOf(investApiService.getApi().getInstrumentsService().getAllCurrenciesSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/all-etfs")
    public String allEtfs() {
        return String.valueOf(investApiService.getApi().getInstrumentsService().getAllEtfsSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/all-futures")
    public String allFutures() {
        return String.valueOf(investApiService.getApi().getInstrumentsService().getAllFuturesSync());
    }
}
