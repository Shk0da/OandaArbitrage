package com.oanda.arbitrage;

import akka.actor.ActorSystem;
import com.oanda.arbitrage.actor.Messages;
import com.oanda.arbitrage.config.ActorConfig;
import com.oanda.arbitrage.domain.Candle;
import com.oanda.arbitrage.repository.CandleRepository;
import com.oanda.arbitrage.repository.InstrumentRepository;
import com.oanda.arbitrage.service.TradeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@EnableScheduling
@AllArgsConstructor
@Component("scheduler")
public class Scheduler {

    private final ActorSystem actorSystem;
    private final TradeService tradeService;
    private final InstrumentRepository instrumentRepository;
    private final CandleRepository candleRepository;

    @Scheduled(fixedDelayString = "${oanda.scheduler.collector.interval}")
    public void fireCollect() {
        tradeService.getCurrentPrices(instrumentRepository.getInstruments()).forEach(price ->
                candleRepository.addCandle(Candle.builder()
                        .symbol(price.getInstrument().toString())
                        .ask(price.getCloseoutAsk().doubleValue())
                        .bid(price.getCloseoutBid().doubleValue())
                        .time(new DateTime(price.getTime().toString(), DateTimeZone.getDefault()))
                        .build()));

        log.debug("Collect {} candles", candleRepository.getCandles().size());
    }

    @Scheduled(fixedDelayString = "${oanda.scheduler.calculate.interval}")
    public void fireCalculate() {
        if (actorSystem == null) return;
        actorSystem.actorSelection(ActorConfig.ACTOR_PATH_HEAD + "ManagerActor")
                .tell(Messages.Collect, actorSystem.guardian());
    }

    @Scheduled(fixedDelayString = "${oanda.scheduler.balance.check}")
    public void fireShowBalance() {
        log.warn("Balance: {} USD", tradeService.getBalance());
    }
}
