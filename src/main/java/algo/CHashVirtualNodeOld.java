package algo;

import com.sun.tools.javac.jvm.Gen;
import org.apache.hadoop.fs.DF;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalDirAllocator;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by xiaoyue26 on 12/4/16.
 */
public class CHashVirtualNodeOld implements  DirAlgo{

    //FNV1_32_HASH
    public static int FNV1_32_HASH(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // for negative
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    private static long getHash(String str){
        return GeneralHashFunctionLibrary.APHash(str);
    }
    private final int VIRTUAL_HDDS = 5;
    private final int VIRTUAL_SSDS = 5;

    SortedMap<Long, RealNode> virtualHdds;
    SortedMap<Long, RealNode> virtualSsds;
    private RealNode[] hddNodes;
    private RealNode[] ssdNodes;

    private boolean preferSsd;

    public CHashVirtualNodeOld(String contextCfgItemName) {
        if ("yarn.nodemanager.local-dirs".equals(contextCfgItemName)
                //    ) {
                || "mapreduce.cluster.local.dir".equals(contextCfgItemName)) {
            preferSsd = true;
        } else {
            preferSsd = false;
        }
    }

    public void updateHdd(String[] hddDirs, DF[] hddDFs) {
        hddNodes = new RealNode[hddDirs.length];
        virtualHdds = new TreeMap();
        for (int i = 0; i < hddDirs.length; i++) {
            RealNode cur = new RealNode(hddDirs[i], hddDFs[i]);
            for (int j = 0; j < VIRTUAL_HDDS; j++) {
                String virtualNodeName = hddDirs[i] + "&&VN" + String.valueOf(j);
                Long dirHash = getHash(virtualNodeName);
                virtualHdds.put(dirHash, cur);
            }
        }
    }

    public void updateSsd(String[] ssdDirs, DF[] ssdDFs) {
        ssdNodes = new RealNode[ssdDirs.length];
        virtualSsds = new TreeMap();
        for (int i = 0; i < ssdDirs.length; i++) {
            RealNode cur = new RealNode(ssdDirs[i], ssdDFs[i]);
            for (int j = 0; j < VIRTUAL_SSDS; j++) {
                String virtualNodeName = ssdDirs[i] + "&&VN" + String.valueOf(j);
                Long dirHash = getHash(virtualNodeName);
                virtualSsds.put(dirHash, cur);
            }
        }
    }

    private String getDir(String pathStr, long size, SortedMap<Long, RealNode> virtualNodes) {
        Long hash = getHash(pathStr);
        SortedMap<Long, RealNode> subMap;
        RealNode realNode;
        int count = 0;
        subMap = virtualNodes.tailMap(hash);
        if (subMap.size() != 0) {
            subMap = virtualNodes;
        }
        Set<Long> set = subMap.keySet();
        for (Long key : set) {
            realNode = subMap.get(key);
            if (realNode.df.getCapacity() > size) {
                return realNode.dir;
            } else {
                count++;
            }
        }
        if (count < virtualNodes.size()) {
            set = virtualNodes.keySet();
            for (Long key : set) {
                if (count >= virtualNodes.size()) {
                    break;
                }
                realNode = virtualNodes.get(key);
                if (realNode.df.getCapacity() > size) {
                    return realNode.dir;
                } else {
                    count++;
                }
            }
        }
        return null;
    }

    public String getDir(String pathStr, long size) {
        String res;
        if(preferSsd){
        if (ssdNodes != null) { // prefer ssd
            res = getDir(pathStr, size, virtualSsds);
            if (res != null) {
                return res;
            }
        }
        }
        if (hddNodes != null) {
            res = getDir(pathStr, size, virtualHdds);
            return res;
        } else {//error
            throw new RuntimeException("CHashVirtualNodeOld hddNodes null");
        }

    }

    @Override
    public Path find(String pathStr, FileSystem localFS) throws IOException {
        return null;
    }

    private File find(String pathStr, SortedMap<Long, RealNode> virtualNodes) throws IOException {
        Long hash = getHash(pathStr);
        SortedMap<Long, RealNode> subMap;
        RealNode realNode;
        int count = 0;
        subMap = virtualNodes.tailMap(hash);
        if (subMap.size() != 0) {
            subMap = virtualNodes;
        }
        Set<Long> set = subMap.keySet();
        for (Long key : set) {
            realNode = subMap.get(key);
            File file=new File(realNode.dir, pathStr);
            //Path res = new Path(realNode.dir, pathStr);
            if (file.exists()) {
                return file;
            } else {
                count++;
            }
        }
        if (count < virtualNodes.size()) {
            set = virtualNodes.keySet();
            for (Long key : set) {
                if (count >= virtualNodes.size()) {
                    break;
                }
                realNode = virtualNodes.get(key);
                File file=new File(realNode.dir, pathStr);
                //Path res = new Path(realNode.dir, pathStr);
                if (file.exists()) {
                    return file;
                } else {
                    count++;
                }
            }
        }
        return null;
    }

    public File find(String pathStr) throws IOException {
        File res;
        if (preferSsd) {
            if (ssdNodes != null) {
                res = find(pathStr, virtualSsds);
                if (res != null) {
                    return res;
                }
            }
        }
        if (hddNodes != null) {
            res = find(pathStr, virtualHdds);
            return res;
        }
        return null;
    }

    public boolean ifExists(String pathStr, FileSystem localFS) throws IOException {
        File res;
        if (preferSsd) {
            if (ssdNodes != null) {
                res = find(pathStr, virtualSsds);
                if (res != null) {
                    return true;
                }
            }
        }
        if (hddNodes != null) {
            res = find(pathStr, virtualHdds);
            if (res != null) {
                return true;
            }
        }
        return false;
    }

    /*//  @deprecated
    private static String getDir(String pathStr, String[] localDirs) {
        // init
        SortedMap<Integer, String> virtualNodes = new TreeMap();

        for (String str : localDirs) {
            for (int i = 0; i < VIRTUAL_HDDS; i++) {
                String virtualNodeName = str + "&&VN" + String.valueOf(i);
                int dirHash = getHash(virtualNodeName);
                virtualNodes.put(dirHash, virtualNodeName);
            }
        }
        // locate pathStr
        int hash = getHash(pathStr);
        SortedMap<Integer, String> subMap =
                virtualNodes.tailMap(hash);
        Integer i = subMap.firstKey();
        String virtualNode = subMap.get(i);

        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }*/
    private String getDir(String pathStr, SortedMap<Long, RealNode> virtualNodes) {
        long hash = getHash(pathStr);
        SortedMap<Long, RealNode> subMap = virtualNodes.tailMap(hash);
        if (subMap.size() != 0) {
            Long i = subMap.firstKey();
            RealNode realNode = subMap.get(i);
            return realNode.dir;
        } else {
            return virtualNodes.get(virtualNodes.firstKey()).dir;
        }
    }

    // size unknown
    public String getDir(String pathStr) {
        if (ssdNodes != null) {
            return getDir(pathStr, virtualSsds);
        } else if (hddNodes != null) {
            return getDir(pathStr, virtualHdds);
        } else {//error
            DebugClient.print("ConsistentHashing null");
            return null;
        }
    }
}
