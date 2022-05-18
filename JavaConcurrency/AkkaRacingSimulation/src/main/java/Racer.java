import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;

public class Racer extends AbstractBehavior<Racer.Command> {

    public interface Command extends Serializable{}

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
        private int raceLength;
    }

    private Racer(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create(){
        return Behaviors.setup(Racer::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }

}
