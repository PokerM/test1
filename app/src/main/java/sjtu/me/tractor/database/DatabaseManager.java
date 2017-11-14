package sjtu.me.tractor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.tractorinfo.TractorInfo;


/**
 * ����SQL���ִ�����ݿ����
 * SQL��׼��䣺
 * 1.����:
 * sql = "insert into person(name,address,age) values(?,?,?)" bindArgs = {"����","����","36"}
 * 2.�޸�:
 * sql = "update person set name = ?,address = ?,age = ? where pid = ?" bindArgs = {"������","����","33",1}
 * 3.ɾ����ģ��ƥ�䣩:
 * sql = "delete from person where name like ?" bindArgs = {"%����%"}
 * 4.ɾ������ȷƥ�䣩
 * sql = "delete from person where name = ?" bindArgs = {"������"}
 * 5.����ɾ����
 * sql = "delete from person" bindArgs = null
 * 6.��ѯ��ģ��ƥ�䣩:
 * sql = "select * from person where name like ?" bindArgs = {"��%"}
 * 7.��ѯ����ȷƥ�䣩
 * sql = "select * from person where name = ?" bindArgs = {"������"}
 * 8.�����ѯ��
 * sql = "select * from person" bindArgs = null
 */

/**
 * @author billhu
 *         ר�Ź������ݿ�Ĺ��������˴���װ�˹����࣬ͬʱӵ�ж��ַ��������ݿ���в���
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private static final boolean D = true;

    private static MyDatabaseHelper dbHelper;
    private SQLiteDatabase mDatabase;
    public Context context;

    /*ʹ��ԭ�Ӳ����͵���ģʽ������sqlite���̲߳�������*/
    private static AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;

    //�ڹ������Ĵ����������´���DBHelper
    public DatabaseManager(Context context) {
        this.context = context;
        dbHelper = new MyDatabaseHelper(context);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    /**
     * �������ݿ�
     */
    private synchronized void connectDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            mDatabase = dbHelper.getReadableDatabase();
        }

        if (D) {
            Log.e(TAG, "CONNECTING DATABASE --> $$GET READABLE DATABASE$$");
        }
    }

    /**
     * �ر����ݿ�
     * ע�⣺��ʵʱ����������мǲ��ɹر����ݿ⣡��Ҫ��LoadFinish��ʱ��ſ����ͷ���Դ��
     */
    public synchronized void releaseDataBase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            if (mDatabase != null && mDatabase.isOpen()) {
                mDatabase.close();
            }
        }
        dbHelper.close();
    }


    /**
     * ��������
     *
     * @param table  ִ�в������ݵı���
     * @param values ��ֵ�ԣ����ڹ涨�������������ֵ����û�з�������������û�з������ֵΪNull
     * @return flag ��־�������ݿ����Ӱ��ʱ����true��
     */
    public boolean insert(String table, ContentValues values) {
        connectDatabase();
        long count = mDatabase.insert(table, null, values);
        boolean flag = (count > 0);
        System.out.println("-->--" + flag);
        return flag;
    }

    /**
     * ��ѯ����
     *
     * @param table         ִ�в�ѯ���ݵı���
     * @param columns       Ҫ��ѯ����������
     * @param whereClause   ��ѯ�����Ӿ䣬����ʹ��ռλ����?��
     * @param selectionArgs ����ΪwhereClause�Ӿ���ռλ���������ֵ
     * @param groupBy       ���ڿ��Ʒ���
     * @param having        ���ڶԷ�����й���
     * @param orderBy       ���ڶԼ�¼��������
     * @return ��ѯ����α�
     */
    public Cursor queryCursor(String table, String[] columns, String whereClause, String[] selectionArgs,
                              String groupBy, String having, String orderBy) {
        connectDatabase();
        return mDatabase.query(table, columns, whereClause, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * ��ѯ�����ض���������������
     *
     * @param table         ִ�в�ѯ���ݵı���
     * @param columns       Ҫ��ѯ����������
     * @param whereClause   ��ѯ�����Ӿ䣬����ʹ��ռλ����?��
     * @param selectionArgs ����ΪwhereClause�Ӿ���ռλ���������ֵ
     * @return ��ѯ����α�
     */
    public Cursor queryCursorWithoutGroup(String table, String[] columns, String whereClause, String[] selectionArgs) {
        connectDatabase();
        return mDatabase.query(table, columns, whereClause, selectionArgs, null, null, null);
    }

    /**
     * �������ֲ�ѯ����
     *
     * @param table ִ�в�ѯ���ݵı���
     * @param names ����/����/������/�ͺ�/������
     * @return ��ѯ���
     */
    public Map<String, String> queryByName(String table, String[] names) {
        connectDatabase();
        Cursor cursor;
        cursor = mDatabase.query(table, null, "name = ?", names, null, null, null);
        Map<String, String> map;
        map = cursorToMap(cursor);
        return map;
    }

    /**
     * ��ѯ�����ָ����
     *
     * @param table ����
     * @param columns ��ѯ��
     * @return ��ѯ���
     */
    public Cursor queryColnCursor(String table, String[] columns) {
        connectDatabase();
        return mDatabase.query(table, columns, null, null, null, null, null);
    }

    /**
     * ��ѯ����
     *
     * @param table ����
     * @return ���
     */
    public Cursor queryAllColnCursor(String table) {
        connectDatabase();
        return mDatabase.query(table, null, null, null, null, null, "name");
    }

    /**
     * MethodName: updateBySQL
     * Description:
     *
     * @param sql SQL���
     * @param bindArgs ����
     * @return boolean �ɹ���־
     */
    public boolean updateBySQL(String sql, Object[] bindArgs) {
        connectDatabase();
        boolean flag = false;
        try {
            mDatabase.execSQL(sql, bindArgs);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * ��װֱ��ɾ������������ķ�����
     * �����ã�����ǰ���������ʾ��
     *
     * @return �ɹ���־
     */
    public boolean clearAllTractorData() {
        connectDatabase();
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, null, null);
        boolean flag = (count > 0);
        return flag;
    }

    /**
     * �����������в���һ������
     *
     * @param tractorInfo ������Ϣ
     * @return �ɹ���־
     */
    public boolean insertDataToTractor(String[] tractorInfo) {
        if (tractorInfo == null || tractorInfo.length < 14) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TractorInfo.T_NAME, tractorInfo[0]);
        values.put(TractorInfo.T_TYPE, tractorInfo[1]);
        values.put(TractorInfo.T_MADE, tractorInfo[2]);
        values.put(TractorInfo.T_TYPE_NUMBER, tractorInfo[3]);
        values.put(TractorInfo.T_WHEELBASE, tractorInfo[4]);
        values.put(TractorInfo.T_ANTENNA_LATERAL, tractorInfo[5]);
        values.put(TractorInfo.T_ANTENNA_REAR, tractorInfo[6]);
        values.put(TractorInfo.T_ANTENNA_HEIGHT, tractorInfo[7]);
        values.put(TractorInfo.T_ANGLE_CORRECTION, tractorInfo[8]);
        values.put(TractorInfo.T_MIN_TURNING_RADIUS, tractorInfo[9]);
        values.put(TractorInfo.T_IMPLEMENT_WIDTH, tractorInfo[10]);
        values.put(TractorInfo.T_IMPLEMENT_OFFSET, tractorInfo[11]);
        values.put(TractorInfo.T_IMPLEMENT_LENGTH, tractorInfo[12]);
        values.put(TractorInfo.T_OPERATION_LINESPACING, tractorInfo[13]);
        connectDatabase(); //�������ݿ�
        long count = mDatabase.insert(MyDatabaseHelper.TABLE_TRACTOR, null, values);
        return count > 0;
    }

    /**
     * �������Ƹ��±�
     *
     * @param tractorName ����������
     * @param tractorInfo ������Ϣ
     * @return �ɹ���־
     */
    public boolean updateTractorByName(String tractorName, String[] tractorInfo) {
        if (tractorInfo == null || tractorInfo.length < 14) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TractorInfo.T_NAME, tractorInfo[0]);
        values.put(TractorInfo.T_TYPE, tractorInfo[1]);
        values.put(TractorInfo.T_MADE, tractorInfo[2]);
        values.put(TractorInfo.T_TYPE_NUMBER, tractorInfo[3]);
        values.put(TractorInfo.T_WHEELBASE, tractorInfo[4]);
        values.put(TractorInfo.T_ANTENNA_LATERAL, tractorInfo[5]);
        values.put(TractorInfo.T_ANTENNA_REAR, tractorInfo[6]);
        values.put(TractorInfo.T_ANTENNA_HEIGHT, tractorInfo[7]);
        values.put(TractorInfo.T_ANGLE_CORRECTION, tractorInfo[8]);
        values.put(TractorInfo.T_MIN_TURNING_RADIUS, tractorInfo[9]);
        values.put(TractorInfo.T_IMPLEMENT_WIDTH, tractorInfo[10]);
        values.put(TractorInfo.T_IMPLEMENT_OFFSET, tractorInfo[11]);
        values.put(TractorInfo.T_IMPLEMENT_LENGTH, tractorInfo[12]);
        values.put(TractorInfo.T_OPERATION_LINESPACING, tractorInfo[13]);
        connectDatabase(); //�������ݿ�
        String[] names = {tractorName};
        int count = mDatabase.update(MyDatabaseHelper.TABLE_TRACTOR, values, "name = ?", names);
        return (count > 0);
    }

    /**
     * ��������ɾ������
     * ���������������ʹ��ͬһ�����ƣ�
     *
     * @param name ��ɾ������������
     * @return ��־
     */
    public boolean deleteTractorByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, TractorInfo.T_NAME + " = ?", names);
        return (count > 0);
    }

    /**
     * �������Ʋ�ѯ����������Ϣ
     *
     * @param queryName ��ѯ����
     * @return ���
     */
    public Cursor queryTractorByName(String queryName) {
        connectDatabase();
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(TractorInfo.T_NAME).append(", ")
                .append(TractorInfo.T_TYPE).append(", ")
                .append(TractorInfo.T_MADE).append(", ")
                .append(TractorInfo.T_TYPE_NUMBER).append(", ")
                .append(TractorInfo.T_WHEELBASE).append(", ")
                .append(TractorInfo.T_ANTENNA_LATERAL).append(", ")
                .append(TractorInfo.T_ANTENNA_REAR).append(", ")
                .append(TractorInfo.T_ANTENNA_HEIGHT).append(", ")
                .append(TractorInfo.T_MIN_TURNING_RADIUS).append(", ")
                .append(TractorInfo.T_ANGLE_CORRECTION).append(", ")
                .append(TractorInfo.T_IMPLEMENT_WIDTH).append(", ")
                .append(TractorInfo.T_IMPLEMENT_OFFSET).append(", ")
                .append(TractorInfo.T_IMPLEMENT_LENGTH).append(", ")
                .append(TractorInfo.T_OPERATION_LINESPACING)
                .append(" from ").append(MyDatabaseHelper.TABLE_TRACTOR)
                .append(" where ").append(TractorInfo.T_NAME).append(" like ?")
                .toString(), new String[]{queryName});
        return cursor;
    }


    /**
     * ��յؿ��������ݵķ�����
     * �����ã�����ǰ���������ʾ��
     *
     * @return
     */
    public boolean clearAllFieldData() {
        connectDatabase();
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_FIELD, null, null);
        boolean flag = count > 0;
        return flag;
    }

    /**
     * ��ؿ���в�����Ŀ
     *
     * @param fieldInfo �ؿ���Ϣ
     * @return �ɹ���־
     */
    public boolean insertDataToField(String[] fieldInfo) {
        if (fieldInfo == null || fieldInfo.length < 8) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(FieldInfo.FIELD_ID, fieldInfo[0]);
        values.put(FieldInfo.FIELD_NAME, fieldInfo[1]);
        values.put(FieldInfo.FIELD_DATE, fieldInfo[2]);
        values.put(FieldInfo.FIELD_POINT_NO, fieldInfo[3]);
        values.put(FieldInfo.FIELD_POINT_LATITUDE, fieldInfo[4]);
        values.put(FieldInfo.FIELD_POINT_LONGITUDE, fieldInfo[5]);
        values.put(FieldInfo.FIELD_POINT_X_COORDINATE, fieldInfo[6]);
        values.put(FieldInfo.FIELD_POINT_Y_COORDINATE, fieldInfo[7]);
        connectDatabase();
        long count = mDatabase.insert(MyDatabaseHelper.TABLE_FIELD, null, values);
        boolean flag = count > 0;
        return flag;
    }

    /**
     * �������ֲ�ѯ�ؿ�
     * (����ѯ�ؿ����ƣ�����ѯ�ؿ鶥�㣩
     *
     * @param queryName
     * @return ����α�
     */
    public Cursor queryFieldByName(String queryName) {
        connectDatabase();
        Cursor cursor = null;
        //SQL��ѯ��䣬�Ƚ���һ���ֱ�Ȼ��ӷֱ��и������Ʋ�ѯ
        cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(FieldInfo.FIELD_ID).append(", ")
                .append(FieldInfo.FIELD_NAME).append(", ")
                .append(FieldInfo.FIELD_DATE).append(", ")
                .append(FieldInfo.FIELD_POINT_NO)
                .append(" from ").append(MyDatabaseHelper.TABLE_FIELD)
                .append(" as a where ")
                .append(FieldInfo.FIELD_POINT_NO)
                .append("=(select max(b.").append(FieldInfo.FIELD_POINT_NO).append(") from ")
                .append(MyDatabaseHelper.TABLE_FIELD)
                .append(" as b where a.").append(FieldInfo.FIELD_NAME)
                .append(" = b.").append(FieldInfo.FIELD_NAME).append(") and ")
                .append(FieldInfo.FIELD_NAME).append(" like ?").toString(), new String[]{queryName});
        return cursor;
    }

    /**
     * �������ֲ�ѯ�ؿ�
     * ������ֵ�а��������ѯ�ĵؿ�����ж������ݣ�
     *
     * @param queryName ����
     * @return ���
     */
    public Cursor queryFieldWithPointsByName(String queryName) {
        connectDatabase();
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(FieldInfo.FIELD_ID).append(", ")
                .append(FieldInfo.FIELD_NAME).append(", ")
                .append(FieldInfo.FIELD_DATE).append(", ")
                .append(FieldInfo.FIELD_POINT_NO).append(", ")
                .append(FieldInfo.FIELD_POINT_X_COORDINATE).append(", ")
                .append(FieldInfo.FIELD_POINT_Y_COORDINATE)
                .append(" from ").append(MyDatabaseHelper.TABLE_FIELD)
                .append(" where ").append(FieldInfo.FIELD_NAME)
                .append(" like ?")
                .toString(), new String[]{queryName});
        return cursor;
    }

    /**
     * ��������ɾ���ؿ�
     * �������������ؿ�������ͬ��
     *
     * @param name ɾ���ؿ�����
     * @return �ɹ���־
     */
    public boolean deleteFieldByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_FIELD, FieldInfo.FIELD_NAME + " = ?", names);
        boolean flag = (count > 0);
        return flag;
    }

    /**
     * ����Cursor������ת�浽Map����
     *
     * @param cursor �α�
     * @return Map��
     */
    public static Map<String, String> cursorToMap(Cursor cursor) {
        Map<String, String> map = new HashMap<String, String>();
        cursor.moveToFirst(); //ֻȡ��¼��ĵ�һ�У������¼��������
        int columnNum = cursor.getColumnCount();
        for (int i = 0; i < columnNum; i++) {
            String columnName = cursor.getColumnName(i);
            String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
            if (columnValue == null) {
                columnValue = "";
            }
            map.put(columnName, columnValue);
        }
        return map;
    }

    /**
     * ����Cursor������ת�浽ArrayList����
     *
     * @param cursor �α�
     * @return �б�
     */
    public static ArrayList<Map<String, String>> cursorToList(Cursor cursor) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (cursor == null) {
            return list;
        }

//	      cursor.moveToFirst();
//	      do{
//	          int columnNum = cursor.getColumnCount();
//            Map<String, String> map = new HashMap<String, String>();
//            for (int i = 0; i < columnNum; i++) {
//                String cols_name = cursor.getColumnName(i);
//                String cols_values = cursor.getString(cursor.getColumnIndex(cols_name));
//                if (cols_values == null) {
//                    cols_values = "";
//                }
//                map.put(cols_name, cols_values);
//            }
//            list.add(map);
//	      }while(cursor.moveToNext());

        /*cursor�ĳ�ʼλ��Ϊ��0�����ݣ���һ�ε���cursor.moveToNext()���൱��cursor.moveToFirst()*/
        while (cursor.moveToNext()) {
            int columnNum = cursor.getColumnCount();
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < columnNum; i++) {
                String columnName = cursor.getColumnName(i);
                String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
                if (columnValue == null) {
                    columnValue = "";
                }
                map.put(columnName, columnValue);
            }
            list.add(map);
        }
        return list;
    }

    /**
     * ����Cursor������ת�浽ArrayList���棬������б���Ŀ���
     *
     * @param cursor
     * @return
     */
    public static ArrayList<Map<String, String>> cursorToListAddListNumber(Cursor cursor) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (cursor == null) {
            return list;
        }
        int listNumber = 0;
        while (cursor.moveToNext()) {
            listNumber++;
            int columnNum = cursor.getColumnCount();
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < columnNum; i++) {
                String columnName = cursor.getColumnName(i);
                String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
                if (columnValue == null) {
                    columnValue = "";
                }
                map.put(columnName, columnValue);
            }
            // ������Ŀ���
            map.put("listNumber", Integer.toString(listNumber));
            list.add(map);
        }
        return list;
    }

}
