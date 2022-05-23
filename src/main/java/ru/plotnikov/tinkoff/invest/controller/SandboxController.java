package ru.plotnikov.tinkoff.invest.controller;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import ru.plotnikov.tinkoff.invest.service.InvestApiService;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

@Controller("/sandbox")
public class SandboxController {

    private final InvestApiService investApiService;
    private final String accountId;

    public SandboxController(
            InvestApiService investApiService,
            @Value("${trading.tinkoff.account-id}") String accountId
    ) {
        this.investApiService = investApiService;
        this.accountId = accountId;
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/create")
    public String create() {
        return investApiService.getApi().getSandboxService().openAccountSync();
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/receipt/{sum}")
    public String receipt(@QueryValue long sum) {
        investApiService.getApi().getSandboxService().payInSync(
                accountId,
                MoneyValue.newBuilder().setCurrency("RUB").setUnits(sum).build()
        );
        return "ok";
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/accounts")
    public String accounts() {
        return String.valueOf(investApiService.getApi().getSandboxService().getAccountsSync());
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/portfolio")
    public String portfolio() {
        return String.valueOf(investApiService.getApi().getSandboxService().getPortfolioSync(accountId));
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/positions")
    public String positions() {
        return String.valueOf(investApiService.getApi().getSandboxService().getPositionsSync(accountId));
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/order-state/{id}")
    public String orderState(@QueryValue String id) {
        return String.valueOf(investApiService.getApi().getSandboxService().getOrderStateSync(accountId, id));
    }

    @Get(produces = MediaType.TEXT_PLAIN, value = "/orders")
    public String orders() {
        return String.valueOf(investApiService.getApi().getSandboxService().getOrdersSync(accountId));
    }
}
