package sjtu.me.tractor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.navigation.HistoryPath;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;


/**
 * 根据SQL语句执行数据库操作
 * SQL标准语句：
 * 1.插入:
 * sql = "insert into person(name,address,age) values(?,?,?)" bindArgs = {"张三","北京","36"}
 * 2.修改:
 * sql = "update person set name = ?,address = ?,age = ? where pid = ?" bindArgs = {"张三丰","河南","33",1}
 * 3.删除（模糊匹配）:
 * sql = "delete from person where name like ?" bindArgs = {"%三丰%"}
 * 4.删除（精确匹配）
 * sql = "delete from person where name = ?" bindArgs = {"张三丰"}
 * 5.整表删除：
 * sql = "delete from person" bindArgs = null
 * 6.查询（模糊匹配）:
 * sql = "select * from person where name like ?" bindArgs = {"张%"}
 * 7.查询（精确匹配）
 * sql = "select * from person where name = ?" bindArgs = {"张三丰"}
 * 8.整表查询：
 * sql = "select * from person" bindArgs = null
 */

/**
 * @author billhu
 *         专门管理数据库的管理器，此处封装了工具类，同时拥有多种方法对数据库进行操作
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private static final boolean D = true;

    private static MyDatabaseHelper dbHelper;
    private SQLiteDatabase mDatabase;
    public Context context;

    /*使用原子操作和单例模式来保障sqlite多线程并发访问*/
    private static AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;

    //在管理器的创建方法中新创建DBHelper
    private DatabaseManager(Context context) {
        this.context = context;
        dbHelper = MyDatabaseHelper.getInstance(context);
    }

    /**
     * 单例模式
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    /**
     * 连接数据库
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
     * 关闭数据库
     * 注意：在实时监控条件下切记不可关闭数据库！需要在LoadFinish的时候才可以释放资源。
     */
    public synchronized void releaseDataBase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            if (mDatabase != null && mDatabase.isOpen()) {
                mDatabase.close();
            }
            dbHelper.close();
        }

        if (D) {
            Log.e(TAG, "RELEASING DATABASE --> $$CLOSE DATABASE$$");
        }
    }


    /**
     * 插入数据
     *
     * @param table  执行插入数据的表名
     * @param values 键值对，用于规定插入的列名和列值，若没有放入所有列名则没有放入的列值为Null
     * @return flag 标志（对数据库产生影响时返回true）
     */
    public boolean insert(String table, ContentValues values) {
        connectDatabase();
        long count = mDatabase.insert(table, null, values);
        boolean flag = (count > 0);
        System.out.println("-->--" + flag);
        return flag;
    }

    /**
     * 查询数据
     *
     * @param table         执行查询数据的表名
     * @param columns       要查询出来的列名
     * @param whereClause   查询条件子句，允许使用占位符“?”
     * @param selectionArgs 用于为whereClause子句中占位符传入参数值
     * @param groupBy       用于控制分组
     * @param having        用于对分组进行过滤
     * @param orderBy       用于对记录进行排序
     * @return 查询结果游标
     */
    public Cursor queryCursor(String table, String[] columns, String whereClause, String[] selectionArgs,
                              String groupBy, String having, String orderBy) {
        connectDatabase();
        return mDatabase.query(table, columns, whereClause, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * 查询满足特定过滤条件的数据
     *
     * @param table         执行查询数据的表名
     * @param columns       要查询出来的列名
     * @param whereClause   查询条件子句，允许使用占位符“?”
     * @param selectionArgs 用于为whereClause子句中占位符传入参数值
     * @return 查询结果游标
     */
    public Cursor queryCursorWithoutGroup(String table, String[] columns, String whereClause, String[] selectionArgs) {
        connectDatabase();
        return mDatabase.query(table, columns, whereClause, selectionArgs, null, null, null);
    }

    /**
     * 根据名字查询数据
     *
     * @param table 执行查询数据的表名
     * @param names 名称/类型/制造商/型号/控制器
     * @return 查询结果
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
     * 查询整表的指定列
     *
     * @param table   表名
     * @param columns 查询列
     * @return 查询结果
     */
    public Cursor queryColnCursor(String table, String[] columns) {
        connectDatabase();
        return mDatabase.query(table, columns, null, null, null, null, null);
    }

    /**
     * 查询整表
     *
     * @param table 表名
     * @return 结果
     */
    public Cursor queryAllColnCursor(String table) {
        connectDatabase();
        return mDatabase.query(table, null, null, null, null, null, "name");
    }

    /**
     * MethodName: updateBySQL
     * Description:
     *
     * @param sql      SQL语句
     * @param bindArgs 参数
     * @return boolean 成功标志
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
     * 封装直接删除拖拉机整表的方法。
     * （慎用，调用前务必启用提示框）
     *
     * @return 成功标志
     */
    public boolean clearAllTractorData() {
        connectDatabase();
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, null, null);
        return (count > 0);
    }

    /**
     * 向拖拉机表中插入一行数据
     *
     * @param tractorInfo 参数信息
     * @return 成功标志
     */
    public boolean insertDataToTractor(String[] tractorInfo) {
        if (tractorInfo == null || tractorInfo.length < 14) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TractorInfo.TRACTOR_NAME, tractorInfo[0]);
        values.put(TractorInfo.TRACTOR_TYPE, tractorInfo[1]);
        values.put(TractorInfo.TRACTOR_MADE, tractorInfo[2]);
        values.put(TractorInfo.TRACTOR_TYPE_NUMBER, tractorInfo[3]);
        values.put(TractorInfo.TRACTOR_WHEELBASE, tractorInfo[4]);
        values.put(TractorInfo.TRACTOR_ANTENNA_LATERAL, tractorInfo[5]);
        values.put(TractorInfo.TRACTOR_ANTENNA_REAR, tractorInfo[6]);
        values.put(TractorInfo.TRACTOR_ANTENNA_HEIGHT, tractorInfo[7]);
        values.put(TractorInfo.TRACTOR_MIN_TURNING_RADIUS, tractorInfo[8]);
        values.put(TractorInfo.TRACTOR_ANGLE_CORRECTION, tractorInfo[9]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_WIDTH, tractorInfo[10]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_OFFSET, tractorInfo[11]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_LENGTH, tractorInfo[12]);
        values.put(TractorInfo.TRACTOR_OPERATION_LINESPACING, tractorInfo[13]);
        connectDatabase(); //连接数据库
        long count = mDatabase.insert(MyDatabaseHelper.TABLE_TRACTOR, null, values);
        return count > 0;
    }

    /**
     * 根据名称更新表
     *
     * @param tractorName 拖拉机名称
     * @param tractorInfo 参数信息
     * @return 成功标志
     */
    public boolean updateTractorByName(String tractorName, String[] tractorInfo) {
        if (tractorInfo == null || tractorInfo.length < 14) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TractorInfo.TRACTOR_NAME, tractorInfo[0]);
        values.put(TractorInfo.TRACTOR_TYPE, tractorInfo[1]);
        values.put(TractorInfo.TRACTOR_MADE, tractorInfo[2]);
        values.put(TractorInfo.TRACTOR_TYPE_NUMBER, tractorInfo[3]);
        values.put(TractorInfo.TRACTOR_WHEELBASE, tractorInfo[4]);
        values.put(TractorInfo.TRACTOR_ANTENNA_LATERAL, tractorInfo[5]);
        values.put(TractorInfo.TRACTOR_ANTENNA_REAR, tractorInfo[6]);
        values.put(TractorInfo.TRACTOR_ANTENNA_HEIGHT, tractorInfo[7]);
        values.put(TractorInfo.TRACTOR_MIN_TURNING_RADIUS, tractorInfo[8]);
        values.put(TractorInfo.TRACTOR_ANGLE_CORRECTION, tractorInfo[9]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_WIDTH, tractorInfo[10]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_OFFSET, tractorInfo[11]);
        values.put(TractorInfo.TRACTOR_IMPLEMENT_LENGTH, tractorInfo[12]);
        values.put(TractorInfo.TRACTOR_OPERATION_LINESPACING, tractorInfo[13]);
        connectDatabase(); //连接数据库
        String[] names = {tractorName};
        int count = mDatabase.update(MyDatabaseHelper.TABLE_TRACTOR, values, TractorInfo.TRACTOR_NAME + " = ?", names);
        return (count > 0);
    }

    /**
     * 根据名称删除数据
     * （不允许多辆车辆使用同一个名称）
     *
     * @param name 待删除拖拉机名称
     * @return 标志
     */
    public boolean deleteTractorByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, TractorInfo.TRACTOR_NAME + " = ?", names);
        return (count > 0);
    }

    /**
     * 根据名称查询车辆参数信息
     *
     * @param queryName 查询名称
     * @return 结果
     */
    public Cursor queryTractorByName(String queryName) {
        connectDatabase();
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(TractorInfo.TRACTOR_NAME).append(", ")
                .append(TractorInfo.TRACTOR_TYPE).append(", ")
                .append(TractorInfo.TRACTOR_MADE).append(", ")
                .append(TractorInfo.TRACTOR_TYPE_NUMBER).append(", ")
                .append(TractorInfo.TRACTOR_WHEELBASE).append(", ")
                .append(TractorInfo.TRACTOR_ANTENNA_LATERAL).append(", ")
                .append(TractorInfo.TRACTOR_ANTENNA_REAR).append(", ")
                .append(TractorInfo.TRACTOR_ANTENNA_HEIGHT).append(", ")
                .append(TractorInfo.TRACTOR_MIN_TURNING_RADIUS).append(", ")
                .append(TractorInfo.TRACTOR_ANGLE_CORRECTION).append(", ")
                .append(TractorInfo.TRACTOR_IMPLEMENT_WIDTH).append(", ")
                .append(TractorInfo.TRACTOR_IMPLEMENT_OFFSET).append(", ")
                .append(TractorInfo.TRACTOR_IMPLEMENT_LENGTH).append(", ")
                .append(TractorInfo.TRACTOR_OPERATION_LINESPACING)
                .append(" from ").append(MyDatabaseHelper.TABLE_TRACTOR)
                .append(" where ").append(TractorInfo.TRACTOR_NAME).append(" like ?")
                .toString(), new String[]{queryName});
        return cursor;
    }

    /**
     * 获取所有车辆名称
     *
     * @return
     */
    public Cursor getTractorsNameSet() {
        connectDatabase();
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(TractorInfo.TRACTOR_NAME)
                .append(" from ").append(MyDatabaseHelper.TABLE_TRACTOR)
                .append(" where ").append(TractorInfo.TRACTOR_NAME).append(" like ?")
                .toString(), new String[]{"%%"});
        return cursor;
    }


    /**
     * 清空地块整表数据的方法。
     * （慎用，调用前务必启用提示框）
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
     * 向地块表中插入条目
     *
     * @param fieldInfo 地块信息
     * @return 成功标志
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
     * 根据名字查询地块
     * (仅查询地块名称，不查询地块顶点）
     *
     * @param queryName
     * @return 结果游标
     */
    public Cursor queryFieldByName(String queryName) {
        connectDatabase();
        //SQL查询语句，先建立一个分表，然后从分表中根据名称查询
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
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
     * 获取所有地块名称
     *
     * @return 结果游标
     */
    public Cursor getFieldsNameSet() {
        connectDatabase();
        //SQL查询语句，先建立一个分表，然后从分表中根据名称查询
        Cursor cursor = mDatabase.rawQuery(new StringBuilder()
                .append("select ")
                .append(FieldInfo.FIELD_NAME)
                .append(" from ").append(MyDatabaseHelper.TABLE_FIELD)
                .append(" as a where ")
                .append(FieldInfo.FIELD_POINT_NO)
                .append("=(select max(b.").append(FieldInfo.FIELD_POINT_NO).append(") from ")
                .append(MyDatabaseHelper.TABLE_FIELD)
                .append(" as b where a.").append(FieldInfo.FIELD_NAME)
                .append(" = b.").append(FieldInfo.FIELD_NAME).append(") and ")
                .append(FieldInfo.FIELD_NAME).append(" like ?").toString(), new String[]{"%%"});
        return cursor;
    }

    /**
     * 根据名字查询地块
     * （返回值中包含满足查询的地块的所有顶点数据）
     *
     * @param queryName 名称
     * @return 结果
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
     * 根据名称删除地块
     * （不允许两个地块名称相同）
     *
     * @param name 删除地块名称
     * @return 成功标志
     */
    public boolean deleteFieldByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_FIELD, FieldInfo.FIELD_NAME + " = ?", names);
        return (count > 0);
    }

    /**
     * 向AB线表中插入条目
     *
     * @param date      日期
     * @param line      AB线
     * @param fieldName 地块标志
     * @return 成功标志
     */
    public boolean insertABline(String date, ABLine line, String fieldName) {
        if (line == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ABLine.AB_LINE_NAME_BY_DATE, date);
        values.put(ABLine.A_POINT_X_COORDINATE, line.getAX());
        values.put(ABLine.A_POINT_Y_COORDINATE, line.getAY());
        values.put(ABLine.B_POINT_X_COORDINATE, line.getBX());
        values.put(ABLine.B_POINT_Y_COORDINATE, line.getBY());
        values.put(ABLine.FIELD_NAME, fieldName);
        connectDatabase();
        long count = mDatabase.insert(MyDatabaseHelper.TABLE_AB_LINE, null, values);
        return count > 0;
    }

    /**
     * @param nameByDate 以时间命名的AB线
     * @return 成功标志
     */
    public boolean deleteABLine(String nameByDate) {
        connectDatabase();
        String[] args = {nameByDate};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_AB_LINE, ABLine.AB_LINE_NAME_BY_DATE + " = ?", args);
        return (count > 0);
    }

    /**
     * @param date 日期
     * @return 结果游标
     */
    public Cursor queryABlineByDate(String date) {
        connectDatabase();
        return mDatabase.rawQuery(new StringBuilder()
                .append("select * from ").append(MyDatabaseHelper.TABLE_AB_LINE)
                .append(" where ").append(ABLine.AB_LINE_NAME_BY_DATE)
                .append(" like ?").toString(), new String[]{date});
    }

    /**
     * 获取所有AB线
     *
     * @return 结果游标
     */
    public Cursor getAllABlines() {
        connectDatabase();
        return mDatabase.rawQuery(new StringBuilder()
                .append("select * from ").append(MyDatabaseHelper.TABLE_AB_LINE)
                .append(" where ").append(ABLine.AB_LINE_NAME_BY_DATE)
                .append(" like ?").toString(), new String[]{"%%"});
    }

    /**
     * @param fileName  历史记录文件名
     * @param fieldName 地块名
     * @return 成功标志
     */
    public boolean insertHistoryEntry(String fileName, String fieldName) {
        if (TextUtils.isEmpty(fieldName) || TextUtils.isEmpty(fieldName)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(HistoryPath.HISTORY_RECORD_FILE_NAME, fileName);
        values.put(HistoryPath.FIELD_NAME, fieldName);
        connectDatabase();
        long count = mDatabase.insert(MyDatabaseHelper.TABLE_HISTORY, null, values);
        return count > 0;
    }

    /**
     * @param fileName 历史记录文件名
     * @return 结果游标
     */
    public Cursor queryHistoryEntries(String fileName) {
        connectDatabase();
        return mDatabase.rawQuery(new StringBuilder()
                .append("select * from ").append(MyDatabaseHelper.TABLE_HISTORY)
                .append(" where ").append(HistoryPath.HISTORY_RECORD_FILE_NAME)
                .append(" like ?").toString(), new String[]{fileName});
    }

    /**
     * @param fileName 历史记录文件名
     * @return 成功标志
     */
    public boolean deleteHistoryEntries(String fileName) {
        connectDatabase();
        String[] args = {fileName};
        int count = mDatabase.delete(MyDatabaseHelper.TABLE_HISTORY, HistoryPath.HISTORY_RECORD_FILE_NAME + " = ?", args);
        return (count > 0);
    }

    /**
     * 遍历Cursor的内容转存到Map里面
     *
     * @param cursor 游标
     * @return Map表
     */
    public static Map<String, String> cursorToMap(Cursor cursor) {
        Map<String, String> map = new HashMap<String, String>();
        if (cursor == null) {
            return map;
        }

        if (cursor.moveToFirst()) {
            //只取记录表的第一行（假设记录不重名）
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
     * 遍历Cursor的内容转存到ArrayList里面
     *
     * @param cursor 游标
     * @return 列表
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

        /*cursor的初始位置为第0条数据，第一次调用cursor.moveToNext()，相当于cursor.moveToFirst()*/
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
     * 遍历Cursor的内容转存到ArrayList里面，并添加列表条目序号
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
            // 增加条目序号
            map.put("listNumber", Integer.toString(listNumber));
            list.add(map);
        }
        return list;
    }

}
