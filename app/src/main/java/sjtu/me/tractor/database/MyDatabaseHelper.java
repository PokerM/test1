package sjtu.me.tractor.database;

//DBHelper�����ڴ����͸������ݿ�

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.navigation.HistoryPath;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // ֱ���þ�̬final�����������ݿ�İ汾�����ƣ���ֱ���ù��췽�����������������
    public static final String DB_NAME = "auto_tractor";
    private static final int DB_VERSION = 1;
    private static MyDatabaseHelper instance;

    // ����ؿ����ݿ�
    static final String TABLE_FIELD = "field";   //�ؿ����

    // �������ɵؿ����ݱ�SQL���
    private static final String CREATE_FIELD_TABLE =
            "create table if not exists " + TABLE_FIELD
                    + " (_id integer primary key autoincrement, "
                    + FieldInfo.FIELD_ID + " integer, "
                    + FieldInfo.FIELD_NAME + " text, "
                    + FieldInfo.FIELD_DATE + " date, "      //�����ֶΣ�Ҫ��ĸ�ʽ�� ��YYYY-MM-DD HH:MM:SS��,�����Ķ��������ԣ�
                    + FieldInfo.FIELD_POINT_NO + " integer, "
                    + FieldInfo.FIELD_POINT_LATITUDE + " text, "
                    + FieldInfo.FIELD_POINT_LONGITUDE + " text, "
                    + FieldInfo.FIELD_POINT_X_COORDINATE + " text, "
                    + FieldInfo.FIELD_POINT_Y_COORDINATE + " text);";

    // �����������������ݱ�
    static final String TABLE_TRACTOR = "tractor";  //����������

    // ���������������������ݱ�SQL���
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

    // ����AB�����ݱ�
    static final String TABLE_AB_LINE = "ab_line";  //����������
    // ���������������������ݱ�SQL���
    private static final String CREATE_AB_LINE_TABLE =
            "create table if not exists " + TABLE_AB_LINE
                    + " (_id integer primary key autoincrement, "
                    + ABLine.AB_LINE_NAME_BY_DATE + " text, "
                    + ABLine.A_POINT_X_COORDINATE + " text, "
                    + ABLine.A_POINT_Y_COORDINATE + " text, "
                    + ABLine.B_POINT_X_COORDINATE + " text, "
                    + ABLine.B_POINT_Y_COORDINATE + " text, "
                    + ABLine.FIELD_NAME + " text);";

    // ������ʷ�����ļ������ݱ�
    static final String TABLE_HISTORY = "history";  //����������
    // ���������������������ݱ�SQL���
    private static final String CREATE_HISTORY_TABLE =
            "create table if not exists " + TABLE_HISTORY
                    + " (_id integer primary key autoincrement, "
                    + HistoryPath.HISTORY_RECORD_FILE_NAME + " text, "
                    + HistoryPath.FIELD_NAME + " text);";


    /*���췽������Ĭ�����ƺͰ汾��Ϊ����ľ�̬��������cursorFactory��Ϊ��*/
    private MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * ��ȡ����ʵ��
     *
     * @param context Ӧ�ñ���
     * @return
     */
    public static MyDatabaseHelper getInstance(Context context) {
     /*˫��������ֻ����Ϊ�յ�ʱ�򣬲Ż���ͬ������Ӱ�죬�����������Ч��*/
        if (instance == null) {
            synchronized (MyDatabaseHelper.class) {
                if (instance == null) {
                    instance = new MyDatabaseHelper(context);
                }
            }
        }
        return instance;
    }

    /*onCreate()���������ݿ��һ�α�����ʱ���ã����ҽ�������һ�Ρ�*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("-->>-" + "db_creating");
        //��׼SQL��䣬���ڴ���һ���±�ʹ��_idΪ�Զ����������������Ҵ���name��address���ַ�������
        db.execSQL(CREATE_FIELD_TABLE);
        db.execSQL(CREATE_TRACTOR_TABLE);
        db.execSQL(CREATE_AB_LINE_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    /*���汾�ŷ����ı�ʱ����ִ��onUpgrade�������÷�����Ҫ������/ɾһ�л���еĴ�����޸Ĺ���*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        /*onUpgrade()���������ݿ���а汾����ʱ�����á�����ʱ����Ҫ���佨��ʱ�����԰ѽ������д�����
        ���Ա���android.database.sqlite.SQLiteException:no such table ... �쳣��*/
        super.onOpen(db);
//        db.execSQL(CREATE_FIELD_TABLE);
//        db.execSQL(CREATE_TRACTOR_TABLE);
//        db.execSQL(CREATE_AB_LINE_TABLE);
//        db.execSQL(CREATE_HISTORY_TABLE);
    }

}
