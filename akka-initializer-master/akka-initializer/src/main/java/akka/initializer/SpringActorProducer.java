package akka.initializer;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import akka.initializer.model.Parameters;
import org.springframework.context.ApplicationContext;

/**
 * An actor producer that lets Spring create the Actor instances.
 */
public class SpringActorProducer implements IndirectActorProducer {

    private final ApplicationContext applicationContext;
    private final String actorBeanName;
    private final Parameters parameters;

    public SpringActorProducer(ApplicationContext applicationContext,
                               String actorBeanName, Parameters parameters) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
        this.parameters = parameters;
    }

    @Override
    public Actor produce() {

        Actor actor = (Actor) applicationContext.getBean(actorBeanName);
        return setParametersIfPresent(actor);

    }

    private Actor setParametersIfPresent(Actor actor) {

        if (parameters != null) {

            if (!(actor instanceof ParameterInjector)) {
                throw new IllegalStateException("Found parameters but Actor: " + actorBeanName + " does not implement ParameterInjector.");
            }

            ParameterInjector parameterInjector = (ParameterInjector) actor;
            parameterInjector.setParameters(parameters);

        } else {
            if (actor instanceof ParameterInjector) {
                throw new IllegalStateException("Actor: " + actorBeanName + " implements ParameterInjector but no parameters found to set.");
            }
        }

        return actor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }
}
