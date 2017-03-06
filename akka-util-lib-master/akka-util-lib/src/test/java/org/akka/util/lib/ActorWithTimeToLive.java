package org.akka.util.lib;

import org.akka.util.lib.model.Time;
import org.akka.util.lib.model.TimeToLive;
import org.akka.util.lib.model.Parameters;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * Helps to test actor removal functionality by implement TimeToLive interface.
 */
public class ActorWithTimeToLive extends MessageExpiryDetector implements TimeToLive {

    private Time actorTTL;

    public ActorWithTimeToLive(ApplicationContext applicationContext, Parameters parameters) {
        super(applicationContext, parameters);
        actorTTL = new Time(parameters.parseLong(ACTOR_TIME_TO_LIVE_SECONDS), TimeUnit.SECONDS);
    }

    @Override
    public Time actorTtl() {
        return actorTTL;
    }
}
