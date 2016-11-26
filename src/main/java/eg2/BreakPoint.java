package eg2;

import java.io.Serializable;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class BreakPoint implements Serializable {
    private static final long serialVersionUID = 7590999461767050471L;

    private String breakLine;

    public String getObjID() {
        return objID;
    }

    public void setObjID(String objID) {
        this.objID = objID;
    }

    private String objID;

    public String getBreakLine() {
        return breakLine;
    }

    public void setBreakLine(String breakLine) {
        this.breakLine = breakLine;
    }
}
