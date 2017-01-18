package cacheBlock;

import java.io.FileInputStream;
import java.io.Serializable;

/**
 * Created by xiaoyue26 on 11/23/16.
 */
public class Command implements Serializable {
    private static final long serialVersionUID = -7034661554614338732L;
    CommandType type;
    //Detail detail;
    FileInputStream fio;

    public FileInputStream getFio() {
        return fio;
    }

    public void setFio(FileInputStream fio) {
        this.fio = fio;
    }

    public enum CommandType {
        GET, SET, REG, REQ
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }



}
