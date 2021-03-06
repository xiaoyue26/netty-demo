package eg2;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

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
        try {
            int[] a =new int[1];
            a[2]=2;

        }catch (Exception e ){
            System.out.print("---"+e.toString()+"--");

        }
        String []ssdDirs={"a","b"};
        String []localDirs={"c","d","e"};
        String[] allDirs=new String[ssdDirs.length+localDirs.length];
        System.arraycopy(ssdDirs, 0, allDirs, 0, ssdDirs.length);
        System.arraycopy(localDirs, 0, allDirs, ssdDirs.length, localDirs.length);
        for(String a:allDirs){
            System.out.println(a);
        }

        HelloServer server = new HelloServer();
        try {
            File log = new File("/home/xiaoyue26/hello");
            String data = FileUtils.readFileToString(log);
            data += "log\n";
            FileUtils.write(log, data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        server.run();
    }
}