package cacheBlock;

import algo.DebugClient;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static cacheBlock.Command.CommandType.SET;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class ObjectServerHandler extends SimpleChannelHandler {
    String filename = "/home/xiaoyue26/Downloads/s3cmd-master" +
            "/download/bak/yahoo.log";
    FileInputStream fin;

    {
        try {
            fin = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        DebugClient.record("enter","server");
        System.out.println("hello");
        Command cmd = new Command();
        cmd.type = SET;
        cmd.fio = fin;
        ctx.getChannel().write(cmd);
        DebugClient.record("return","server");

        //ctx.sendUpstream(e);
    }

}
