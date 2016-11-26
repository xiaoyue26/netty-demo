package eg2;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class DebugClient {
    int breakNo;
    String objID;
    ObjectClientHandler handler;

    public DebugClient(ObjectClientHandler handler, String objID) {
        this.handler = handler;
        this.objID = objID;
        handler.objIDs.add(this.toString());
    }

    public void check(String line) {
        breakNo = handler.addBreakPoint(this.toString(), line);
        while (handler.debuggerEnabled && handler.stopHere(this.toString(), breakNo)) {
        }
    }
}
