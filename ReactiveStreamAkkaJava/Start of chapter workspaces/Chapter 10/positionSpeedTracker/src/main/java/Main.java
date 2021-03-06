import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Map<Integer, VehiclePositionMessage> vehicleTrackingMap = new HashMap<>();
        for (int i = 1; i <=8; i++) {
            vehicleTrackingMap.put(i, new VehiclePositionMessage(1, new Date(), 0,0));
        }

        Random random = new Random();
        //source - repeat some value every 10 seconds.
        Source<Integer, NotUsed> source = Source.repeat(1)
                .throttle(1, Duration.ofSeconds(10));

        //flow 1 - transform into the ids of each van (ie 1..8) with mapConcat
//        Flow<Integer, Integer, NotUsed> transformIdFlow = Flow.of(Integer.class)
//                .mapConcat(value -> {
//                    List<Integer> vehicleIdList = new ArrayList<>();
//                    for (int i = value; i <=8; i++) {
//                        vehicleIdList.add(i);
//                    }
//                    return vehicleIdList;
//                });
        Flow<Integer, Integer, NotUsed> transformIdFlow = Flow.of(Integer.class)
                .mapConcat(value -> List.of(1,2,3,4,5,6,7,8));

        //flow 2 - get position for each van as a VPMs with a call to the lookup method (create a new instance of
        //utility functions each time). Note that this process isn't instant so should be run in parallel.
        Flow<Integer, VehiclePositionMessage, NotUsed> getPositionFlow = Flow.of(Integer.class)
                .mapAsyncUnordered(4, value -> {

                    CompletableFuture<VehiclePositionMessage> futurePosition = new CompletableFuture<>();

                    futurePosition.completeAsync(() -> {
                        VehiclePositionMessage position = new UtilityFunctions().getVehiclePosition(value);
                        System.out.println("Vehicle " + value + " Lat " + position.getLatPosition() + " Long " + position.getLongPosition());
                        return position;
                    });

                    return futurePosition;
                });
        //flow 3 - use previous position from the map to calculate the current speed of each vehicle. Replace the
        // position in the map with the newest position and pass the current speed downstream
        Flow<VehiclePositionMessage, VehicleSpeed, NotUsed> getSpeedFlow = Flow.of(VehiclePositionMessage.class)
                .map(value -> {

                    VehicleSpeed speed = new UtilityFunctions().calculateSpeed(value, vehicleTrackingMap.get(value.getVehicleId()));
                    System.out.println("Vehicle " + value + " Speed " + speed);

                    vehicleTrackingMap.replace(value.getVehicleId(), value);

                    return speed;
                });

        //flow 4 - filter to only keep those values with a speed > 95
        Flow<VehicleSpeed, VehicleSpeed, NotUsed> speedFilterFlow = Flow.of(VehicleSpeed.class).filter(speed -> speed.getSpeed() > 95);

        //sink - as soon as 1 value is received return it as a materialized value, and terminate the stream
        Sink<VehicleSpeed, CompletionStage<VehicleSpeed>> sink = Sink.head();
//        CompletionStage<VehicleSpeed> result = source
//                .via(transformIdFlow)
//                .async()
//                .via(getPositionFlow)
//                .async()
//                .via(getSpeedFlow)
//                .via(speedFilterFlow)
//                .toMat(Sink.head(), Keep.right())
//                .run(actorSystem);

        RunnableGraph<CompletionStage<VehicleSpeed>> graph = RunnableGraph.fromGraph(
                GraphDSL.create( sink, (builder, out) -> {
                    SourceShape<Integer> sourceShape = builder.add(source);


                    UniformFanOutShape<Integer, Integer> balance = builder.add(Balance.create(8, true));
                    UniformFanInShape<VehiclePositionMessage, VehiclePositionMessage> merge = builder.add(Merge.create(8));

                    FlowShape<Integer, Integer> vehicleIdShape = builder.add(transformIdFlow);

//                    FlowShape<Integer, VehiclePositionMessage> vehiclePositionShape = builder.add(getPositionFlow.async());

                    FlowShape<VehiclePositionMessage, VehicleSpeed> vehicleSpeedShape = builder.add(getSpeedFlow);
                    FlowShape<VehicleSpeed, VehicleSpeed> speedFilterShape = builder.add(speedFilterFlow);

                    // DON'T NEED TO DO THIS - out IS OUR SINKSHAPE
                    // SinkShape<VehicleSpeed> resultShape = builder.add(sink);
                    builder.from(sourceShape)
                            .via(vehicleIdShape)
                            .viaFanOut(balance);

                    for (int i = 0; i < 8; i++) {
                        builder.from(balance)
                                .via(builder.add(getPositionFlow.async()))
                                .toFanIn(merge);
                    }


                    builder.from(merge)
                            .via(vehicleSpeedShape)
                            .via(speedFilterShape)
                            .to(out);

                    return ClosedShape.getInstance();
                })
        );

        CompletionStage<VehicleSpeed> result = graph.run(actorSystem);

        result.whenComplete( (value, throwable) -> {
            if (throwable == null) {
                System.out.println("The graph's materialized value is " + value.getSpeed());
            } else {
                System.out.println("Something went wrong " + throwable);
            }

            actorSystem.terminate();
        });

    }

}
