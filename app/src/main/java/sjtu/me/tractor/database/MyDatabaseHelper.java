package sjtu.me.tractor.database;

//DBHelper�����ڴ����͸������ݿ�
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	 
	// ֱ���þ�̬final�����������ݿ�İ汾�����ƣ���ֱ���ù��췽�����������������
    public static final String DB_NAME = "auto_tractor";
    private static final int DB_VERSION = 1;
    
    // ����ؿ����ݿ�
    static final String TABLE_FIELD = "field";   //�ؿ����
    
    // �������ɵؿ����ݱ�SQL���
    private static final String CREATE_FIELD_TABLE = 
            "create table " + TABLE_FIELD 
            + " (_id integer primary key autoincrement, " 
            + FieldInfo.FIELD_ID + " integer, "
            + FieldInfo.FIELD_NAME + " text, "
            + FieldInfo.FIELD_DATE + " date, "      //�����ֶΣ�Ҫ��ĸ�ʽ�� ��YYYY-MM-DD HH:MM:SS��,�����Ķ��������ԣ�
            + FieldInfo.FIELD_POINT_NO + " integer, "
            + FieldInfo.FIELD_POINT_LATITUDE + " text, "
            + FieldInfo.FIELD_POINT_LONGITUDE + " text, "
            + FieldInfo.FIELD_POINT_X_COORDINATE + " text, "
            + FieldInfo.FIELD_POINT_Y_COORDINATE + " text);";
    
    // �����������������ݿ�
    static final String TABLE_TRACTOR = "tractor";  //����������
    
    // ���������������������ݱ�SQL���
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
            
	
	//���췽������Ĭ�����ƺͰ汾��Ϊ����ľ�̬��������cursorFactory��Ϊ��
	MyDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
	}
	
	//ֻҪDBHelper����������������onCreate���������ҽ�������һ�Ρ�
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("-->>-" + "db_creating");
		//��׼SQL��䣬���ڴ���һ���±�ʹ��_idΪ�Զ����������������Ҵ���name��address���ַ�������
		db.execSQL(CREATE_FIELD_TABLE);
		db.execSQL(CREATE_TRACTOR_TABLE);
	}
	
	//���汾�ŷ����ı�ʱ����ִ��onUpgrade�������÷�����Ҫ������/ɾһ�л���еĴ�����޸Ĺ���
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
