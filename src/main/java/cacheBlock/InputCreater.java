package cacheBlock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by xiaoyue26 on 1/12/17.
 */
public class InputCreater {
    static int FILE_SIZE = 102400000;
    static String FILENAME = "in6";

    public static void CreateBigFile() {


        byte[] memFile = new byte[FILE_SIZE];
        for (int i = 0; i < FILE_SIZE; i++) {
            memFile[i] = (byte) (i % 20);
        }

        RandomAccessFile randomFile;
        try {
            randomFile = new RandomAccessFile(FILENAME, "rw");


            for (int i = 0; i < 100; i++) {
                long fileLength = randomFile.length();
                randomFile.seek(fileLength);
                randomFile.write(memFile);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CreateBigFile();
    }
}
