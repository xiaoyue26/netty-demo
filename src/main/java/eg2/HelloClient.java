package eg2;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class HelloClient {
    public void run() {
        System.out.println(this.toString());
        // Client服务启动器
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // 设置一个处理服务端消息和各种消息事件的类(Handler)
        final ObjectClientHandler handler = new ObjectClientHandler();
        //register this obj to debugger server
        DebugClient debugClient = new DebugClient(handler, this.toString());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader()))
                        , handler
                        ,new ObjectEncoder()
                );
            }
        });

        // 连接到本地的8000端口的服务端
        bootstrap.connect(new InetSocketAddress("127.0.0.1", 8000));

        System.out.println("line 1");
        debugClient.check("line 1");

        System.out.println("line 2");
        debugClient.check("line 2");

        System.out.println("line 3");
        debugClient.check("line 3");

        bootstrap.releaseExternalResources();

    }

    public static void main(String args[]) {
        HelloClient client = new HelloClient();
        client.run();
    }
}