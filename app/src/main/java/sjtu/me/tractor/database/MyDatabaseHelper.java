package sjtu.me.tractor.database;

//DBHelper，用于创建和更新数据库
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	 
	// 直接用静态final变量设置数据库的版本和名称，并直接让构造方法仅传入参数上下文
    public static final String DB_NAME = "auto_tractor";
    private static final int DB_VERSION = 1;
    
    // 定义地块数据库
    static final String TABLE_FIELD = "field";   //地块表名
    
    // 定义生成地块数据表SQL语句
    private static final String CREATE_FIELD_TABLE = 
            "create table " + TABLE_FIELD 
            + " (_id integer primary key autoincrement, " 
            + FieldInfo.FIELD_ID + " integer, "
            + FieldInfo.FIELD_NAME + " text, "
            + FieldInfo.FIELD_DATE + " date, "      //日期字段，要求的格式是 ‘YYYY-MM-DD HH:MM:SS’,其他的东西被忽略；
            + FieldInfo.FIELD_POINT_NO + " integer, "
            + FieldInfo.FIELD_POINT_LATITUDE + " text, "
            + FieldInfo.FIELD_POINT_LONGITUDE + " text, "
            + FieldInfo.FIELD_POINT_X_COORDINATE + " text, "
            + FieldInfo.FIELD_POINT_Y_COORDINATE + " text);";
    
    // 定义拖拉机参数数据库
    static final String TABLE_TRACTOR = "tractor";  //拖拉机表名
    
    // 定义生成拖拉机参数数据表SQL语句
    private static final String CREATE_TRACTOR_TABLE = 
            "create table " + TABLE_TRACTOR
            + " (_id integer primary key autoincrement, "
            + TractorInfo.T_NAME + " text, "
            + TractorInfo.T_TYPE + " text, "
            + TractorInfo.T_MADE + " text, "
            + TractorInfo.T_TYPE_NUMBER + " text, "
            + TractorInfo.T_WHEELBASE + " text, "
            + TractorInfo.T_ANTENNA_LATERAL + " text, "
            + TractorInfo.T_ANTENNA_REAR + " text, "
            + TractorInfo.T_ANTENNA_HEIGHT + " text, "
            + TractorInfo.T_MIN_TURNING_RADIUS + " text, "
            + TractorInfo.T_ANGLE_CORRECTION + " text, "
            + TractorInfo.T_IMPLEMENT_WIDTH + " text, "
            + TractorInfo.T_IMPLEMENT_OFFSET + " text, "
            + TractorInfo.T_IMPLEMENT_LENGTH + " text, "
            + TractorInfo.T_OPERATION_LINESPACING + " text);";
            
	
	//构造方法，将默认名称和版本置为上面的静态变量，将cursorFactory置为空
	MyDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
	}
	
	//只要DBHelper被构造则立即调用onCreate方法，并且仅仅调用一次。
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("-->>-" + "db_creating");
		//标准SQL语句，用于创建一张新表，使得_id为自动增长的主键，并且带有name和address等字符串变量
		db.execSQL(CREATE_FIELD_TABLE);
		db.execSQL(CREATE_TRACTOR_TABLE);
	}
	
	//当版本号发生改变时即会执行onUpgrade方法，该方法主要用来增/删一列或多列的大幅度修改工作
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
