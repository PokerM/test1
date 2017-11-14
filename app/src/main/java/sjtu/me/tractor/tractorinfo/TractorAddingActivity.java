package sjtu.me.tractor.tractorinfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.AlertDialogUtil;

public class TractorAddingActivity extends FragmentActivity implements OnClickListener {

    public static final String TAG = "TractorAddingActivity";
    public static final boolean D = true;

    public static final String[] TRACTOR_TYPES = new String[]{"�½�ʽ", "����ʽ"};
    public static final String[] TRACTOR_MADES = new String[]{"CashIH", "John Deere", "New Holland", "����", "����", "�ñ���"};
    public static final String[] TRACTOR_TYPE_NUMBERS = new String[]{"MT-755", "MT-775", "MT-805", "MT-835"};

    private Button btnSelectType;
    private Button btnSelectMade;
    private Button btnSelectTypeNumber;
    private ImageButton btnExitTractorAddingActivity;  //���ذ�ť
    private Button btnAffirmTractorName;
    private Button btnTractorNameEdit;
    private Button btnAffirmAddingTractor;
    private Button btnExitAddingTractor;
    public TextView txtTractorName;
    public EditText editTextTractorName;
    public TextView txtTractorType;
    public TextView txtTractorMade;
    public TextView txtTractorTypeNumber;
    private LinearLayout layoutSetTractorType;
    private LinearLayout layoutSetTractorMade;
    private LinearLayout layoutSetTractorTypeNumber;
    private LinearLayout layoutPagerCircles;

    private OnClickChoiceListener choiceListenerTractorType;
    private OnClickChoiceListener choiceListenerTractorMade;
    private OnClickChoiceListener choiceListenerTractorTypeNumber;

    private View viewWheelbase; //������������ͼ
    private View viewAntennaLateral; //���ߺ���ƫ�����������ͼ
    private View viewAntennaRearAxle; //���ߵ�����ƫ�����������ͼ
    private View viewAntennaHeight; //���߰�װ�߶Ȳ���������ͼ
    private View viewTurningRadius; //��Сת��뾶����������ͼ
    private View viewAngleCorrection; //�Ƕȴ�����У������������ͼ
    private View viewImplementWidth;  //ũ�߷������������ͼ
    private View viewImplementOffset;  //ũ�߷������������ͼ
    private View viewImplementLength;  //ũ�߷������������ͼ
    private View viewLineSpacing;  //��ҵ�м�����������ͼ

    public TextView txtWheelbase;
    public TextView txtAntennaLateral;
    public TextView txtAntennaRear;
    public TextView txtAntennaHeight;
    public TextView txtMinTurning;
    public TextView txtAngleCorrection;
    public TextView txtImplementWidth;
    public TextView txtImplementOffset;
    public TextView txtImplementLength;
    public TextView txtLineSpacing;
    public ListView lstInfoInput;

    public String[] tractorAttributeName = new String[10];  //������Ϣ��������
    public String[] tractorAttributeValue = new String[10];  //������Ϣ����ֵ
    public List<Map<String, String>> listItems;
    public TractorInfoInputListAdapter tractorInfoInputListAdapter;
    public TractorPagerAdapter tractorPagerAdapter;
    public ViewPager viewPager;
    public List<Fragment> tractorInfoFragments = new ArrayList<>();
    private Info0Wheelbase wheelbaseFragment = new Info0Wheelbase();
    private Info1AntennaLateral antennaLateralFragment = new Info1AntennaLateral();
    private Info2AntennaRear antennaRearFragment = new Info2AntennaRear();
    private Info3AntennaHeight antennaHeightFragment = new Info3AntennaHeight();
    private Info4MinTurning turningRadiusFragment = new Info4MinTurning();
    private Info5AngleCorrection angleCorrectionFragment = new Info5AngleCorrection();
    private Info6ImplementWidth implementWidthFragment = new Info6ImplementWidth();
    private Info7ImplementOffset implementOffsetFragment = new Info7ImplementOffset();
    private Info8ImplementLength implementLengthFragment = new Info8ImplementLength();
    private Info9Linespacing lineSpacingFragment = new Info9Linespacing();

    public Intent intent;
    public Bundle bundle;

    private MyApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tractor_adding_activity);

        if (D) {
            Log.e(TAG, "*** ON CREATE ***");
        }

        // ��ʼ��ȫ�ֱ���
        mApp = (MyApplication) getApplication();

        initViews();

        tractorAttributeName = new String[]{
                getString(R.string.tractor_wheelbase),
                getString(R.string.tractor_antenna_lateral),
                getString(R.string.tractor_antenna_rear),
                getString(R.string.tractor_antenna_height),
                getString(R.string.tractor_min_turning_radius),
                getString(R.string.tractor_angle_correction),
                getString(R.string.tractor_implement_width),
                getString(R.string.tractor_implement_offset),
                getString(R.string.tractor_implement_length),
                getString(R.string.tractor_operation_linespcing)};

        tractorAttributeValue = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};

        //��ȡlistView����ʹ֮���أ����������ƺ󷽿����֣�
        lstInfoInput = (ListView) findViewById(R.id.lstTractorInfoInput);
        lstInfoInput.setVisibility(View.INVISIBLE);

        //�����б����ݲ��½������������б����ݼ��ص���������
        updateItems();
        tractorInfoInputListAdapter = new TractorInfoInputListAdapter(TractorAddingActivity.this, listItems);
        lstInfoInput.setAdapter(tractorInfoInputListAdapter);

        // ��������Ƭ��ӵ��б�
        addFragments(tractorInfoFragments);

        //��ȡviewPager����ʹ֮���أ����������ƺ󷽿����֣�
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setVisibility(View.INVISIBLE);

        //��fragments��viewPager���ݴ��ݵ���������
        tractorPagerAdapter = new TractorPagerAdapter(this.getSupportFragmentManager(), tractorInfoFragments, viewPager, this);

        //���û������޸Ľ���ʱ��Ҳ����ʼ�Ŀ��������Ͳ�Ϊ��ʱ��һ������ʾȫ��ҳ�沢���ú����ݣ�
        intent = getIntent();
        bundle = intent.getExtras();
        if (bundle != null) {
            lstInfoInput.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            layoutSetTractorType.setVisibility(View.VISIBLE);
            layoutSetTractorMade.setVisibility(View.VISIBLE);
            layoutSetTractorTypeNumber.setVisibility(View.VISIBLE);
            layoutPagerCircles.setVisibility(View.VISIBLE);
            editTextTractorName.setVisibility(View.GONE);
            txtTractorName.setVisibility(View.VISIBLE);

            editTextTractorName.setText(bundle.getString(TractorInfo.T_NAME));
            txtTractorName.setText(bundle.getString(TractorInfo.T_NAME));
            txtTractorType.setText(bundle.getString(TractorInfo.T_TYPE));
            txtTractorMade.setText(bundle.getString(TractorInfo.T_MADE));
            txtTractorTypeNumber.setText(bundle.getString(TractorInfo.T_TYPE_NUMBER));

            AlertDialog dialog = new AlertDialog.Builder(TractorAddingActivity.this)
                    .setTitle(getString(R.string.alert_dialog_title_edit_tractor))
                    .setMessage(R.string.alert_msg_tractor_info_edit_back)
                    .setPositiveButton(getString(R.string.affirm),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tractorAttributeValue[0] = bundle.getString(TractorInfo.T_WHEELBASE);
                                    tractorAttributeValue[1] = bundle.getString(TractorInfo.T_ANTENNA_LATERAL);
                                    tractorAttributeValue[2] = bundle.getString(TractorInfo.T_ANTENNA_REAR);
                                    tractorAttributeValue[3] = bundle.getString(TractorInfo.T_ANTENNA_HEIGHT);
                                    tractorAttributeValue[4] = bundle.getString(TractorInfo.T_MIN_TURNING_RADIUS);
                                    tractorAttributeValue[5] = bundle.getString(TractorInfo.T_ANGLE_CORRECTION);
                                    tractorAttributeValue[6] = bundle.getString(TractorInfo.T_IMPLEMENT_WIDTH);
                                    tractorAttributeValue[7] = bundle.getString(TractorInfo.T_IMPLEMENT_LENGTH);
                                    tractorAttributeValue[8] = bundle.getString(TractorInfo.T_IMPLEMENT_OFFSET);
                                    tractorAttributeValue[9] = bundle.getString(TractorInfo.T_OPERATION_LINESPACING);

                                    updateItems();

                                    //��ȡlistView�����е�textView�ؼ���ʹ֮��������viewPagerÿһҳ��editTextʵ������
                                    tractorInfoInputListAdapter = new TractorInfoInputListAdapter(TractorAddingActivity.this, listItems);
                                    TractorInfoInputListAdapter tractorListAdapter = (TractorInfoInputListAdapter) lstInfoInput.getAdapter();
                                    tractorListAdapter.updateData(listItems);

                                    viewWheelbase = lstInfoInput.getChildAt(0);
                                    txtWheelbase = (TextView) viewWheelbase.findViewById(R.id.txtLabelDetail);
                                    txtWheelbase.setText(tractorAttributeValue[0]);

                                    viewAntennaLateral = lstInfoInput.getChildAt(1);
                                    txtAntennaLateral = (TextView) viewAntennaLateral.findViewById(R.id.txtLabelDetail);
                                    txtAntennaLateral.setText(tractorAttributeValue[1]);

                                    viewAntennaRearAxle = lstInfoInput.getChildAt(2);
                                    txtAntennaRear = (TextView) viewAntennaRearAxle.findViewById(R.id.txtLabelDetail);
                                    txtAntennaRear.setText(tractorAttributeValue[2]);

                                    viewAntennaHeight = lstInfoInput.getChildAt(3);
                                    txtAntennaHeight = (TextView) viewAntennaHeight.findViewById(R.id.txtLabelDetail);
                                    txtAntennaHeight.setText(tractorAttributeValue[3]);

                                    viewTurningRadius = lstInfoInput.getChildAt(4);
                                    txtMinTurning = (TextView) viewTurningRadius.findViewById(R.id.txtLabelDetail);
                                    txtMinTurning.setText(tractorAttributeValue[4]);

                                    viewAngleCorrection = lstInfoInput.getChildAt(5);
                                    txtAngleCorrection = (TextView) viewAngleCorrection.findViewById(R.id.txtLabelDetail);
                                    txtAngleCorrection.setText(tractorAttributeValue[5]);

                                    viewImplementWidth = lstInfoInput.getChildAt(6);
                                    txtImplementWidth = (TextView) viewImplementWidth.findViewById(R.id.txtLabelDetail);
                                    txtImplementWidth.setText(tractorAttributeValue[6]);

                                    viewImplementOffset = lstInfoInput.getChildAt(7);
                                    txtImplementOffset = (TextView) viewImplementOffset.findViewById(R.id.txtLabelDetail);
                                    txtImplementOffset.setText(tractorAttributeValue[7]);

                                    viewImplementLength = lstInfoInput.getChildAt(8);
                                    txtImplementLength = (TextView) viewImplementLength.findViewById(R.id.txtLabelDetail);
                                    txtImplementLength.setText(tractorAttributeValue[8]);

                                    viewLineSpacing = lstInfoInput.getChildAt(9);
                                    txtLineSpacing = (TextView) viewLineSpacing.findViewById(R.id.txtLabelDetail);
                                    txtLineSpacing.setText(tractorAttributeValue[9]);
                                }
                            })
                    .create();
            dialog.show();
            AlertDialogUtil.changeDialogTheme(dialog);
        } else {
            //��һ���Ի������������ʱ�䣬ʹview�ܹ�����ȡ
            AlertDialog dialogStartSetting = new AlertDialog.Builder(TractorAddingActivity.this)
                    .setTitle(getString(R.string.alert_dialog_title_tractorinfo_setting))
                    .setMessage(getString(R.string.alert_dialog_message_tractorinfo_setting))
                    .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            viewWheelbase = lstInfoInput.getChildAt(0);
                            txtWheelbase = (TextView) viewWheelbase.findViewById(R.id.txtLabelDetail);

                            viewAntennaLateral = lstInfoInput.getChildAt(1);
                            txtAntennaLateral = (TextView) viewAntennaLateral.findViewById(R.id.txtLabelDetail);

                            viewAntennaRearAxle = lstInfoInput.getChildAt(2);
                            txtAntennaRear = (TextView) viewAntennaRearAxle.findViewById(R.id.txtLabelDetail);

                            viewAntennaHeight = lstInfoInput.getChildAt(3);
                            txtAntennaHeight = (TextView) viewAntennaHeight.findViewById(R.id.txtLabelDetail);

                            viewTurningRadius = lstInfoInput.getChildAt(4);
                            txtMinTurning = (TextView) viewTurningRadius.findViewById(R.id.txtLabelDetail);

                            viewAngleCorrection = lstInfoInput.getChildAt(5);
                            txtAngleCorrection = (TextView) viewAngleCorrection.findViewById(R.id.txtLabelDetail);

                            viewImplementWidth = lstInfoInput.getChildAt(6);
                            txtImplementWidth = (TextView) viewImplementWidth.findViewById(R.id.txtLabelDetail);

                            viewImplementOffset = lstInfoInput.getChildAt(7);
                            txtImplementOffset = (TextView) viewImplementOffset.findViewById(R.id.txtLabelDetail);

                            viewImplementLength = lstInfoInput.getChildAt(8);
                            txtImplementLength = (TextView) viewImplementLength.findViewById(R.id.txtLabelDetail);

                            viewLineSpacing = lstInfoInput.getChildAt(9);
                            txtLineSpacing = (TextView) viewLineSpacing.findViewById(R.id.txtLabelDetail);
                        }
                    }).create();
            dialogStartSetting.show();
            AlertDialogUtil.changeDialogTheme(dialogStartSetting);
        }

        //Ϊ������趨�ı�������ʵ��ʵʱ�������ƵĹ���
        editTextTractorName.addTextChangedListener(new TextWatcher() {
            String newString, filteredString;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newString = editTextTractorName.getText().toString();
                filteredString = stringFilter(newString);
                txtTractorName.setText(filteredString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    //  ��ʼ�����ؼ�
    private void initViews() {
        btnSelectType = (Button) findViewById(R.id.btnSelectType);
        btnSelectType.setOnClickListener(this);

        btnSelectMade = (Button) findViewById(R.id.btnSelectMade);
        btnSelectMade.setOnClickListener(this);

        btnSelectTypeNumber = (Button) findViewById(R.id.btnSelectTypeNumber);
        btnSelectTypeNumber.setOnClickListener(this);

        btnExitTractorAddingActivity = (ImageButton) findViewById(R.id.btnExitTractorAdding);
        btnExitTractorAddingActivity.setOnClickListener(this);

        btnAffirmTractorName = (Button) findViewById(R.id.btnAffirmTractorName);
        btnAffirmTractorName.setOnClickListener(this);

        btnTractorNameEdit = (Button) findViewById(R.id.btnTractorNameEdit);
        btnTractorNameEdit.setOnClickListener(this);

        btnExitAddingTractor = (Button) findViewById(R.id.btnExitAddingTractor);
        btnExitAddingTractor.setOnClickListener(this);

        btnAffirmAddingTractor = (Button) findViewById(R.id.btnAffirmAddingTractor);
        btnAffirmAddingTractor.setOnClickListener(this);


        editTextTractorName = (EditText) findViewById(R.id.editTextTractorName);
        txtTractorName = (TextView) findViewById(R.id.cellTextTractorName);
        txtTractorType = (TextView) findViewById(R.id.txtTractorTypeValue);
        txtTractorMade = (TextView) findViewById(R.id.txtTractorMadeValue);
        txtTractorTypeNumber = (TextView) findViewById(R.id.txtTractorTypeNumberValue);

        layoutSetTractorType = (LinearLayout) findViewById(R.id.layoutSetTractorType);
        layoutSetTractorMade = (LinearLayout) findViewById(R.id.layoutSetTractorMade);
        layoutSetTractorTypeNumber = (LinearLayout) findViewById(R.id.layoutSetTractorTypeNumber);
        layoutPagerCircles = (LinearLayout) findViewById(R.id.imgPagerCircles);

        choiceListenerTractorType = new OnClickChoiceListener(R.id.btnSelectType);
        choiceListenerTractorMade = new OnClickChoiceListener(R.id.btnSelectMade);
        choiceListenerTractorTypeNumber = new OnClickChoiceListener(R.id.btnSelectTypeNumber);

    }

    /**
     * ����������ʽ�������˵��ַ����еĿո�
     *
     * @param string �������ַ���
     * @return ���˺��ַ���
     */
    public static String stringFilter(String string) {
        String regEx = " ";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(string);
        return matcher.replaceAll("");
    }

    /**
     * ʵ�ֳ�ʼ���͸����б����ݲ���
     */
    public void updateItems() {
        List<Map<String, String>> list = new ArrayList<>();

        for (int i = 0; i < tractorAttributeName.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("name", tractorAttributeName[i]);
            map.put("detail", tractorAttributeValue[i]);
            list.add(map);
        }
        this.listItems = list;
    }

    /**
     * �б���ӳ�Ա
     *
     * @param fragments ��ӵ���Ƭ
     */
    private void addFragments(List<Fragment> fragments) {
        fragments.add(wheelbaseFragment);
        fragments.add(antennaLateralFragment);
        fragments.add(antennaRearFragment);
        fragments.add(antennaHeightFragment);
        fragments.add(turningRadiusFragment);
        fragments.add(angleCorrectionFragment);
        fragments.add(implementWidthFragment);
        fragments.add(implementOffsetFragment);
        fragments.add(implementLengthFragment);
        fragments.add(lineSpacingFragment);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnAffirmTractorName:
                if (!editTextTractorName.getText().toString().isEmpty()) {
                    //��ȥ�����������ʾ�ı��ؼ�
                    editTextTractorName.setText(txtTractorName.getText());
                    editTextTractorName.setVisibility(View.GONE);
                    txtTractorName.setVisibility(View.VISIBLE);
                    // �������ƺ�������ÿɼ�
                    layoutSetTractorType.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.editTextTractorName:
                editTextTractorName.setText(txtTractorName.getText());
                editTextTractorName.setVisibility(View.VISIBLE);
                txtTractorName.setVisibility(View.GONE);
                break;

            case R.id.btnSelectType:
                AlertDialog dialogType = new AlertDialog.Builder(TractorAddingActivity.this)
                        .setTitle(R.string.dialog_title_select_tractor_type)
                        .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //�����ѡ��ſ��Լ����������涯������ͬ
                                if (!txtTractorType.getText().toString().isEmpty()) {
                                    layoutSetTractorMade.setVisibility(View.VISIBLE);
                                }
                            }

                        })
                        .setSingleChoiceItems(TRACTOR_TYPES, -1, choiceListenerTractorType)
                        .create();
                dialogType.show();
                AlertDialogUtil.changeDialogTheme(dialogType);
                break;

            case R.id.btnSelectMade:
                AlertDialog dialogMade = new AlertDialog.Builder(TractorAddingActivity.this)
                        .setTitle(R.string.dialog_title_select_tractor_made)
                        .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //�����ѡ��ſ��Լ����������涯������ͬ
                                if (!txtTractorMade.getText().toString().isEmpty()) {
                                    layoutSetTractorTypeNumber.setVisibility(View.VISIBLE);
                                }
                            }

                        })
                        .setSingleChoiceItems(TRACTOR_MADES, -1, choiceListenerTractorMade)
                        .create();
                dialogMade.show();
                AlertDialogUtil.changeDialogTheme(dialogMade);
                break;

            case R.id.btnSelectTypeNumber:
                AlertDialog dialogTypeNumber = new AlertDialog.Builder(TractorAddingActivity.this)
                        .setTitle(R.string.dialog_title_select_tractor_type_number)
                        .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //�����ѡ��ſ��Լ����������涯��
                                if (!txtTractorTypeNumber.getText().toString().isEmpty()) {
                                    lstInfoInput.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.VISIBLE);
                                    layoutPagerCircles.setVisibility(View.VISIBLE);
                                }
                                // ��ȡListView�����е�TextView�ؼ���ʹ֮����ViewPagerÿһҳ��EditTextʵ������
                                viewWheelbase = lstInfoInput.getChildAt(0);
                                txtWheelbase = (TextView) viewWheelbase.findViewById(R.id.txtLabelDetail);

                                viewAntennaLateral = lstInfoInput.getChildAt(1);
                                txtAntennaLateral = (TextView) viewAntennaLateral.findViewById(R.id.txtLabelDetail);

                                viewAntennaRearAxle = lstInfoInput.getChildAt(2);
                                txtAntennaRear = (TextView) viewAntennaRearAxle.findViewById(R.id.txtLabelDetail);

                                viewAntennaHeight = lstInfoInput.getChildAt(3);
                                txtAntennaHeight = (TextView) viewAntennaHeight.findViewById(R.id.txtLabelDetail);

                                viewTurningRadius = lstInfoInput.getChildAt(4);
                                txtMinTurning = (TextView) viewTurningRadius.findViewById(R.id.txtLabelDetail);

                                viewAngleCorrection = lstInfoInput.getChildAt(5);
                                txtAngleCorrection = (TextView) viewAngleCorrection.findViewById(R.id.txtLabelDetail);

                                viewImplementWidth = lstInfoInput.getChildAt(6);
                                txtImplementWidth = (TextView) viewImplementWidth.findViewById(R.id.txtLabelDetail);

                                viewImplementOffset = lstInfoInput.getChildAt(7);
                                txtLineSpacing = (TextView) viewLineSpacing.findViewById(R.id.txtLabelDetail);

                                viewImplementLength = lstInfoInput.getChildAt(8);
                                txtLineSpacing = (TextView) viewLineSpacing.findViewById(R.id.txtLabelDetail);

                                viewLineSpacing = lstInfoInput.getChildAt(9);
                                txtLineSpacing = (TextView) viewLineSpacing.findViewById(R.id.txtLabelDetail);
                            }

                        })
                        .setSingleChoiceItems(TRACTOR_TYPE_NUMBERS, -1, choiceListenerTractorTypeNumber)
                        .create();
                dialogTypeNumber.show();
                AlertDialogUtil.changeDialogTheme(dialogTypeNumber);
                break;

            case R.id.btnExitTractorAdding:
            case R.id.btnExitAddingTractor:
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnAffirmAddingTractor:
                AlertDialog dialogAffirm = new AlertDialog.Builder(TractorAddingActivity.this)
                        .setTitle(getString(R.string.alert_title_tips))
                        .setMessage(getString(R.string.alert_msg_tractor_info_tips))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.affirm_submit, new DialogInterface.OnClickListener() {
                            String tractorName = txtTractorName.getText().toString();

                            void turnPage() {
//                                Intent intent = new Intent("sjtu.me.tractor.main.HomeActivity");
//                                startActivity(intent);
                                TractorAddingActivity.this.finish();
                            }

                            //
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (!tractorName.isEmpty()) {
                                    if (mApp.getDatabaseManager() != null) {
                                        if (!(txtTractorType.toString().isEmpty() || txtTractorMade.toString().isEmpty()
                                                || txtTractorTypeNumber.toString().isEmpty())) {
                                            final String[] tractorInfo = new String[14];
                                            tractorInfo[0] = tractorName;
                                            tractorInfo[1] = txtTractorType.getText().toString();
                                            tractorInfo[2] = txtTractorMade.getText().toString();
                                            tractorInfo[3] = txtTractorTypeNumber.getText().toString();
                                            System.arraycopy(tractorAttributeValue, 0, tractorInfo, 4, tractorAttributeValue.length);

                                            //�������Ʋ�ѯ���ݿ�
                                            Cursor cursor = mApp.getDatabaseManager().queryTractorByName(tractorName);
                                            Map<String, String> map = DatabaseManager.cursorToMap(cursor);
                                            if (map != null && map.size() != 0) {

                                                AlertDialog dialogNameDuplicate = new AlertDialog.Builder(TractorAddingActivity.this)
                                                        .setTitle(getString(R.string.alert_title_tractor_name_error))
                                                        .setMessage(getString(R.string.alert_msg_tractor_name_error))
                                                        .setIcon(R.drawable.alert)
                                                        .setPositiveButton(getString(R.string.alert_btn_tractor_overrite), new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                // �������ݿ��ļ��еĳ�����Ϣ��Ŀ
                                                                boolean flag = mApp.getDatabaseManager().updateTractorByName(tractorName, tractorInfo);
                                                                Log.e(TAG, "Update tractor table successfully? " + flag);
                                                                turnPage();
                                                            }
                                                        })
                                                        .setNegativeButton(getString(R.string.alert_btn_tractor_rename), new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }

                                                        })
                                                        .create();
                                                dialogNameDuplicate.show();
                                                AlertDialogUtil.changeDialogTheme(dialogNameDuplicate);
                                            } else {
                                                boolean flag = mApp.getDatabaseManager().insertDataToTractor(tractorInfo);
                                                Log.e(TAG, "Insert new entry to tractor table successfully? " + flag);
                                                turnPage();
                                            }
                                        } else {
                                            AlertDialog dialogTypeNullAlert = new AlertDialog.Builder(TractorAddingActivity.this)
                                                    .setTitle(getString(R.string.alert_title))
                                                    .setIcon(R.drawable.alert)
                                                    .setMessage(getString(R.string.tractor_type_null_alert))
                                                    .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .create();
                                            dialogTypeNullAlert.show();
                                            AlertDialogUtil.changeDialogTheme(dialogTypeNullAlert);
                                        }
                                    } else {
                                        turnPage();
                                    }
                                } else {
                                    AlertDialog dialogNameNullAlert = new AlertDialog.Builder(TractorAddingActivity.this)
                                            .setTitle(getString(R.string.alert_title))
                                            .setIcon(R.drawable.alert)
                                            .setMessage(getString(R.string.tractor_name_null_alert))
                                            .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .create();
                                    dialogNameNullAlert.show();
                                    AlertDialogUtil.changeDialogTheme(dialogNameNullAlert);
                                }
                            }

                        })
                        .setNegativeButton(R.string.back_to_edit, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }

                        }).create();
                dialogAffirm.show();
                AlertDialogUtil.changeDialogTheme(dialogAffirm);
                break;

            default:
                break;

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog alertDialog = new AlertDialog.Builder(TractorAddingActivity.this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.alert_dialog_message_warning))
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(getString(R.string.affirm),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TractorAddingActivity.this.finish();
                                }
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .create();
            alertDialog.show();
            AlertDialogUtil.changeDialogTheme(alertDialog);
        }
        return false;
    }


    //Ϊ�����Ի���ʵ��ʵʱ����
    public class OnClickChoiceListener implements DialogInterface.OnClickListener {
        private int id;

        //ʹ�ù��췽������һ����ť��id,��֪�����ĸ���ť�������ĶԻ�����
        OnClickChoiceListener(int id) {
            this.id = id;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (id) {
                case R.id.btnSelectType:
                    String tractorType = TRACTOR_TYPES[which];
                    txtTractorType.setText(tractorType);
                    break;

                case R.id.btnSelectMade:
                    String tractorMade = TRACTOR_MADES[which];
                    txtTractorMade.setText(tractorMade);
                    break;

                case R.id.btnSelectTypeNumber:
                    String tractorTypeNumber = TRACTOR_TYPE_NUMBERS[which];
                    txtTractorTypeNumber.setText(tractorTypeNumber);
                    break;

                default:
                    break;
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (D) {
            Log.e(TAG, "*** ON START ***");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (D) {
            Log.e(TAG, "*** ON RESUME ***");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D) {
            Log.e(TAG, "*** ON PAUSE ***");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (D) {
            Log.e(TAG, "*** ON STOP ***");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (D) {
            Log.e(TAG, "*** ON DESTROY ***");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (D) {
            Log.e(TAG, "*** ON RESTART ***");
        }
    }
}

