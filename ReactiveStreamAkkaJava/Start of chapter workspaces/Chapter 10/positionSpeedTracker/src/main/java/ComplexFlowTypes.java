import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.*;
import akka.stream.javadsl.*;
import scala.Int;

import java.util.concurrent.CompletionStage;

public class ComplexFlowTypes {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1, 10);

        Flow<Integer, Integer, NotUsed> flow1 = Flow.of(Integer.class)
                .map(x -> {
                    System.out.println("Flow 1 is processing " + x);
                    return (x * 2);
                });

        Flow<Integer, Integer, NotUsed> flow2 = Flow.of(Integer.class)
                .map(x -> {
                    System.out.println("Flow 2 is processing " + x);
                    return (x + 1);
                });

        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
                GraphDSL.create(sink, (builder, out) -> {
                    SourceShape<Integer> sourceShape = builder.add(source);

                    FlowShape<Integer, Integer> flowShape1 = builder.add(flow1);
                    FlowShape<Integer, Integer> flowShape2 = builder.add(flow2);

                    UniformFanOutShape<Integer, Integer> boardcast =
                            builder.add(Broadcast.create(2));

                    UniformFanInShape<Integer, Integer> merge =
                            builder.add(Merge.create(2));

//                    builder.from(sourceShape)
//                            .viaFanOut(boardcast);
//
//                    builder.from(boardcast.out(0))
//                            .via(flowShape1);
//
//                    builder.from(boardcast.out(1))
//                            .via(flowShape2);
//
//                    builder.from(flowShape1)
//                            .toInlet(merge.in(0));
//
//                    builder.from(flowShape2)
//                            .toInlet(merge.in(1));
//
//                    builder.from(merge)
//                            .to(out);

                    builder.from(sourceShape)
                            .viaFanOut(boardcast)
                            .via(flowShape1);

                    builder.from(boardcast).via(flowShape2);

                    builder.from(flowShape1)
                            .viaFanIn(merge)
                            .to(out);

                    builder.from(flowShape2).viaFanIn(merge);


                    return ClosedShape.getInstance();
                })
        );

        graph.run(actorSystem);
    }
}
