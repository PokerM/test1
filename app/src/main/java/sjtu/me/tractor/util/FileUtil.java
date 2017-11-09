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
     * д�ļ�
     * @param albumName ����ļ�·��
     * @param fileName �ļ���
     * @param dataStrs  Ҫд�������
     * @param append   �Ƿ�׷��
     * @param timestamp   �Ƿ��¼ʱ��
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
     * Checks if external storage is available for read and write ����ⲿ�洢���Ƿ����ʹ��
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
     * ����һ���û��������ݱ���Ŀ¼
     * @param albumName �û���������Ŀ¼��
     * @return ���ɺõ�Ŀ¼
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
     * ��ȡ�ļ����������
     * @param filePath �ļ�·��
     * @param spec ������������������ spec==nullʱ������������
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
}
