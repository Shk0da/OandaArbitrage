package com.oanda.arbitrage.config;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OandaConfig {

    @Value("${oanda.url}")
    private String url;

    @Value("${oanda.account.id}")
    private String accountId;

    @Value("${oanda.token}")
    private String token;

    @Value("${oanda.balance.risk}")
    private Double balanceRisk;

    @Value("${oanda.balance.currency}")
    private String balanceCurrency;

    @Bean("context")
    public Context context() {
        return new Context(url, token);
    }

    @Bean("accountID")
    public AccountID accountID() {
        return new AccountID(accountId);
    }

    @Bean("balanceRisk")
    public Double balanceRisk() {
        return balanceRisk;
    }

    @Bean("balanceCurrency")
    public String balanceCurrency() {
        return balanceCurrency;
    }
}
