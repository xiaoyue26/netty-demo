package cacheBlock;

import algo.DebugClient;
import org.jboss.netty.channel.*;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class ObjectClientHandler extends SimpleChannelHandler {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        System.out.println(e.getCause());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        System.out.print("hello");
        Command command = (Command) e.getMessage();
        DebugClient.record("enter\n" + System.nanoTime(), "client");

        FileInputStream fin = command.getFio();
        byte[] tempbytes = new byte[8196];
        while ((fin.read(tempbytes)) != -1) {
        }
        if (fin != null) {
            fin.close();
        }
        DebugClient.record("success\n" + System.nanoTime(), "client");


    }
}
