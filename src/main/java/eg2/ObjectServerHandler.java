package eg2;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class ObjectServerHandler extends SimpleChannelHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ObjectServerHandler.class.getName());
    Map<String, Integer> breakNoMap = new HashMap<String, Integer>();
    Scanner scanner = new Scanner(System.in);

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Command msg = (Command) e.getMessage();
        switch (msg.type) {
            case REG:
                System.out.println("register obj:");
                System.out.println(msg.getObjID());
                Command getCmd = new Command();
                getCmd.setType(Command.CommandType.GET);
                getCmd.setObjID(msg.getObjID());
                ctx.getChannel().write(getCmd);
                break;
            case REQ:
                System.out.println("obj detail report:");
                Detail detail = msg.getDetail();
                System.out.println("objID:" + detail.getObjID());
                System.out.println("break lines:");
                int i = 1;
                for (String line : detail.getBreakLines()) {
                    System.out.println(i + ": " + line);
                    i++;
                }
                int newStop = scanner.nextInt();
                System.out.println("input complete");
                Command setCmd = new Command();
                setCmd.setType(Command.CommandType.SET);
                setCmd.setObjID(detail.getObjID());
                setCmd.setStopNo(newStop);
                ctx.getChannel().write(setCmd);
                break;
            default:
                break;
        }


    }
}
