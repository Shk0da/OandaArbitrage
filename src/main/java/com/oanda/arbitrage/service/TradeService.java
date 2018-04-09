package com.oanda.arbitrage.service;

import com.google.common.collect.Lists;
import com.oanda.v20.Context;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.order.*;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.primitives.Instrument;
import com.oanda.v20.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TradeService {

    private final Double balanceRisk;
    private final Context context;
    private final AccountID accountID;

    public List<Instrument> getInstruments() {
        try {
            return context.account.instruments(accountID).getInstruments();
        } catch (RequestException ex) {
            log.error("GetInstruments: {}", ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("GetInstruments: {}", ex.getMessage());
        }

        return Lists.newArrayList();
    }

    public List<Candlestick> getLastCandles(Instrument instrument, int size) {
        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(instrument.getName());
            request.setPrice("AB");
            request.setCount(size);
            return context.instrument.candles(request).getCandles();
        } catch (RequestException ex) {
            log.error("GetLastCandles: {}", ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("GetLastCandles: {}", ex.getMessage());
        }

        return Lists.newArrayList();
    }

    public List<Price> getCurrentPrices(List<Instrument> instruments) {
        try {
            return context.pricing.get(accountID, instruments.stream()
                    .map(Instrument::getName)
                    .collect(Collectors.toList())).getPrices();
        } catch (RequestException ex) {
            log.error("GetCurrentPrices: {}", ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("GetCurrentPrices: {}", ex.getMessage());
        }

        return Lists.newArrayList();
    }

    public void createMarketOrder(String instrument, double price, int side) {
        try {
            MarketOrderRequest orderRequest = new MarketOrderRequest();
            // GTC The Order is “Good unTil Cancelled”
            // GTD The Order is “Good unTil Date” and will be cancelled at the provided time
            // GFD The Order is “Good For Day” and will be cancelled at 5pm New York time
            // FOK The Order must be immediately “Filled Or Killed”
            // IOC The Order must be “Immediatedly paritally filled Or Cancelled”
            orderRequest.setTimeInForce(TimeInForce.FOK);
            orderRequest.setPositionFill(OrderPositionFill.DEFAULT);
            orderRequest.setType(OrderType.MARKET);
            orderRequest.setUnits(getMaxUnits(price, side));
            orderRequest.setInstrument(instrument);

            OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountID);
            orderCreateRequest.setOrder(orderRequest);
            Transaction transaction = context.order.create(orderCreateRequest).getOrderCreateTransaction();
            log.info("Transaction: {} {} {} {} {}", transaction.getId(), instrument, orderRequest.getUnits(),
                    transaction.getType(), transaction.getTime());
        } catch (RequestException ex) {
            log.error("CreateOrder: {}", ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("CreateOrder: {}", ex.getMessage());
        }
    }

    public double getBalance() {
        try {
            return context.account.summary(accountID).getAccount().getBalance().doubleValue();
        } catch (RequestException ex) {
            log.error("GetBalance: {}", ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("GetBalance: {}", ex.getMessage());
        }

        return 0;
    }

    private Integer getMaxUnits(double price, int side) {
        try {
            return Math.abs((int) (getBalance() / price * (balanceRisk * 0.01))) * (side > 0 ? 1 : -1);
        } catch (Exception ex) {
            log.error("GetMaxUnits: {}", ex.getMessage());
        }

        return 0;
    }
}
