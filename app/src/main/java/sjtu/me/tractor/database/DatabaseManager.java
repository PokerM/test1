package sjtu.me.tractor.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
 *
 * @param sql
 * @param bindArgs
 * @return
 */

/**
 * @author billhu
 * ר�Ź������ݿ�Ĺ��������˴���װ�˹����࣬ͬʱӵ�ж��ַ��������ݿ���в���
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private static final boolean D = true;

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase myDatabase;
    public Context context;

    //�ڹ������Ĵ����������´���DBHelper
    public DatabaseManager(Context context) {
        this.context = context;
        dbHelper = new MyDatabaseHelper(context);
    }

    /**
     * �������ݿ�
     */
    public void connectDatabase() {
        myDatabase = dbHelper.getReadableDatabase();

        if (D) {
            Log.e(TAG, "CONNECTING DATABASE --> $$GET READABLE DATABASE$$");
        }
    }

    /**
     * �ر����ݿ�
     * ע�⣺��ʵʱ����������мǲ��ɹر����ݿ⣡��Ҫ��LoadFinish��ʱ��ſ����ͷ���Դ��
     */
    public void releaseDataBase() {
        if (myDatabase != null && myDatabase.isOpen()) {
            myDatabase.close();
        }
        dbHelper.close();
    }


    /**
     * ��������
     * @param table ִ�в������ݵı���
     * @param values ��ֵ�ԣ����ڹ涨�������������ֵ����û�з�������������û�з������ֵΪNull
     * @return flag ��־�������ݿ����Ӱ��ʱ����true��
     */
    public boolean insert(String table, ContentValues values) {
        connectDatabase();
        boolean flag = false;
        long count = myDatabase.insert(table, null, values);
        flag = (count > 0 ? true : false);
        System.out.println("-->--" + flag);
        return flag;
    }

    /**
     * ��ѯ����
     * @param table ִ�в�ѯ���ݵı���
     * @param columns Ҫ��ѯ����������
     * @param whereClause ��ѯ�����Ӿ䣬����ʹ��ռλ����?��
     * @param selectionArgs ����ΪwhereClause�Ӿ���ռλ���������ֵ
     * @param groupBy ���ڿ��Ʒ���
     * @param having ���ڶԷ�����й���
     * @param orderBy ���ڶԼ�¼��������
     * @return ��ѯ����α�
     */
    public Cursor queryCursor(String table, String[] columns, String whereClause, String[] selectionArgs,
                              String groupBy, String having, String orderBy) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, columns, whereClause, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    /**
     * ��ѯ�����ض���������������
     * @param table ִ�в�ѯ���ݵı���
     * @param columns Ҫ��ѯ����������
     * @param whereClause ��ѯ�����Ӿ䣬����ʹ��ռλ����?��
     * @param selectionArgs ����ΪwhereClause�Ӿ���ռλ���������ֵ
     * @return ��ѯ����α�
     */
    public Cursor queryCursorWithoutGroup(String table, String[] columns, String whereClause, String[] selectionArgs) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, columns, whereClause, selectionArgs, null, null, null);
        return cursor;
    }

    /**
     * �������ֲ�ѯ����
     * @param table ִ�в�ѯ���ݵı���
     * @param names ����/����/������/�ͺ�/������
     * @return ��ѯ���
     */
    public Map<String, String> queryByName(String table, String[] names) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, null, "name = ?", names, null, null, null);
        Map<String, String> map = new HashMap<String, String>();
        map = cursorToMap(cursor);
        return map;
    }

    /**
     * ��ѯ�����ָ����
     * @param table
     * @param columns
     * @return
     */
    public Cursor queryColnCursor(String table, String[] columns) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, columns, null, null, null, null, null);
        return cursor;
    }

    /**
     * ��ѯ����
     * @param table
     * @return
     */
    public Cursor queryAllColnCursor(String table) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, null, null, null, null, null, "name");
        return cursor;
    }


    /**
     * �������Ƹ��±�
     * @param values ����ֵ
     * @param tractorName ����������
     * @return
     */
    public boolean updateTractorByName(String tractorName, ContentValues values) {
        connectDatabase();
        boolean flag = false;
        String[] names = {tractorName};
        int count = myDatabase.update(MyDatabaseHelper.TABLE_TRACTOR, values, "name = ?", names);
        flag = (count > 0 ? true : false);
        return flag;
    }


    public boolean updateBySQL(String sql, Object[] bindArgs) {
        connectDatabase();
        boolean flag = false;
        try {
            myDatabase.execSQL(sql, bindArgs);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * ��װֱ��ɾ������������ķ�����
     * �����ã�����ǰ���������ʾ��
     * @return
     */
    public boolean clearAllTractorData() {
        connectDatabase();
        boolean flag = false;
        int count = myDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, null, null);
        flag = (count > 0 ? true : false);
        return flag;
    }

    /**
     * �����������в���һ������
     * @param tName ����
     * @param tType ����
     * @param tMade ������
     * @param tTypeNumber �ͺ�
     * @param wheelbase ���
     * @param antennaLateral ���ߺ���ƫ��
     * @param antennaRear ���ߵ�������ƫ��
     * @param antennaHeight ���߰�װ�߶�
     * @param minTurning ��С����뾶
     * @param angleCorrection �Ƕ�У��ֵ
     * @param implementWidth ũ�߷���
     * @param implementOffset ũ�߰�װƫ��
     * @param implementLength ũ�߳���
     * @param lineSpacing ��ҵ�м��
     */
    public void insertDataToTractor(String tName, String tType, String tMade, String tTypeNumber, String wheelbase, String antennaLateral, String antennaRear,
                                    String antennaHeight, String minTurning, String angleCorrection, String implementWidth, String implementOffset,
                                    String implementLength, String lineSpacing) {
        connectDatabase();
        myDatabase.execSQL("insert into " + MyDatabaseHelper.TABLE_TRACTOR + " values(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{tName, tType, tMade, tTypeNumber, wheelbase, antennaLateral, antennaRear, antennaHeight, minTurning, angleCorrection
                        , implementWidth, implementOffset, implementLength, lineSpacing});
    }

    /**
     * ��������ɾ������
     * ���������������ʹ��ͬһ�����ƣ�
     * @param name ��ɾ������������
     * @return ��־
     */
    public boolean deleteTractorByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = myDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, "name = ?", names);
        return count > 0;
    }

    /**
     * �������Ʋ�ѯ����������Ϣ
     * @param queryName ��ѯ����
     * @return
     */
    public Cursor queryTractorByName(String queryName) {
        connectDatabase();
        Cursor cursor;
        cursor = myDatabase.rawQuery("select "
                        + TractorInfo.T_NAME + ", "
                        + TractorInfo.T_WHEELBASE + ", "
                        + TractorInfo.T_ANTENNA_LATERAL + ", "
                        + TractorInfo.T_ANTENNA_REAR + ", "
                        + TractorInfo.T_ANTENNA_HEIGHT + ", "
                        + TractorInfo.T_MIN_TURNING_RADIUS + ", "
                        + TractorInfo.T_ANGLE_CORRECTION + ", "
                        + TractorInfo.T_IMPLEMENT_WIDTH + ", "
                        + TractorInfo.T_IMPLEMENT_OFFSET + ", "
                        + TractorInfo.T_IMPLEMENT_LENGTH + ", "
                        + TractorInfo.T_OPERATION_LINESPACING
                        + " from " + MyDatabaseHelper.TABLE_TRACTOR + " where "
                        + TractorInfo.T_NAME + " like ?",
                new String[]{queryName});
        return cursor;
    }


    /**
     * ��յؿ��������ݵķ�����
     * �����ã�����ǰ���������ʾ��
     * @return
     */
    public boolean clearAllFieldData() {
        connectDatabase();
        boolean flag = false;
        int count = myDatabase.delete(MyDatabaseHelper.TABLE_FIELD, null, null);
        flag = (count > 0 ? true : false);
        return flag;
    }

    /**
     * ��ؿ���в�����Ŀ
     * @param fNo
     * @param fName
     * @param fDate
     * @param fPNo
     * @param fPX
     * @param fPY
     */
    public void insertDataToField(String fNo, String fName, String fDate, String fPNo, String fPLat, String fPLng, String fPX, String fPY) {
        connectDatabase();
        myDatabase.execSQL("insert into " + MyDatabaseHelper.TABLE_FIELD + " values(null, ?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{fNo, fName, fDate, fPNo, fPLat, fPLng, fPX, fPY});
    }

    /**
     * �������ֲ�ѯ�ؿ�
     * (����ѯ�ؿ����ƣ�����ѯ�ؿ鶥�㣩
     * @param queryName
     * @return
     */
    public Cursor queryFieldByName(String queryName) {
        connectDatabase();
        Cursor cursor = null;
        //SQL��ѯ��䣬�Ƚ���һ���ֱ�Ȼ��ӷֱ��и������Ʋ�ѯ
        cursor = myDatabase.rawQuery("select "
                + FieldInfo.FIELD_ID + ", "
                + FieldInfo.FIELD_NAME + ", "
                + FieldInfo.FIELD_DATE + ", "
                + FieldInfo.FIELD_POINT_NO + " from "
                + MyDatabaseHelper.TABLE_FIELD + " as a "
                + "where " + FieldInfo.FIELD_POINT_NO
                + "=(select max(b." + FieldInfo.FIELD_POINT_NO + ") from "
                + MyDatabaseHelper.TABLE_FIELD + " as b where a."
                + FieldInfo.FIELD_NAME + " = b." + FieldInfo.FIELD_NAME
                + ") and " + FieldInfo.FIELD_NAME + " like ?", new String[]{"%" + queryName + "%"});
        return cursor;
    }

    /**
     * �������ֲ�ѯ�ؿ�
     * ������ֵ�а��������ѯ�ĵؿ�����ж������ݣ�
     * @param queryName
     * @return
     */
    public Cursor queryFieldWithPointsByName(String queryName) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.rawQuery("select "
                + FieldInfo.FIELD_ID + ", "
                + FieldInfo.FIELD_NAME + ", "
                + FieldInfo.FIELD_DATE + ", "
                + FieldInfo.FIELD_POINT_NO + ", "
                + FieldInfo.FIELD_POINT_X_COORDINATE + ", "
                + FieldInfo.FIELD_POINT_Y_COORDINATE + " from "
                + MyDatabaseHelper.TABLE_FIELD + " where "
                + FieldInfo.FIELD_NAME + " like ?", new String[]{queryName});
        return cursor;
    }

    /**
     * ��������ɾ���ؿ�
     * �������������ؿ�������ͬ��
     * @param name ɾ���ؿ�����
     * @return
     */
    public boolean deleteFieldByName(String name) {
        connectDatabase();
        boolean flag = false;
        String[] names = {name};
        int count = myDatabase.delete(MyDatabaseHelper.TABLE_FIELD, FieldInfo.FIELD_NAME + " = ?", names);
        flag = (count > 0 ? true : false);
        return flag;
    }

    /**
     * ����Cursor������ת�浽Map����
     * @param cursor
     * @return
     */
    public static Map<String, String> cursorToMap(Cursor cursor) {
        Map<String, String> map = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            int columnNum = cursor.getColumnCount();
            for (int i = 0; i < columnNum; i++) {
                String columnName = cursor.getColumnName(i);
                String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
                if (columnValue == null) {
                    columnValue = "";
                }
                map.put(columnName, columnValue);
            }
        }
        return map;
    }

    /**
     * ����Cursor������ת�浽ArrayList����
     * @param cursor
     * @return
     */
    public static ArrayList<Map<String, String>> cursorToList(Cursor cursor) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();

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
     * @param cursor
     * @return
     */
    public static ArrayList<Map<String, String>> cursorToListAddListNumber(Cursor cursor) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
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
