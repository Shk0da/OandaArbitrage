package com.oanda.arbitrage.actor;

import akka.actor.UntypedAbstractActor;
import com.oanda.arbitrage.domain.Candle;
import com.oanda.arbitrage.repository.CandleRepository;
import com.oanda.arbitrage.repository.GraphRepository;
import com.oanda.arbitrage.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Scope("prototype")
@Component("calculateActor")
public class CalculateActor extends UntypedAbstractActor {

    private static final Object lock = new Object();

    private final Set<String> graph;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private CandleRepository candleRepository;

    private volatile long lastUpdate;

    public CalculateActor(Set<String> graph) {
        this.graph = graph;
    }

    @Override
    public void onReceive(Object message) {
        if (!Messages.Collect.equals(message)) return;

        if (graph.size() == GraphRepository.TRIANGULAR_SIZE) {
            String[] symbols = graph.stream()
                    .map(symbol -> symbol = symbol.replace('/', '_'))
                    .collect(Collectors.toSet())
                    .toArray(new String[]{});

            // bue
            Candle vertex1 = candleRepository.getCandle(symbols[0]);
            // sell
            Candle vertex2 = candleRepository.getCandle(symbols[1]);
            // sell
            Candle vertex3 = candleRepository.getCandle(symbols[2]);

            if (vertex1 == null || vertex2 == null || vertex3 == null) return;

            long currentUpdate = Long.min(
                    Long.min(
                            vertex1.getTime().getMillis(),
                            vertex2.getTime().getMillis()),
                    vertex3.getTime().getMillis()
            );

            if (lastUpdate == currentUpdate) return;

            double result = vertex1.getAsk() - vertex2.getBid() * vertex3.getBid();
            double spread = vertex1.getAsk() - vertex1.getBid();
            double diff = result - spread;
            if (diff > 0) {
                synchronized (lock) {
                    log.info("****************** START ARBITRAGE ******************");
                    log.info("{} - {} * {} = {}, diff = {}", symbols[0], symbols[1], symbols[2], result, diff);
                    tradeService.createMarketOrder(vertex1.getSymbol(), vertex1.getAsk(), 1);
                    tradeService.createMarketOrder(vertex2.getSymbol(), vertex2.getBid(), -1);
                    tradeService.createMarketOrder(vertex3.getSymbol(), vertex2.getBid(), -1);
                    log.info("******************* END ARBITRAGE *******************");
                }
            }

            lastUpdate = currentUpdate;
        }
    }
}
