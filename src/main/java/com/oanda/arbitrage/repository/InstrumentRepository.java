package com.oanda.arbitrage.repository;

import com.google.common.collect.Lists;
import com.oanda.arbitrage.service.TradeService;
import com.oanda.v20.primitives.Instrument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InstrumentRepository {

    private final Map<String, Instrument> instruments = new ConcurrentHashMap<>();

    public InstrumentRepository(TradeService tradeService) {
        addInstruments(tradeService.getInstruments());
    }

    public void addInstrument(Instrument instrument) {
        instruments.put(instrument.getDisplayName(), instrument);
    }

    public void addInstruments(List<Instrument> instrumentList) {
        instrumentList.forEach(this::addInstrument);
    }

    public Instrument getInstrument(String displayName) {
        return instruments.getOrDefault(displayName, null);
    }

    public List<Instrument> getInstruments() {
        return Lists.newArrayList(instruments.values());
    }

    public List<String> getSymbols() {
        return Lists.newArrayList(instruments.keySet());
    }
}
