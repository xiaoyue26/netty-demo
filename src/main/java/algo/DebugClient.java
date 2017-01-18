package algo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiaoyue26 on 12/8/16.
 */
public class DebugClient {
    private static int lineno = 0;
    private static String dtString;

    static {
        Date dt = new Date();
        SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");
        dtString = matter1.format(dt);
    }

    public synchronized static void print(String line) {
        String data = lineno + ":" + line + "\n\n";
        System.out.println(data);
        lineno++;
    }

    public synchronized static void flush(String record, String logname) {
        try {
            File log = new File("/home/xiaoyue26/" + logname + dtString);
            if (!log.exists()) {
                log.createNewFile();
            }
            String data = FileUtils.readFileToString(log);
            data +=  record;
            FileUtils.write(log, data);
            lineno++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void record(String line, String logname) {
        try {
            File log = new File("/home/xiaoyue26/" + logname + dtString);
            if (!log.exists()) {
                log.createNewFile();
            }
            String data = FileUtils.readFileToString(log);
            data +=  line + "\n";
            FileUtils.write(log, data);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getExceptionInfo(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    public static String getArrayString(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String cur : array) {
            sb.append(cur + ",");
        }
        return sb.toString();
    }

}
