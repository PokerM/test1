package sjtu.me.tractor.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

public final class FileUtil {
    public static final String TAG = "FileUitl";

    /**
     * Write data into external strorage.
     * 写文件
     * @param albumName 输出文件路径
     * @param fileName 文件名
     * @param dataStrs  要写入的内容
     * @param append   是否追加
     * @param timestamp   是否记录时间
     * @return
     * @since 2016-3-1
     */
    public static void writeDataToExternalStorage(String albumName, String fileName, String dataStrs, boolean append,
            boolean timestamp) {
        // create a new file objection
        File dataFile = null;
        if (isExternalStorageWritable()) {
            File albumDirectory = getAlbumStorageDir(albumName);
            dataFile = new File(albumDirectory, fileName);
        } else {
            Log.e(TAG, "外部存储不可用！");
        }
        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            FileWriter mFileWriter = new FileWriter(dataFile, append);
            BufferedWriter mBufferedWriter = new BufferedWriter(mFileWriter);
            if (timestamp) {
                String mTimeStamp = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS")).format(new java.util.Date());
                mBufferedWriter.write(dataStrs + "," + mTimeStamp + "\r\n");
            } else {
                mBufferedWriter.write(dataStrs + "\r\n");
            }
            mBufferedWriter.close();
        } catch (IOException e) {
            Log.e("FILE", "DataFileWriter Error!");
        }
    }

    /**
     * Checks if external storage is available for read and write 检查外部存储器是否可以使用
     * 
     * @return
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Get a file object using getExternalStoragePublicDirectory()
     * 生成一个用户程序数据保存目录
     * @param albumName 用户程序数据目录名
     * @return 生成好的目录
     */
    private static File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), albumName);
        // File file = new File(Environment.getExternalStorageDirectory(),
        // albumName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 读取文件并按行输出
     * @param filePath 文件路径
     * @param spec 允许解析的最大行数， spec==null时，解析所有行
     * @return
     * @author
     * @since 2016-3-1
     */
    public static String[] readFileFromExternalStorage(final String filePath, final Integer spec) {
        File file = new File(filePath);
        // 当文件不存在或者不可读时
        if ((!isFileExists(file)) || (!file.canRead())) {
            System.out.println("file [" + filePath + "] is not exist or cannot read!!!");
            return null;
        }

        List<String> lines = new LinkedList<String>();
        BufferedReader br = null;
        FileReader fb = null;
        try {
            fb = new FileReader(file);
            br = new BufferedReader(fb);

            String str = null;
            int index = 0;
            while (((spec == null) || index++ < spec) && (str = br.readLine()) != null) {
                lines.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(br);
            closeQuietly(fb);
        }

        return lines.toArray(new String[lines.size()]);
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
        }
    }

    private static boolean isFileExists(final File file) {
        if (file.exists() && file.isFile()) {
            return true;
        }

        return false;
    }
}
