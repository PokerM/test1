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
 *
 * @param sql
 * @param bindArgs
 * @return
 */

/**
 * @author billhu
 * 专门管理数据库的管理器，此处封装了工具类，同时拥有多种方法对数据库进行操作
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private static final boolean D = true;

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase myDatabase;
    public Context context;

    //在管理器的创建方法中新创建DBHelper
    public DatabaseManager(Context context) {
        this.context = context;
        dbHelper = new MyDatabaseHelper(context);
    }

    /**
     * 连接数据库
     */
    public void connectDatabase() {
        myDatabase = dbHelper.getReadableDatabase();

        if (D) {
            Log.e(TAG, "CONNECTING DATABASE --> $$GET READABLE DATABASE$$");
        }
    }

    /**
     * 关闭数据库
     * 注意：在实时监控条件下切记不可关闭数据库！需要在LoadFinish的时候才可以释放资源。
     */
    public void releaseDataBase() {
        if (myDatabase != null && myDatabase.isOpen()) {
            myDatabase.close();
        }
        dbHelper.close();
    }


    /**
     * 插入数据
     * @param table 执行插入数据的表名
     * @param values 键值对，用于规定插入的列名和列值，若没有放入所有列名则没有放入的列值为Null
     * @return flag 标志（对数据库产生影响时返回true）
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
     * 查询数据
     * @param table 执行查询数据的表名
     * @param columns 要查询出来的列名
     * @param whereClause 查询条件子句，允许使用占位符“?”
     * @param selectionArgs 用于为whereClause子句中占位符传入参数值
     * @param groupBy 用于控制分组
     * @param having 用于对分组进行过滤
     * @param orderBy 用于对记录进行排序
     * @return 查询结果游标
     */
    public Cursor queryCursor(String table, String[] columns, String whereClause, String[] selectionArgs,
                              String groupBy, String having, String orderBy) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, columns, whereClause, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    /**
     * 查询满足特定过滤条件的数据
     * @param table 执行查询数据的表名
     * @param columns 要查询出来的列名
     * @param whereClause 查询条件子句，允许使用占位符“?”
     * @param selectionArgs 用于为whereClause子句中占位符传入参数值
     * @return 查询结果游标
     */
    public Cursor queryCursorWithoutGroup(String table, String[] columns, String whereClause, String[] selectionArgs) {
        connectDatabase();
        Cursor cursor = null;
        cursor = myDatabase.query(table, columns, whereClause, selectionArgs, null, null, null);
        return cursor;
    }

    /**
     * 根据名字查询数据
     * @param table 执行查询数据的表名
     * @param names 名称/类型/制造商/型号/控制器
     * @return 查询结果
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
     * 查询整表的指定列
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
     * 查询整表
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
     * 根据名称更新表
     * @param values 属性值
     * @param tractorName 拖拉机名称
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
     * 封装直接删除拖拉机整表的方法。
     * （慎用，调用前务必启用提示框）
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
     * 向拖拉机表中插入一行数据
     * @param tName 名称
     * @param tType 类型
     * @param tMade 制造商
     * @param tTypeNumber 型号
     * @param wheelbase 轴距
     * @param antennaLateral 天线横向偏差
     * @param antennaRear 天线到后轮轴偏差
     * @param antennaHeight 天线安装高度
     * @param minTurning 最小拐弯半径
     * @param angleCorrection 角度校正值
     * @param implementWidth 农具幅宽
     * @param implementOffset 农具安装偏移
     * @param implementLength 农具长度
     * @param lineSpacing 作业行间距
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
     * 根据名称删除数据
     * （不允许多辆车辆使用同一个名称）
     * @param name 待删除拖拉机名称
     * @return 标志
     */
    public boolean deleteTractorByName(String name) {
        connectDatabase();
        String[] names = {name};
        int count = myDatabase.delete(MyDatabaseHelper.TABLE_TRACTOR, "name = ?", names);
        return count > 0;
    }

    /**
     * 根据名称查询车辆参数信息
     * @param queryName 查询名称
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
     * 清空地块整表数据的方法。
     * （慎用，调用前务必启用提示框）
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
     * 向地块表中插入条目
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
     * 根据名字查询地块
     * (仅查询地块名称，不查询地块顶点）
     * @param queryName
     * @return
     */
    public Cursor queryFieldByName(String queryName) {
        connectDatabase();
        Cursor cursor = null;
        //SQL查询语句，先建立一个分表，然后从分表中根据名称查询
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
     * 根据名字查询地块
     * （返回值中包含满足查询的地块的所有顶点数据）
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
     * 根据名称删除地块
     * （不允许两个地块名称相同）
     * @param name 删除地块名称
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
     * 遍历Cursor的内容转存到Map里面
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
     * 遍历Cursor的内容转存到ArrayList里面
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
     * 遍历Cursor的内容转存到ArrayList里面，并添加列表条目序号
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
            // 增加条目序号
            map.put("listNumber", Integer.toString(listNumber));
            list.add(map);
        }
        return list;
    }

}
