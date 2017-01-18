package algo;

import org.apache.hadoop.fs.DF;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Created by xiaoyue26 on 12/15/16.
 */
public class CHashJump implements DirAlgo {

    String hddDirs[];
    DF[] hddDFs;
    String ssdDirs[];
    DF[] ssdDFs;
    boolean preferSsd;

    public CHashJump(String contextCfgItemName) {
        if ("yarn.nodemanager.local-dirs".equals(contextCfgItemName)
                //    ) {
                || "mapreduce.cluster.local.dir".equals(contextCfgItemName)) {
            preferSsd = true;
        } else {
            preferSsd = false;
        }
        if ("test".equals(contextCfgItemName)) {
            preferSsd = true;
        }
    }

    private static long getHash(String str) {
        // change hash function here:
        return GeneralHashFunctionLibrary.APHash(str);
    }

    @Override
    public void updateHdd(String[] h, DF[] d) {
        hddDirs = h;
        hddDFs = d;
    }

    @Override
    public void updateSsd(String[] s, DF[] d) {
        ssdDirs = s;
        ssdDFs = d;
    }
    private String getDir(long input, long size, String[] dirs, DF[] dfs) {
        int index = JumpConsistentHash.consistentHash(input, dirs.length);
        for (int i = 0; i < dirs.length; ++i) {
            if (dfs[index].getAvailable() > size) {
                //DebugClient.rebuild(pathStr, dirs[index]);
                //System.out.println(dirs[index]+" : "+dfs[index].getAvailable());
                return dirs[index];
            }
            index = (index + 1) % dirs.length;
        }
        return null;
    }
    @Override
    public String getDir(String pathStr, long size) {
        long input = getHash(pathStr);
        String res;
        if (preferSsd) {
            res = getDir(input, size, ssdDirs, ssdDFs);
            if (res != null) {
                return res;
            }
        }
        return getDir(input, size, hddDirs, hddDFs);

    }
    private Path find(long input, String pathStr, FileSystem localFS, String[] dirs) throws IOException {
        int index = JumpConsistentHash.consistentHash(input, dirs.length);
        for (int i = 0; i < dirs.length; ++i) {
            Path path = new Path(dirs[index], pathStr);
            if (localFS.exists(path)) {
                return path;
            }
            index = (index + 1) % dirs.length;
        }
        DebugClient.print("didn't find" + pathStr);
        return null;
    }
    @Override
    public Path find(String pathStr, FileSystem localFS) throws IOException {
        long input = getHash(pathStr);
        Path res;
        if (preferSsd) {
            res = find(input, pathStr, localFS, ssdDirs);
            if (res != null) {
                return res;
            }
        }
        return find(input, pathStr, localFS, hddDirs);
    }

    @Override
    public boolean ifExists(String pathStr, FileSystem localFS) throws IOException {
        long input = getHash(pathStr);
        Path res;
        if (preferSsd) {
            res = find(input, pathStr, localFS, ssdDirs);
            if (res != null) {
                return true;
            }
        }
        res = find(input, pathStr, localFS, hddDirs);
        if (res != null) {
            return true;
        }
        DebugClient.print("no exists " + pathStr);
        return false;
    }
}
