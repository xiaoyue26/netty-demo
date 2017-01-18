package algo;

/**
 * Created by xiaoyue26 on 12/28/16.
 */
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.common.hash.Hashing;
public class KetamaConsistentHash<T> {
    private final int numberOfVirtualNodeReplicas;
    private final SortedMap<Long, T> circle = new TreeMap<Long, T>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public KetamaConsistentHash(int numberOfVirtualNodeReplicas, List<T> nodes) {
        this.numberOfVirtualNodeReplicas = numberOfVirtualNodeReplicas;
        add(nodes);
    }

    public synchronized void add(T node) {
        w.lock();
        try {
            addNode(node);
        } finally {
            w.unlock();
        }
    }

    public synchronized void add(List<T> nodes) {
        w.lock();
        try {
            for (T node : nodes) {
                addNode(node);
            }
        } finally {
            w.unlock();
        }
    }

    public synchronized void remove(List<T> nodes) {
        w.lock();
        try {
            for (T node : nodes) {
                removeNode(node);
            }
        } finally {
            w.unlock();
        }
    }

    public synchronized void remove(T node) {
        w.lock();
        try {
            removeNode(node);
        } finally {
            w.unlock();
        }
    }

    public T get(Object key) {
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
            return circle.get(hash);
        } finally {
            r.unlock();
        }
    }

    private void addNode(T node) {
        for (int i = 0; i < numberOfVirtualNodeReplicas / 4; i++) {
            byte[] digest = md5(node + "-" + i);
            for (int h = 0; h < 4; h++) {
                circle.put(getKetamaKey(digest, h), node);
            }
        }
    }

    private void removeNode(T node) {
        for (int i = 0; i < numberOfVirtualNodeReplicas / 4; i++) {
            byte[] digest = md5(node.toString() + "-" + i);
            for (int h = 0; h < 4; h++) {
                circle.remove(getKetamaKey(digest, h));
            }
        }
    }

    public static byte[] md5(String text) {
        return Hashing.md5().hashBytes(text.getBytes()).asBytes();
    }

    public static long[] getKetamaKeys(String text) {
        long[] pairs = new long[4];
        byte[] digest = md5(text);
        for (int h = 0; h < 4; h++) {
            pairs[h] = getKetamaKey(digest, h);
        }
        return pairs;
    }

    public static long getKetamaKey(final String k) {
        byte[] digest = md5(k);
        return getKetamaKey(digest, 0) & 0xffffffffL;
    }

    public static Long getKetamaKey(byte[] digest, int h) {
        return ((long) (digest[3 + h * 4] & 0xFF) << 24) | ((long) (digest[2 + h * 4] & 0xFF) << 16) | ((long) (digest[1 + h * 4] & 0xFF) << 8) | (digest[h * 4] & 0xFF);
    }

}