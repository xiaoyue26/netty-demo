package cacheBlock;

import org.apache.commons.io.FileUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class HelloServer {

    private void run() {
        // Server服务启动器
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        // 设置一个处理客户端消息和各种消息事件的类(Handler)
        final ObjectServerHandler handler = new ObjectServerHandler();
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader()))
                        , handler
                        , new ObjectEncoder()
                );
            }
        });
        // 开放8000端口供客户端访问。
        bootstrap.bind(new InetSocketAddress(8000));


    }

    public static void main(String args[]) {
        HelloServer server = new HelloServer();
        server.run();
    }
}