package sjtu.me.tractor.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public final class FileUtil {
    public static final String TAG = "FileUitl";
    private static final String ROOT_DIRECTORY = "AutoTractor";

    /**
     * Write data into external strorage.
     * д�ļ�
     *
     * @param albumName ����ļ�·��
     * @param fileName  �ļ���
     * @param dataStrs  Ҫд�������
     * @param append    �Ƿ�׷��
     * @param timestamp �Ƿ��¼ʱ��
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
            Log.e(TAG, "�ⲿ�洢�����ã�");
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
     * ����ⲿ�洢���Ƿ����ʹ��
     *
     * @return �ⲿ�洢�Ƿ���ñ�־
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
     * ����һ���û��������ݱ���Ŀ¼
     *
     * @param albumName �û���������Ŀ¼��
     * @return ���ɺõ�Ŀ¼
     */
    private static File getAlbumStorageDir(String albumName) {
        String dir = new StringBuilder()
                .append(ROOT_DIRECTORY)
                .append(File.separator)
                .append(albumName).toString();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * ��ȡ�ļ����������
     *
     * @param filePath �ļ�·��
     * @param spec     ������������������ spec==nullʱ������������
     * @return
     * @author
     * @since 2016-3-1
     */
    public static String[] readFileFromExternalStorage(final String filePath, final Integer spec) {
        File file = new File(filePath);
        // ���ļ������ڻ��߲��ɶ�ʱ
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

    /**
     * �������ݿ��ļ����ⲿ�洢�ռ�
     *
     * @param dbPath ���ݿ��ļ�·��
     * @return
     */
    @SuppressLint("SdCardPath")
    public static boolean copyDbFilesToExternalStorage(String dbPath) {
//	    String dbPath = "/data/data/com.example.fielddatabase/databases/" +"auto_tractor";
        String dir = new StringBuilder()
                .append(ROOT_DIRECTORY)
                .append(File.separator)
                .append("dbFiles").toString();
        File newPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), dir);
        if (!newPath.exists()) {
            newPath.mkdirs();
        }
        return copyFile(dbPath, newPath + "/" + "auto_tractor.db");
    }

    public static boolean copyFile(String source, String dest) {
        try {
            File f1 = new File(source);
            File f2 = new File(dest);
            InputStream in = new FileInputStream(f1);
            OutputStream out = new FileOutputStream(f2);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            in.close();
            out.close();
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

}
