package com.oanda.arbitrage.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.joda.time.DateTime;

@Getter
@Builder
@ToString
public class Candle {
    private String symbol;
    private Double ask;
    private Double bid;
    private DateTime time;
}
