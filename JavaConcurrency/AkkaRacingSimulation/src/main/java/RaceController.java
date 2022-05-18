import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;

public class RaceController extends AbstractBehavior<RaceController.Command>{

    public interface Command extends Serializable {}

    private RaceController(ActorContext<RaceController.Command> context) {
        super(context);
    }

    public static Behavior<RaceController.Command> create(){
        return Behaviors.setup(RaceController::new);
    }

    @Override
    public Receive<RaceController.Command> createReceive() {
        return null;
    }
}
