package sjtu.me.tractor.connection;


import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.bluetooth.DeviceListActivity;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.ToastUtil;

/**
 * @author BillHu
 *         ����Fragment��ͼ
 */
public class ConnectionFragment extends Fragment implements OnClickListener {

    public static final String TAG = "ConnectionFragment";
    public static final boolean D = false;

    // ��תҳ����������
    private static final int REQUEST_ENABLE_BLUETOOTH = 0; // ��ת��������������ҳ��ı�־
    private static final int REQUEST_SELECTE_BLUETOOTH = 1;

    //�����������ݱ�ǩ 
    public static String EXTRA_DEVICE_ADDRESS = "DeviceMacAddress";
    public static String EXTRA_DEVICE_NAME = "DeviceName";

    private MyApplication myApp; // ����ȫ�ֱ���
    private BluetoothService mBluetoothService = null; // ������������ʵ��
    BluetoothAdapter mBluetoothAdapter; // ����������
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;    //����������豸�б�
    private ArrayAdapter<String> mNewDevicesArrayAdapter;        //����δ����豸�б�

    private ListView pairedListView;
    private ListView newDevicesListView;
    private TextView txtReceivedMessage;
    private TextView txtReceivedMessageNum;
    private TextView txtSentMessage;
    private TextView txtCurrentDevice;
    private Button btnStartConnection;
    private Button btnScan;
    private Button btnCancel;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartConnection:
//            if (mBluetoothAdapter.isEnabled()) {
//                if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
//                    startActivityForResult(new Intent(getActivity(), DeviceListActivity.class), REQUEST_SELECTE_BLUETOOTH);
//                } else {
//                    myApp.getBluetoothService().stopConnection();
//                    txtReceivedMessage.setText("ֹͣ����");
//                    txtSentMessage.setText("ֹͣ����");
//                    btnStartConnection.setText("������");
//                }
//            }

                if (mBluetoothAdapter.isEnabled()) {
                    if (myApp.getBluetoothService().getState() != BluetoothService.STATE_NONE) {
                        myApp.getBluetoothService().stopConnection();
                        txtReceivedMessage.setText(R.string.stop_receiving);
                        txtSentMessage.setText(R.string.stop_sending);
                        btnStartConnection.setText(R.string.open_connection);
                    } else {
                        if (!MyApplication.ADDRESS_NULL.equals(myApp.getBluetoothAddress())) {
                            myApp.getBluetoothService().startConnection(myApp.getBluetoothAddress());
                            btnStartConnection.setText(R.string.close_connection);
                        } else {
                            ToastUtil.showToast(getString(R.string.select_an_available_device), true);
                        }
                    }
                }
                break;

            case R.id.button_cancel:
                // �ر����ڽ��еĲ��ҷ���
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                break;

            case R.id.button_scan:
                doDiscovery();
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    ToastUtil.showToast(getString(R.string.bluetooth_is_on), true);
                } else {
                    ToastUtil.showToast(getString(R.string.bluetooth_is_off), true);
                }
                break;

            case REQUEST_SELECTE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
                    if (!"".equals(address)) {
                        myApp.setBluetoothAddress(address);
//                    txtAddress.setText(myApp.getBluetoothAddress());
                    }
                    txtCurrentDevice.setText(name + ": " + address);

                    if (!MyApplication.ADDRESS_NULL.equals(myApp.getBluetoothAddress())) {
                        myApp.getBluetoothService().startConnection(myApp.getBluetoothAddress());
                        btnStartConnection.setText(R.string.close_connection);
                    } else {
                        ToastUtil.showToast(getString(R.string.bluetooth_device_is_not_available), true);
                    }

                } else {
                    ToastUtil.showToast(getString(R.string.reselect_a_bluetooth_device_to_connect), true);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (D) {
            Log.e(TAG, "++++ ON CREATE ++++");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (D) {
            Log.e(TAG, "++++ ON CREATE VIEW ++++");
        }

        View view = inflater.inflate(R.layout.home_fragment_connection, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        if (D) {
            Log.e(TAG, "++++ ON CREATE VIEW ++++");
        }

        // ��ȡApplicationȫ�ֱ���
        myApp = (MyApplication) getActivity().getApplication();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ����豸�Ƿ���������豸
        if (mBluetoothAdapter == null) {
            ToastUtil.showToast(getString(R.string.bluetooth_is_not_supported), true);
            getActivity().finish();
            return;
        }

        // ��ʾѡ�����豸����
        if (mBluetoothAdapter.isEnabled() == false) {
            new Thread() {

                public void run() {
                    if (mBluetoothAdapter.isEnabled() == false) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
                    }
                }
            }.start();
        } else {
            if (mBluetoothService == null) {
                mBluetoothService = new BluetoothService(mHomeHandler);
                myApp.setBluetoothService(mBluetoothService);
            }

        }

//		// ����ʾֱ�ӿ����豸����
//		if (mBluetoothAdapter.isEnabled() == false) {
//			new Thread() {
//				public void run() {
//					mBluetoothAdapter.enable();
//				}
//			}.start();
//			MyToast.showToast("���ڴ�����...", true);
//		}

        if (mBluetoothService == null) {
            mBluetoothService = new BluetoothService(mHomeHandler);
            myApp.setBluetoothService(mBluetoothService);
            Log.e(TAG, "btService create");
        }
    }


    @Override
    public void onPause() {

        super.onPause();

        if (D) {
            Log.e(TAG, "++++ ON PAUSE ++++");
        }
    }

    @Override
    public void onStart() {

        super.onStart();

        myApp.getBluetoothService().setHandler(mHomeHandler);

        if (D) {
            Log.e(TAG, "++++ ON START ++++");
        }
    }


    @Override
    public void onResume() {

        super.onResume();

        if (D) {
            Log.e(TAG, "++++ ON RESUME ++++");
        }
    }


    @Override
    public void onStop() {

        super.onStop();

        if (D) {
            Log.e(TAG, "++++ ON STOP ++++");
        }
    }


    @Override
    public void onDestroy() {

        super.onDestroy();

        // �رշ������
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // ע��action������
        getActivity().unregisterReceiver(mReceiver);

        myApp.getBluetoothService().stopConnection();

        if (D) {
            Log.e(TAG, "++++ ON DESTROY ++++");
        }
    }

    /**
     * ��ʼ������豸����
     */
    private void doDiscovery() {
        if (D) {
            Log.d(TAG, "doDiscovery()");
        }

        // �ر����ڽ��еĲ��ҷ���
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //�����¿�ʼ
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * @param view
     */
    private void initViews(View view) {
        txtReceivedMessage = (TextView) view.findViewById(R.id.txtReceivedMessage);
        txtReceivedMessageNum = (TextView) view.findViewById(R.id.txtReceivedMessageNum);
        txtSentMessage = (TextView) view.findViewById(R.id.txtSentMessage);
        txtCurrentDevice = (TextView) view.findViewById(R.id.txtCurrentDevice);

        btnScan = (Button) view.findViewById(R.id.button_scan);
        btnScan.setOnClickListener(this);

        btnCancel = (Button) view.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        btnStartConnection = (Button) view.findViewById(R.id.btnStartConnection);
        btnStartConnection.setOnClickListener(this);

        mPairedDevicesArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.device_name);

        // ����������豸�б�     
        pairedListView = (ListView) view.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // ����δ����豸�б�
        newDevicesListView = (ListView) view.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // ע����ղ��ҵ��豸action������
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // ע����ҽ���action������
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        // �õ������������
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // �õ�����������豸�б�
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // ���������豸���б���ʾ 
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noPairedDevices = getString(R.string.no_paired_devices);
            mPairedDevicesArrayAdapter.add(noPairedDevices);
        }
    }

    //ѡ���豸��Ӧ����
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // ׼�������豸���رշ������
            mBluetoothAdapter.cancelDiscovery();
            myApp.getBluetoothService().stopConnection();
            // �õ�mac��ַ
            String info = ((TextView) v).getText().toString();
            String noPairedDevices = getString(R.string.no_paired_devices);
            String noNewDevices = getString(R.string.no_new_devices);
            if (!noPairedDevices.equals(info) && !noNewDevices.equals(info)) {
                String address = info.substring(info.length() - 17);
                String name = info.substring(0, info.length() - 18);

                if (!TextUtils.isEmpty(address)) {
                    myApp.setBluetoothAddress(address);
                    txtCurrentDevice.setText(name + " (" + address + " )");
                    myApp.getBluetoothService().startConnection(myApp.getBluetoothAddress());
                    Log.e(TAG, myApp.getBluetoothAddress());
                    if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
                        myApp.getBluetoothService().stopConnection();
                        btnStartConnection.setText(R.string.open_connection);
                    } else {
                        btnStartConnection.setText(R.string.close_connection);
                    }
                } else {
                    ToastUtil.showToast(getString(R.string.select_an_available_device), true);
                }
            }

        }
    };

    private boolean isDuplicate(String mString, ArrayAdapter<String> mArrayAdapter) {
        for (int i = 0; i < mArrayAdapter.getCount(); i++) {
            if (mString.equals(mArrayAdapter.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    // ���ҵ��豸���������action������
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // ���ҵ��豸action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // �õ������豸
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // ��ӵ����豸�б��У��������������
                    if (!isDuplicate(device.getName() + "\n" + device.getAddress(), mNewDevicesArrayAdapter)) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                } else {
                    // ��ӵ�������豸�б��������������
                    if (!isDuplicate(device.getName() + "\n" + device.getAddress(), mPairedDevicesArrayAdapter)) {
                        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }

                // �������action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    //�����µ������豸���
                    String noNewDevices = getString(R.string.no_new_devices);
                    mNewDevicesArrayAdapter.add(noNewDevices);
                }
                if (mPairedDevicesArrayAdapter.getCount() > 0) {
                    pairedListView.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private final Handler mHomeHandler = new Handler() {
        StringBuilder recDataString = new StringBuilder();
        int dataNumber = 0;

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_RECEIVED:
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    String dataInPrint = recDataString.toString(); // ��ȡ�ַ���
                    dataNumber++;
                    txtReceivedMessage.setText(dataInPrint);
                    txtReceivedMessageNum.setText(String.valueOf(dataNumber));
                    recDataString.delete(0, recDataString.length());
                    break;

                case BluetoothService.MESSAGE_SENT:
                    String writeMessage = msg.obj.toString();
                    txtSentMessage.setText(writeMessage.subSequence(0, writeMessage.length() - 1));
                    break;

                case BluetoothService.MESSAGE_CONNECT_RESULT:
                    ToastUtil.showToast(msg.obj.toString(), true);
                    break;

                default:
                    break;
            }
        }
    };
}
