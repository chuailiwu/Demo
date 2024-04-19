import greet.Greet;
import greet.GreeterGrpc;
import io.grpc.stub.StreamObserver;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(Greet.HelloRequest request, StreamObserver<Greet.HelloReply> responseObserver) {
        Greet.HelloReply reply = Greet.HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void lotsOfReplies(Greet.HelloRequest request, StreamObserver<Greet.HelloReply> responseObserver) {
        for (int i = 0; i < 10; i++) {
            Greet.HelloReply reply = Greet.HelloReply.newBuilder().setMessage("Hello " + request.getName() + ", response number: " + i).build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Greet.HelloRequest> lotsOfGreetings(StreamObserver<Greet.HelloReply> responseObserver) {
        return new StreamObserver<Greet.HelloRequest>() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void onNext(Greet.HelloRequest helloRequest) {
                sb.append("Hello ").append(helloRequest.getName()).append("! ");
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Greet.HelloReply.newBuilder().setMessage(sb.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Greet.HelloRequest> bidiHello(StreamObserver<Greet.HelloReply> responseObserver) {
        return new StreamObserver<Greet.HelloRequest>() {
            @Override
            public void onNext(Greet.HelloRequest helloRequest) {
                responseObserver.onNext(Greet.HelloReply.newBuilder().setMessage("Hello " + helloRequest.getName()).build());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
