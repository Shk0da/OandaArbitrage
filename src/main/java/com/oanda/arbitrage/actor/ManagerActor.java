package com.oanda.arbitrage.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.google.common.collect.Lists;
import com.oanda.arbitrage.repository.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Scope("prototype")
@Component("managerActor")
public class ManagerActor extends UntypedAbstractActor {

    @Autowired
    private GraphRepository graphRepository;

    private final List<ActorRef> collectors = Lists.newArrayList();

    @Override
    public void preStart() {
        graphRepository.getTriangularList().forEach(
                triangular -> collectors.add(
                        getContext().actorOf(Props.create(SpringDIActor.class, CalculateActor.class, triangular))
                )
        );
    }

    @Override
    public void onReceive(Object message) {
        if (Messages.Collect.equals(message)) {
            collectors.forEach(actorRef -> actorRef.tell(message, sender()));
        } else {
            unhandled(message);
        }
    }
}
