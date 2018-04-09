package com.oanda.arbitrage.repository;

import com.google.common.collect.Lists;
import com.oanda.arbitrage.domain.Candle;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CandleRepository {

    private final Map<String, Candle> candles = new ConcurrentHashMap<>();
    
    public void addCandle(Candle candle) {
        candles.put(candle.getSymbol(), candle);
    }

    public void addCandles(List<Candle> candleList) {
        candleList.forEach(this::addCandle);
    }

    public Candle getCandle(String symbol) {
        return candles.getOrDefault(symbol, null);
    }

    public List<Candle> getCandles() {
        return Lists.newArrayList(candles.values());
    }
}
