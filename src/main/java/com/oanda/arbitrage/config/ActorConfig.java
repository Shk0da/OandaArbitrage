package com.oanda.arbitrage.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.common.collect.Sets;
import com.oanda.arbitrage.actor.ManagerActor;
import com.oanda.arbitrage.actor.SpringDIActor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorConfig {

    public static final String ACTOR_PATH_HEAD = "akka://oanda-arbitrage/user/";

    @Bean(name = "actorSystem")
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = ActorSystem.create("oanda-arbitrage");
        actorSystem.actorOf(Props.create(SpringDIActor.class, ManagerActor.class), "ManagerActor");

        return actorSystem;
    }
}
