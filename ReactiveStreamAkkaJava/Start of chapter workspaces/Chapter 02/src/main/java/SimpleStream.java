import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class SimpleStream {
    public static void main(String[] args) {
        // Finite Source
//        Source<Integer, NotUsed> source = Source.range(1, 10, 2);
//        Source<Integer, NotUsed> source = Source.single(17);
        List<String> names = List.of("James", "Jill", "Simon", "Sally", "Denise", "David");
        Source<String, NotUsed> source = Source.from(names);

        // Infinite Source
        Source<Double, NotUsed> piSource = Source.repeat(3.14592654);
        Source<String, NotUsed> repeatingName = Source.cycle(names::iterator);

        Iterator<Integer> infiniteRange = Stream.iterate(0, i -> i + 1).iterator();
        Source<Integer, NotUsed> infiniteRangeSource = Source
                .fromIterator(() -> infiniteRange)
                .throttle(1, Duration.ofSeconds(3))
                .take(5);

        Flow<Integer, String, NotUsed> flow = Flow.of(Integer.class).map(value -> "The next value is " + value);
        Flow<String, String, NotUsed> stringFlow = Flow.of(String.class).map(value -> "The next value is " + value);
        Flow<Double, String, NotUsed> doubleFlow = Flow.of(Double.class).map(value -> "The next value is " + value);

        Sink<String, CompletionStage<Done>> ignoreSink = Sink.ignore();
        Sink<String, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

//        RunnableGraph<NotUsed> graph = source.via(flow).to(sink);
        RunnableGraph<NotUsed> graph = infiniteRangeSource.via(flow).to(sink);

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

//        graph.run(actorSystem);
        // Convenience methods
        // sink.runWith(repeatingName.via(stringFlow), actorSystem);
        // equivalent to repeatingName.via(stringFlow).to(sink).run(actorSystem)
        // repeatingName.via(stringFlow).runForeach(System.out::println, actorSystem);
    }
}
