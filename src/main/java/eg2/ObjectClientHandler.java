package eg2;

import org.jboss.netty.channel.*;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.util.*;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class ObjectClientHandler extends SimpleChannelHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ObjectClientHandler.class.getName());
    public boolean debuggerEnabled = true;
    private Map<String, Detail> hashmap = new HashMap<String, Detail>();
    public List<String> objIDs = new ArrayList<String>();

    /**
     * 当绑定到服务端的时候触发，给服务端发消息。
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // 向服务端发送Object信息 register this obj
        registerAllObjs(e.getChannel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.info("maybe the server isn't listening");
        debuggerEnabled = false;
        System.out.println(e.getCause());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Command command = (Command) e.getMessage();
        String objID = command.getObjID();
        Command.CommandType type = command.getType();
        switch (type) {
            case SET:
                logger.info("SET received");
                hashmap.get(objID).stopNo = command.getStopNo();
                break;
            case GET:
                logger.info("GET received");
                break;
            default:
                break;
        }
        Detail detail = hashmap.get(objID);
        Command response = new Command();
        response.type = Command.CommandType.REQ;
        response.detail = detail;
        ctx.getChannel().write(response);
    }

    /**
     * 发送Object
     *
     * @param channel
     */
    private void registerAllObjs(Channel channel) {
        Command reg = new Command();
        for (String objID : objIDs) {
            reg.type = Command.CommandType.REG;
            reg.setObjID(objID);
            channel.write(reg);
        }
    }

    public boolean stopHere(String objID, int breakNo) {
        Detail detail = hashmap.get(objID);
        if (detail == null) {
            return false;
        }
        if (detail.stopNo > breakNo) {
            return false;
        }
        // detail.stopNo <= breakNo
        return true;
    }

    public int addBreakPoint(String objID, String line) {
        Detail detail = hashmap.get(objID);
        if (detail == null) {
            detail = new Detail();
            detail.objID = objID;
            hashmap.put(objID, detail);
        }
        detail.breakLines.add(line);
        return detail.breakLines.size();
    }


}
