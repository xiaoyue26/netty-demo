package algo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.DF;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.DiskChecker;
import org.apache.hadoop.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaoyue26 on 12/23/16.
 */
public class TestLocalDirAllocator {
    private Configuration conf = new Configuration();
    private FileSystem localFS;
    private String[] localDirs;
    private String[] ssdDirs;

    private static DF[] dirDF; // detail info of dir (command 'df -h')
    private DF[] ssdDF;
    private DirAlgo chwv;
    private String contextCfgItemName;

    public TestLocalDirAllocator() {
        try {
            localFS = FileSystem.getLocal(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        contextCfgItemName = "test";

    }

    private synchronized String printContext() {
        return "=>" + (contextCfgItemName == null)
                + (contextCfgItemName == null
                ? 0
                : contextCfgItemName.length() + ":" + contextCfgItemName)
                + "<=:\n";
    }

    private synchronized void filterDirs(String inputDir, ArrayList<String> dirs, ArrayList<DF> dfList) {
        Path tmpDir = new Path(inputDir);
        try {
            if (localFS.mkdirs(tmpDir) || localFS.exists(tmpDir)) {
                try {
                    File tmpFile = tmpDir.isAbsolute()
                            ? new File(localFS.makeQualified(tmpDir).toUri())
                            : new File(inputDir);
                    DiskChecker.checkDir(tmpFile);
                    dirs.add(tmpFile.getPath());
                    dfList.add(new DF(tmpFile, 30000));

                } catch (DiskChecker.DiskErrorException de) {
                    System.out.println(inputDir + " is not writable\n" + de.getMessage());
                }
            } else {
                System.out.println("Failed to create " + inputDir);
            }
        } catch (IOException ie) {
            System.out.println("Failed to create " + inputDir + ": " +
                    ie.getMessage() + "\n");
        }
    }

    private synchronized void configureHdd(String newLocalDirs) {
        localDirs = StringUtils.getTrimmedStrings(newLocalDirs);
        int numDirs = localDirs.length;
        ArrayList<String> dirs = new ArrayList<String>(numDirs);
        ArrayList<DF> dfList = new ArrayList<DF>(numDirs);// df -h
        for (int i = 0; i < numDirs; i++) {
            filterDirs(localDirs[i], dirs, dfList);
        }
        localDirs = dirs.toArray(new String[dirs.size()]);
        dirDF = dfList.toArray(new DF[dirs.size()]);
        chwv.updateHdd(localDirs, dirDF);
    }

    private synchronized boolean rebuildDir(int i, boolean[] ssdFlags, String conStr) {
        String realDir;
        for (int j = 0; j < localDirs.length; ++j) {
            realDir = localDirs[j];
            if (realDir.startsWith(ssdDirs[i])) {
                //DebugClient.print(conStr+ssdDirs[i]+" =>\n "+realDir);
                ssdDirs[i] = realDir;
                ssdFlags[j] = true;
                return true;
            }
        }
        //DebugClient.print(conStr+" skip: "+ssdDirs[i]);
        return false;
    }

    private synchronized void configureSsd(String newSsdDirs) {
        ssdDirs = StringUtils.getTrimmedStrings(newSsdDirs);
        int numDirs = ssdDirs.length;
        int numHdd = localDirs.length;
        boolean[] ssdFlags = new boolean[localDirs.length];
        ArrayList<String> dirs = new ArrayList<String>(numDirs);
        ArrayList<DF> dfList = new ArrayList<DF>(numDirs);// df -h
        String conStr = printContext();
        for (int i = 0; i < numDirs; i++) {
            // rebuild from localDirs========>
            if (rebuildDir(i, ssdFlags, conStr)) {
                --numHdd;
            }
            filterDirs(ssdDirs[i], dirs, dfList);
        }
        ssdDirs = dirs.toArray(new String[dirs.size()]);
        ssdDF = dfList.toArray(new DF[dirs.size()]);
        chwv.updateSsd(ssdDirs, ssdDF);
        // update hddDirs:
        updateHddDirs(numHdd, ssdFlags);
    }

    private void updateHddDirs(int numHdd, boolean[] ssdFlags) {
        try {
            String[] hddDirs = new String[numHdd];
            DF[] hddDFs = new DF[numHdd];
            for (int i = 0, k = 0; i < localDirs.length; ++i) {
                if (!ssdFlags[i]) {
                    hddDirs[k] = localDirs[i];
                    hddDFs[k] = dirDF[i];
                    ++k;
                }
            }
            chwv.updateHdd(hddDirs, hddDFs);
        } catch (Exception e) {
            DebugClient.print("update hddDirs error" + e.getMessage());
        }
    }

    private String root = "/home/xiaoyue26/testfolder/";

    private String buildFolder(int num, String postfix) {
        String[] dirs = new String[num];
        for (int i = 0; i < num; i++) {
            dirs[i] = root + postfix + i;
        }
        return StringUtils.join(",", dirs);
    }

    private String getRes(boolean fakeTime, String before, long size) {
        if (!fakeTime) {
            return chwv.getDir(before, size);
        } else {
            return "ssd1";
        }

    }

    long startTime, endTime, duringTime, fakeTime;

    private void run(Class algo, String newLocalDirs, String newSsdDirs
            , int loopTimes, int size, int ssdNum, boolean emptyRun) {
        try {
            Constructor c = algo.getConstructor(String.class);
            chwv = (DirAlgo) c.newInstance(contextCfgItemName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        configureHdd(newLocalDirs);
        configureSsd(newSsdDirs);
        String algoName = algo.getName().replace("algo.", "");
        //StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile(".*(ssd|hdd)([0-9]+)");
        long[] counters = new long[ssdNum];
        startTime = System.nanoTime();
        for (int i = 0; i < loopTimes; i++) {
            String before = "i" + i;
            String res = getRes(emptyRun, before, size);
            //String res = "ssd1000";

            Matcher m = p.matcher(res);
            if (m.matches()) {
                String afterIndex = m.group(2);
                counters[Integer.valueOf(afterIndex)]++;
                //sb.append(before + "\t" + afterIndex + "\n");
                //DebugClient.record(before+"\t"+afterIndex,algoName);
            }
        }
        endTime = System.nanoTime();
        if (!emptyRun) {
            duringTime = endTime - startTime;
        } else {
            fakeTime = endTime - startTime;
        }


        //DebugClient.flush(sb.toString(), algoName);
        double u = (double) loopTimes / ssdNum;
        double res = 0;
        double factor = loopTimes / ssdNum * 10 / 55;
        for (int i = 0; i < counters.length; i++) {
            if ("CHashJumpCapacity".equals(algoName)) {
                res += (counters[i] - factor * (i % 10 + 1)) * (counters[i] - factor * (i % 10 + 1));
            } else {
                res += (counters[i] - u) * (counters[i] - u);
            }

        }
        if (!emptyRun) {
            System.out.println(Math.pow(res, 0.5));
            System.out.println(Math.pow(res, 0.5) / u);
        }
    }

    public void test() {
        int ssdNum = 1024;
        int hddNum = 1024;
        int size = 10;
        int loopTimes = 1000 * 1000;
        Class algos[] = {CHashJump.class, CHashJumpCapacity.class, CHashKetama.class, CHashKetama100.class, CHashKetama1000.class};
        //  ,CHashJump.class, CHashJumpCapacity.class
        //  , CHashVirtualNode.class
        //  , CHashKetama.class
        // Runtime rt = Runtime.getRuntime();
        // System.out.println("Total Memory = " + rt.totalMemory() + " Used Memory = " + (rt.totalMemory() - rt.freeMemory()));

        String newSsdDirs = buildFolder(ssdNum, "ssd");
        String newLocalDirs = newSsdDirs + ","
                + buildFolder(hddNum, "hdd");
        //long startTime, endTime, duringTime, fakeTime;
        for (Class algo : algos) {
            run(algo, newLocalDirs, newSsdDirs, loopTimes, size, ssdNum, true);
            run(algo, newLocalDirs, newSsdDirs, loopTimes, size, ssdNum, false);

            System.out.println(algo.getName() + ":\n" + (duringTime - fakeTime) + "ns");
            System.out.println(algo.getName() + " duringTime:\n" + (duringTime) + "ns");
            System.out.println(algo.getName() + " avg:\n" + (duringTime - fakeTime) / loopTimes + "ns");
            System.out.println(algo.getName() + ":\n" + (duringTime - fakeTime) / 1000000000 + "s");
        }


    }

}
