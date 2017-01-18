package cacheBlock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class Detail implements Serializable {
    private static final long serialVersionUID = -245407453007732528L;
    public String objID;
    public int stopNo = 0;
    public List<String> breakLines = new ArrayList<String>();

    public String getObjID() {
        return objID;
    }

    public void setObjID(String objID) {
        this.objID = objID;
    }

    public int getStopNo() {
        return stopNo;
    }

    public void setStopNo(int stopNo) {
        this.stopNo = stopNo;
    }

    public List<String> getBreakLines() {
        return breakLines;
    }

    public void setBreakLines(List<String> breakLines) {
        this.breakLines = breakLines;
    }


}
