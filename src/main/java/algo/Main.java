package algo;

import org.apache.hadoop.fs.DF;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by xiaoyue26 on 12/11/16.
 */
public class Main {

    private static void jdbc() {
        Date dt = new Date();
        SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");
        String dtString = matter1.format(dt);
        String before = "before";
        String after = "after";
        Connection conn = null;
        String sql;
        String url = "jdbc:mysql://xiaoyue26:3306/data?"
                + "user=root" +
                "&" +
                "password=mysql" +
                "&useUnicode=true&characterEncoding=UTF8";
        try {
            Class.forName("com.mysql.jdbc.Driver");


            DebugClient.print("driver success");
            conn = DriverManager.getConnection(url);
            sql = "insert into data.rebuild(dt,`before`,after) values ( ? , ? , ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            System.out.println(dtString);
            System.out.println(before);
            System.out.println(after);
            pstmt.setString(1, dtString);
            pstmt.setString(2, before);
            pstmt.setString(3, after);

            int result = pstmt.executeUpdate();
            System.out.println(result);
            //}
        } catch (SQLException e) {
            DebugClient.print("Mysql op error");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static void test() throws IOException {
        CHashVirtualNodeOld obj = new CHashVirtualNodeOld("yarn.nodemanager.local-dirs2");
        String[] hddDirs = {"/home/xiaoyue26/tmpfolder/tmp1"
                , "/home/xiaoyue26/tmpfolder/tmp2"
                , "/home/xiaoyue26/tmpfolder/tmp3"};
        DF[] dfs = new DF[3];
        for (int i = 0; i < 3; ++i) {
            File folder = new File(hddDirs[i]);
            if (!(folder.exists() && folder.isDirectory())) {
                folder.mkdirs();
            }
            dfs[i] = new DF(new File(hddDirs[i]), 3000);
        }
        obj.updateHdd(hddDirs, dfs);
        // ssd:
        String[] ssdDirs = {"/home/xiaoyue26/tmpfolder/ssd1"
                , "/home/xiaoyue26/tmpfolder/ssd2"
                , "/home/xiaoyue26/tmpfolder/ssd3"};
        DF[] ssdfs = new DF[ssdDirs.length];
        for (int i = 0; i < ssdDirs.length; ++i) {
            File folder = new File(ssdDirs[i]);
            if (!(folder.exists() && folder.isDirectory())) {
                folder.mkdirs();
            }
            ssdfs[i] = new DF(new File(ssdDirs[i]), 3000);
        }
        obj.updateSsd(ssdDirs, ssdfs);
        String input = "input";
        String dir = obj.getDir(input, 100);
        System.out.println(dir);
        File file = new File(dir, input);
        if (!(file.exists() && file.isDirectory())) {
            file.mkdirs();
        }
        System.out.println(obj.find(input));
    }

    public static void test1() {
        int input = 10;
        int buckets = 10;
        JumpConsistentHash.LinearCongruentialGenerator generator;
        //generator = new JumpConsistentHash.LinearCongruentialGenerator(input);
        int candidate = 0;
        int next;
        //int counter[] = new int[10];
        boolean flag = true;
        boolean counters[] = new boolean[1000];
        int num = 0;
        for (int i = 0; i < 1000; i++) {
            candidate = 0;
            generator = new JumpConsistentHash.LinearCongruentialGenerator(i);
            while (true) {
                next = (int) ((candidate + 1) / generator.nextDouble());
                if (next >= 0 && next < buckets) {
                    candidate = next;
                    //++counter[candidate];
                } else {
                    //System.out.println("output:" + candidate);
                    if (candidate == 7) {
                        //System.out.println("input:" + i);
                        counters[i] = true;
                        ++num;
                    }
                    //System.out.println("output:" + candidate);
                    break;
                    //++counter[candidate];
                }
            }
        }
        System.out.println("num:" + num);
        buckets = 9;
        num = 0;
        for (int i = 0; i < 1000; i++) {
            if (counters[i]) {
                continue;
            }
            candidate = 0;
            generator = new JumpConsistentHash.LinearCongruentialGenerator(i);
            while (true) {
                next = (int) ((candidate + 1) / generator.nextDouble());
                if (next >= 0 && next < buckets) {
                    candidate = next;
                    //++counter[candidate];
                } else {
                    //System.out.println("output:" + candidate);
                    if (candidate == 7) {
                        ++num;
                        //System.out.println("input:" + i);
                        counters[i] = true;
                    }
                    //System.out.println("output:" + candidate);
                    break;
                    //++counter[candidate];
                }
            }
        }
        System.out.println("num:" + num);
        for (int i = 0; i < 10; ++i) {
            // System.out.println(i + ":" + counter[i]);
        }

    }

    private static int inverseSum(long y, long sum[]) {
        int left = 0, right = sum.length - 1, mid;
        while (left < right) {
            mid = left + (right - left) / 2;
            if (sum[mid] < y) {
                left = mid + 1;
            } else if (sum[mid] == y) {
                return mid;
            } else {//sum[mid]>y
                right = mid;
            }
        }
        return left;
    }

    private static void test2() {
        long sum[] = {2, 6, 9, 13};
        long y;
        for (int i = 0; i < 15; i++) {
            y = i;
            System.out.println(i + ":" + sum[inverseSum(y, sum)]);
        }

    }

    public static void testCJump() {
        TestLocalDirAllocator tl = new TestLocalDirAllocator();
        tl.test();

    }

    public static void main(String[] args) {
        //jdbc();
        //test1();
        //test2();
        testCJump();

    }


}
