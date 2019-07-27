package developer.aulia.jasalesprivat.queue.channelTransmission;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class ChannelSingleton {
    private final static ConnectionFactory factory = new ConnectionFactory();
    private static Connection connection = null;
    private static Channel channel = null;

    private ChannelSingleton(){
    }

    //Called when the server is booted
    public static void setUpConnection() throws IOException, TimeoutException {
        connection = factory.newConnection();
    }

    //Called when the server shuts down
    public static void tearDownConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    public static Channel getInstance() throws IOException, TimeoutException {
        if (channel==null||!channel.isOpen()){
            if (connection==null||!connection.isOpen()){
                setUpConnection();
            }
            channel = connection.createChannel();
        }
        return channel;
    }
}
