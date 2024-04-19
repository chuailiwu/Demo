import greet.Greet;
import greet.GreeterGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreeterClient {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterStub asyncStub;

    public GreeterClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // 不加密通信
                .build());
    }

    GreeterClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        asyncStub = GreeterGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // 一元 RPC
    public void greet(String name) {
        Greet.HelloRequest request = Greet.HelloRequest.newBuilder().setName(name).build();
        Greet.HelloReply response = blockingStub.sayHello(request);
        System.out.println("Greeting: " + response.getMessage());
    }

    // 服务器流式 RPC
    public void greetLotsOfReplies(String name) {
        Greet.HelloRequest request = Greet.HelloRequest.newBuilder().setName(name).build();
        blockingStub.lotsOfReplies(request).forEachRemaining(
                response -> System.out.println("Greeting: " + response.getMessage())
        );
    }

    // 客户端流式 RPC
    public void greetLotsOfGreetings(String[] names) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<Greet.HelloRequest> requestObserver = asyncStub.lotsOfGreetings(new StreamObserver<Greet.HelloReply>() {
            @Override
            public void onNext(Greet.HelloReply helloReply) {
                System.out.println("Final Greeting: " + helloReply.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("All greetings have been sent.");
                finishLatch.countDown();
            }
        });

        try {
            for (String name : names) {
                requestObserver.onNext(Greet.HelloRequest.newBuilder().setName(name).build());
                // 模拟延时
                Thread.sleep(100);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        // 完成请求
        requestObserver.onCompleted();

        // 等待所有响应
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.err.println("greetLotsOfGreetings can not finish within 1 minutes");
        }
    }

    // 双向流式 RPC
    public void greetEveryone(String[] names) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<Greet.HelloRequest> requestObserver = asyncStub.bidiHello(new StreamObserver<Greet.HelloReply>() {
            @Override
            public void onNext(Greet.HelloReply helloReply) {
                System.out.println("Greeting: " + helloReply.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("All greetings have been received.");
                finishLatch.countDown();
            }
        });

        try {
            for (String name : names) {
                Greet.HelloRequest request = Greet.HelloRequest.newBuilder().setName(name).build();
                requestObserver.onNext(request);

                // 模拟延时
                Thread.sleep(100);
                if (finishLatch.getCount() == 0) {
                    // RPC 完成或错误发生
                    return;
                }
            }
        } catch (RuntimeException e) {
            // 取消 RPC
            requestObserver.onError(e);
            throw e;
        }
        // 完成 RPC 调用
        requestObserver.onCompleted();

        // 等待服务器结束通知（响应所有消息）
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.err.println("greetEveryone can not finish within 1 minutes");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        GreeterClient client = new GreeterClient("localhost", 50051);
        try {
            // 一元 RPC 调用
            client.greet("world");
            System.out.println("================");

            // 服务器流式响应
            client.greetLotsOfReplies("world");
            System.out.println("================");

            // 客户端流式请求
            String[] names = {"Alice", "Bob", "Charlie"};
            client.greetLotsOfGreetings(names);
            System.out.println("================");

            // 双向流式通信
            client.greetEveryone(new String[]{"David", "Eve", "Frank"});
            System.out.println("================");

        } finally {
            client.shutdown();
        }
    }
}
