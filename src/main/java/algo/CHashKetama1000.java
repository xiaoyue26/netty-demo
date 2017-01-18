package algo;

import com.google.common.hash.Hashing;
import org.apache.hadoop.fs.DF;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xiaoyue26 on 12/28/16.
 */
public class CHashKetama1000<T> implements DirAlgo {
    private final int numberOfVirtualNodeReplicas = 1000;
    private final SortedMap<Long, T> ssdCircle = new TreeMap<Long, T>();
    private final SortedMap<Long, T> hddCircle = new TreeMap<Long, T>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private boolean preferSsd;

    public CHashKetama1000(String contextCfgItemName) {
        if ("yarn.nodemanager.local-dirs".equals(contextCfgItemName)
                //    ) {
                || "mapreduce.cluster.local.dir".equals(contextCfgItemName)) {
            preferSsd = true;
        } else {
            preferSsd = false;
        }
    }

    @Override
    public void updateHdd(String[] h, DF[] d) {
        List<RealNode> hddNodes = new ArrayList<RealNode>(h.length);
        for (int i = 0; i < h.length; i++) {
            hddNodes.add(new RealNode(h[i], d[i]));
        }
        add((List<T>) hddNodes, hddCircle);
    }

    @Override
    public void updateSsd(String[] s, DF[] d) {
        List<RealNode> ssdNodes = new ArrayList<RealNode>(s.length);
        for (int i = 0; i < s.length; i++) {
            ssdNodes.add(new RealNode(s[i], d[i]));
        }
        add((List<T>) ssdNodes, ssdCircle);
    }

    @Override
    public String getDir(String pathStr, long size) {
        RealNode res;
        if (preferSsd) {
            res = (RealNode) get(pathStr, ssdCircle, size);
            if (res != null) {
                return res.dir;
            }
        }
        res = (RealNode) get(pathStr, hddCircle, size);
        if (res != null) {
            return res.dir;
        }
        return null;
    }

    @Override
    public Path find(String pathStr, FileSystem localFS) throws IOException {
        Path res;
        if (preferSsd) {
            res=find(ssdCircle,pathStr,localFS);
            if (res != null) {
                return res;
            }
        }
        res=find(hddCircle,pathStr,localFS);
        return res;
    }

    @Override
    public boolean ifExists(String pathStr, FileSystem localFS) throws IOException {
        Path res;
        if (preferSsd) {
            res = find(ssdCircle, pathStr, localFS);
            if (res != null) {
                return true;
            }
        }
        res = find(hddCircle, pathStr, localFS);
        if (res != null) {
            return true;
        }
        return false;
    }

    private synchronized void add(T node, SortedMap<Long, T> circle) {
        w.lock();
        try {
            addNode(node, circle);
        } finally {
            w.unlock();
        }
    }

    private synchronized void add(List<T> nodes, SortedMap<Long, T> circle) {
        w.lock();
        try {
            for (T node : nodes) {
                addNode(node, circle);
            }
        } finally {
            w.unlock();
        }
    }

    private synchronized void remove(List<T> nodes, SortedMap<Long, T> circle) {
        w.lock();
        try {
            for (T node : nodes) {
                removeNode(node, circle);
            }
        } finally {
            w.unlock();
        }
    }

    private synchronized void remove(T node, SortedMap<Long, T> circle) {
        w.lock();
        try {
            removeNode(node, circle);
        } finally {
            w.unlock();
        }
    }

    private Path find(SortedMap<Long, T> circle, String pathStr, FileSystem localFS) throws IOException {
        if (circle.isEmpty()) {
            return null;
        }
        long hash = getKetamaKey(pathStr);
        r.lock();
        try {
            if (!circle.containsKey(hash)) {
                SortedMap<Long, T> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            RealNode node = (RealNode) circle.get(hash);
            Path path = new Path(node.dir, pathStr);
            if (localFS.exists(path)) {
                return path;
            }
            Set<Long> set = circle.keySet();
            for (Long key : set) {
                node = (RealNode) circle.get(key);
                path = new Path(node.dir, pathStr);
                if (localFS.exists(path)) {
                    return path;
                }
            }
        } finally {
            r.unlock();
        }
        return null;
    }

    private T get(Object key, SortedMap<Long, T> circle, long size) {
        if (circle.isEmpty()) {
            return null;
        }
        long hash = getKetamaKey(key.toString());
        r.lock();
        try {
            if (!circle.containsKey(hash)) {
                SortedMap<Long, T> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            // compare size here
            return circle.get(hash);
        } finally {
            r.unlock();
        }
    }

    private void addNode(T node, SortedMap<Long, T> circle) {
        for (int i = 0; i < numberOfVirtualNodeReplicas / 4; i++) {
            byte[] digest = md5(node + "-" + i);
            for (int h = 0; h < 4; h++) {
                circle.put(getKetamaKey(digest, h), node);
            }
        }
    }

    private void removeNode(T node, SortedMap<Long, T> circle) {
        for (int i = 0; i < numberOfVirtualNodeReplicas / 4; i++) {
            byte[] digest = md5(node.toString() + "-" + i);
            for (int h = 0; h < 4; h++) {
                circle.remove(getKetamaKey(digest, h));
            }
        }
    }

    private static byte[] md5(String text) {
        return Hashing.md5().hashBytes(text.getBytes()).asBytes();
    }

    private static long[] getKetamaKeys(String text) {
        long[] pairs = new long[4];
        byte[] digest = md5(text);
        for (int h = 0; h < 4; h++) {
            pairs[h] = getKetamaKey(digest, h);
        }
        return pairs;
    }

    private static long getKetamaKey(final String k) {
        byte[] digest = md5(k);
        return getKetamaKey(digest, 0) & 0xffffffffL;
    }

    private static Long getKetamaKey(byte[] digest, int h) {
        return ((long) (digest[3 + h * 4] & 0xFF) << 24) | ((long) (digest[2 + h * 4] & 0xFF) << 16) | ((long) (digest[1 + h * 4] & 0xFF) << 8) | (digest[h * 4] & 0xFF);
    }
}
