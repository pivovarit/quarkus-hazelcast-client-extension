package guides.hazelcast.quarkus;

import com.hazelcast.nio.SocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

class HelloSocketInterceptor implements SocketInterceptor {
    @Override
    public void init(Properties properties) {
        System.out.println("Initializing socket interceptor");
    }

    @Override
    public void onConnect(Socket connectedSocket) throws IOException {
        System.out.println("Connected: " + connectedSocket.toString());
    }
}
