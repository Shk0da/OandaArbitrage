package com.oanda.arbitrage.actor;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import com.oanda.arbitrage.provider.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class SpringDIActor implements IndirectActorProducer {

    private Actor actorInstance;
    private Class<? extends Actor> type;
    private Set<String> graph;

    public SpringDIActor(Class<? extends Actor> type) {
        this.type = type;
    }

    public SpringDIActor(Class<? extends Actor> type, Set<String> graph) {
        this.type = type;
        this.graph = graph;
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return type;
    }

    @Override
    public Actor produce() {
        Actor newActor = actorInstance;
        actorInstance = null;
        if (newActor == null) {
            try {
                if (graph != null) {
                    newActor = type.getConstructor(Set.class).newInstance(graph);
                } else {
                    newActor = type.getConstructor().newInstance();
                }

                ApplicationContextProvider.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(newActor);
            } catch (Exception e) {
                log.error("Unable to create actor of type: {}. {}. {}", type, graph, e);
            }
        }

        return newActor;
    }
}