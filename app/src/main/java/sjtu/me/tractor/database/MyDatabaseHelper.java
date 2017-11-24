package sjtu.me.tractor.database;

//DBHelper，用于创建和更新数据库

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.navigation.HistoryPath;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // 直接用静态final变量设置数据库的版本和名称，并直接让构造方法仅传入参数上下文
    public static final String DB_NAME = "auto_tractor";
    private static final int DB_VERSION = 1;
    private static MyDatabaseHelper instance;

    // 定义地块数据库
    static final String TABLE_FIELD = "field";   //地块表名

    // 定义生成地块数据表SQL语句
    private static final String CREATE_FIELD_TABLE =
            "create table if not exists " + TABLE_FIELD
                    + " (_id integer primary key autoincrement, "
                    + FieldInfo.FIELD_ID + " integer, "
                    + FieldInfo.FIELD_NAME + " text, "
                    + FieldInfo.FIELD_DATE + " date, "      //日期字段，要求的格式是 ‘YYYY-MM-DD HH:MM:SS’,其他的东西被忽略；
                    + FieldInfo.FIELD_POINT_NO + " integer, "
                    + FieldInfo.FIELD_POINT_LATITUDE + " text, "
                    + FieldInfo.FIELD_POINT_LONGITUDE + " text, "
                    + FieldInfo.FIELD_POINT_X_COORDINATE + " text, "
                    + FieldInfo.FIELD_POINT_Y_COORDINATE + " text);";

    // 定义拖拉机参数数据表
    static final String TABLE_TRACTOR = "tractor";  //拖拉机表名

    // 定义生成拖拉机参数数据表SQL语句
    private static final String CREATE_TRACTOR_TABLE =
            "create table if not exists " + TABLE_TRACTOR
                    + " (_id integer primary key autoincrement, "
                    + TractorInfo.TRACTOR_NAME + " text, "
                    + TractorInfo.TRACTOR_TYPE + " text, "
                    + TractorInfo.TRACTOR_MADE + " text, "
                    + TractorInfo.TRACTOR_TYPE_NUMBER + " text, "
                    + TractorInfo.TRACTOR_WHEELBASE + " text, "
                    + TractorInfo.TRACTOR_ANTENNA_LATERAL + " text, "
                    + TractorInfo.TRACTOR_ANTENNA_REAR + " text, "
                    + TractorInfo.TRACTOR_ANTENNA_HEIGHT + " text, "
                    + TractorInfo.TRACTOR_MIN_TURNING_RADIUS + " text, "
                    + TractorInfo.TRACTOR_ANGLE_CORRECTION + " text, "
                    + TractorInfo.TRACTOR_IMPLEMENT_WIDTH + " text, "
                    + TractorInfo.TRACTOR_IMPLEMENT_OFFSET + " text, "
                    + TractorInfo.TRACTOR_IMPLEMENT_LENGTH + " text, "
                    + TractorInfo.TRACTOR_OPERATION_LINESPACING + " text);";

    // 定义AB线数据表
    static final String TABLE_AB_LINE = "ab_line";  //拖拉机表名
    // 定义生成拖拉机参数数据表SQL语句
    private static final String CREATE_AB_LINE_TABLE =
            "create table if not exists " + TABLE_AB_LINE
                    + " (_id integer primary key autoincrement, "
                    + ABLine.AB_LINE_NAME_BY_DATE + " text, "
                    + ABLine.A_POINT_X_COORDINATE + " text, "
                    + ABLine.A_POINT_Y_COORDINATE + " text, "
                    + ABLine.B_POINT_X_COORDINATE + " text, "
                    + ABLine.B_POINT_Y_COORDINATE + " text, "
                    + ABLine.FIELD_NAME + " text);";

    // 定义历史数据文件名数据表
    static final String TABLE_HISTORY = "history";  //拖拉机表名
    // 定义生成拖拉机参数数据表SQL语句
    private static final String CREATE_HISTORY_TABLE =
            "create table if not exists " + TABLE_HISTORY
                    + " (_id integer primary key autoincrement, "
                    + HistoryPath.HISTORY_RECORD_FILE_NAME + " text, "
                    + HistoryPath.FIELD_NAME + " text);";


    /*构造方法，将默认名称和版本置为上面的静态变量，将cursorFactory置为空*/
    private MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 获取单例实例
     *
     * @param context 应用背景
     * @return
     */
    public static MyDatabaseHelper getInstance(Context context) {
     /*双重锁定，只有在为空的时候，才会有同步锁的影响，这样可以提高效率*/
        if (instance == null) {
            synchronized (MyDatabaseHelper.class) {
                if (instance == null) {
                    instance = new MyDatabaseHelper(context);
                }
            }
        }
        return instance;
    }

    /*onCreate()方法当数据库第一次被创建时调用，并且仅仅调用一次。*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("-->>-" + "db_creating");
        //标准SQL语句，用于创建一张新表，使得_id为自动增长的主键，并且带有name和address等字符串变量
        db.execSQL(CREATE_FIELD_TABLE);
        db.execSQL(CREATE_TRACTOR_TABLE);
        db.execSQL(CREATE_AB_LINE_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    /*当版本号发生改变时即会执行onUpgrade方法，该方法主要用来增/删一列或多列的大幅度修改工作*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        /*onUpgrade()方法当数据库进行版本升级时被调用。当有时候需要补充建表时，可以把建表语句写在这里。
        可以避免android.database.sqlite.SQLiteException:no such table ... 异常。*/
        super.onOpen(db);
//        db.execSQL(CREATE_FIELD_TABLE);
//        db.execSQL(CREATE_TRACTOR_TABLE);
//        db.execSQL(CREATE_AB_LINE_TABLE);
//        db.execSQL(CREATE_HISTORY_TABLE);
    }

}
