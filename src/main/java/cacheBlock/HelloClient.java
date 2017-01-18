package cacheBlock;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

        // 连接到本地的8000端口的服务端
        bootstrap.connect(new InetSocketAddress(8000));

        //bootstrap.releaseExternalResources();

    }


    public static void readFileByBytes(String fileName) {
        File file = new File(fileName);
        int chunckSize=8192;
        InputStream in = null;
       /* try {//one by one
            in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                System.out.write(tempbyte);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }*/
        try {// with buffer
            byte[] tempbytes = new byte[chunckSize];
            int byteread;
            in = new FileInputStream(fileName);
            // ReadFromFile.showAvailableBytes(in);
            while ((byteread = in.read(tempbytes)) != -1) {
                //System.out.write(tempbytes, 0, byteread);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    //System.out.println("finish");
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    public static void main(String args[]) {
        HelloClient client = new HelloClient();
        //String filename="/home/xiaoyue26/ssd/data/sim_data/dt=2016-07-27/out/sim_data_0";
        //  su -
        //  sync; echo 1 > /proc/sys/vm/drop_caches

        double startTime = System.nanoTime();
        //String filename="/home/xiaoyue26/Downloads/s3cmd-master/download/bak/part-2010-01-10";
        String filename="in1";
        client.readFileByBytes(filename);
        double endTime = System.nanoTime();
        System.out.println( (endTime - startTime) / 1000000000 + "");
        // 10s
        startTime = System.nanoTime();
        FileClient.path=filename;
        FileClient.main(args);
        endTime = System.nanoTime();
        System.out.println( (endTime - startTime) / 1000000000 + "");

    }
}