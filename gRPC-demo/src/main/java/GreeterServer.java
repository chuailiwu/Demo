import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreeterServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreeterImpl())
                .build();

        server.start();
        server.awaitTermination();
    }
}
