package eg2;

import java.io.Serializable;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class Command implements Serializable {
    private static final long serialVersionUID = -7034661554614338732L;
    CommandType type;
    String objID;
    int stopNo;
    Detail detail;

    public enum CommandType {
        GET, SET, REG, REQ
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

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
}
