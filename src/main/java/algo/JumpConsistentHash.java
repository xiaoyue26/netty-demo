package algo;
/**
 * Jump Consistent Hash implementation.
 */
public final class JumpConsistentHash {

    /**
     * Assigns to {@code input} a "bucket" in the range {@code [0, buckets)}, in a uniform
     * manner that minimizes the need for remapping as {@code buckets} grows. That is,
     * {@code consistentHash(h, n)} equals:
     *
     * <ul>
     * <li>{@code n - 1}, with approximate probability {@code 1/n}
     * <li>{@code consistentHash(h, n - 1)}, otherwise (probability {@code 1 - 1/n})
     * </ul>
     *
     * <p>See the <a href="http://en.wikipedia.org/wiki/Consistent_hashing">wikipedia
     * article on consistent hashing</a> for more information.
     */
    public static int consistentHash(long input, int buckets) {
        if (buckets <= 0) {
            throw new IllegalArgumentException("buckets must be positive");
        }
        LinearCongruentialGenerator generator = new LinearCongruentialGenerator(input);
        int candidate = 0;
        int next;

        while (true) {
            next = (int) ((candidate + 1) / generator.nextDouble());
            if (next >=0 && next < buckets) {
                candidate = next;
            } else {
                return candidate;
            }
        }
    }

    private static int inverseSum(long y, long sum[]) {
        int left = 0, right = sum.length - 1, mid;
        while (left < right) {
            mid = left + (right - left) / 2;
            if (sum[mid] <= y) {
                left = mid + 1;
            } else if (sum[mid] == y) {
                return mid;
            } else {//sum[mid]>y
                right = mid;
            }
        }
        return left;
    }
    //  DebugClient.print("next:"+next);
    public static int consistentHash(long input, int buckets, long sum[]) {
        if (buckets <= 0) {
            throw new IllegalArgumentException("buckets must be positive");
        }
        LinearCongruentialGenerator generator = new LinearCongruentialGenerator(input);
        int candidate = 0;
        int next;
        while (true) {
            next = inverseSum((long) (sum[candidate] / generator.nextDouble()), sum);
            if (next >= 0 && next < buckets) {
                candidate = next;
            } else {
                return candidate;
            }
        }
    }
    /**
     * Linear CongruentialGenerator to use for consistent hashing.
     * See http://en.wikipedia.org/wiki/Linear_congruential_generator
     */
    public static final class LinearCongruentialGenerator {
        private long state;
        public LinearCongruentialGenerator(long seed) {
            this.state = seed;
        }
        public double nextDouble() {
            state = 2862933555777941757L * state + 1;
            return ((double) ((int) (state >>> 33) + 1)) / (0x1.0p31);
        }
    }
}