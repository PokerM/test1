package sjtu.me.tractor.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * @author billhu
 * ����������
 */
public class BluetoothService {

    // private static String address = "87:BA:58:D3:9C:5A"; //BILLHU COMPUTER
    // private static String address = "20:16:06:22:85:58"; //BLUETOOTH SJTU01
    // private static String address = "6E:95:38:2A:A9:D3"; //LZT COMPUTER
    // private static String address = "F4:8B:32:7E:D5:58"; //BILLHU CELLPHONE
    // private static String address = "20:16:06:23:05:67"; //BLUETOOTH SJTU00

    private static final String TAG = "BluetoothService"; // ��������ͨѶ����ı�ǩ
    private static final boolean D = true; // ��־��ڿ���
    // private static final String NAME = "BluetoothService"; //�����������������
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // ������UUID��

    // �������ӵ�ǰ״̬����
    public static final int STATE_NONE = 0; // δ���������豸
    public static final int STATE_LISTEN = 1; // δ���������豸
    public static final int STATE_CONNECTING = 2; // �����ⷢ������
    public static final int STATE_CONNECTED = 3; // �����������豸
    public static final int STATE_COMMUNICATING = 4; // �����豸����ͨ����

    // handler��Ϣ����
    public static final int MESSAGE_RECEIVED = 0;
    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_CONNECT_RESULT = 3;

    // ������������
    public static final int COMMAND_SET_A_REQUEST = 1;
    public static final int COMMAND_SET_A_AFFIRM = 2;
    public static final int COMMAND_SET_B_REQUEST = 3;
    public static final int COMMAND_SET_B_AFFIRM = 4;
    public static final int COMMAND_TURN_LEFT = 5;
    public static final int COMMAND_TURN_RIGHT = 6;
    public static final int COMMAND_START_NAVI = 7;
    public static final int COMMAND_STOP_NAVI = 8;
    public static final int COMMAND_SPECIFIED = 20;
    public static final int COMMAND_DEFAULT = 30;
    
    public static final int COMMAND_HEADLAND_POINT_1 = 9;
    public static final int COMMAND_HEADLAND_POINT_2 = 10;

    //����ָ��
    public static final String HEARTBEAT = "11111"; // û���κβ���ʱ���͵�ָ��
    public static final String START_NAVI = "10000"; // ��������ָ��
    public static final String STOP_NAVI = "20000"; // ֹͣ����ָ��
    public static final String TURN_RIGHT = "30001"; // ��תָ��
	public static final String TURN_LEFT = "30002"; // ��תָ��
    public static final String SET_A_REQUEST = "40003"; // ��������A��ָ��
    public static final String SET_A_AFFIRM = "40013"; // ȷ���յ�B��ָ��
    public static final String SET_B_REQUEST = "40004"; // ��������A��ָ��
    public static final String SET_B_AFFIRM = "40014";  // ȷ���յ�B��ָ��
    
    public static final String HEADLAND_POINT_1 = "50001";	// �������õ�ͷ��ָ��
    public static final String HEADLAND_POINT_2 = "50002";

    private BluetoothAdapter mAdapter; // ��������������
    private Handler mHandler; // ���崦����
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    // private AcceptThread mAcceptThread; //�������������������߳�
    // private ConnectThread mConnectThread; // ���������߳�
    private ReceivingThread mReceivingThread; // ���������߳�
    private SendingThread mSendingThread; // ���������߳�
    private int commandType;
    private int mState; // ��������״̬
    private String sendCommand; // �Զ��巢��ָ��


    /**
     * @param mHandler ��Ϣ������
     */
    public BluetoothService(Handler mHandler) {
        this.mHandler = mHandler;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
    }
    
    /**
     * @return
     */
    public Handler getHandler() {
		return mHandler;
	}

	/**
	 * @param mHandler
	 */
	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

    /**
     * @return
     */
    public int getCommandType() {
        return commandType;
    }

    /**
     * ���÷�����������
     * 
     * @param commandType
     *            ������������
     */
    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    /**
     * @return
     */
    public synchronized String getSendCommand() {
        return sendCommand;
    }

    /**
     * ���÷�������
     * 
     * @param sendCommand
     */
    public synchronized void setSendCommand(String sendCommand) {
        setCommandType(COMMAND_SPECIFIED);
        this.sendCommand = sendCommand;
    }

    /**
     * @return
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * @param state
     */
    private synchronized void setState(int state) {
        this.mState = state;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume().
     * ����Ϊ�������������������ò��ϸú���
     */
    // public synchronized void start() {
    // if (D)
    // Log.e(TAG, "start");
    //
    // // ֹͣ���г����������ӵ��߳�
    // if (mConnectThread != null) {
    // mConnectThread.cancel();
    // mConnectThread = null;
    // }
    //
    // // ֹͣ�����������ӵ��߳�
    // if (mSendingThread != null) {
    // mSendingThread.cancel();
    // mSendingThread = null;
    // }
    //
    // if (mReceivingThread != null) {
    // mReceivingThread.cancel();
    // mReceivingThread = null;
    // }
    //
    // // ��ʼ�������������׽��ֵ��߳�
    // if (mAcceptThread == null) {
    // mAcceptThread = new AcceptThread();
    // mAcceptThread.start();
    // }
    //
    // setState(STATE_LISTEN);
    // }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device
     *            The BluetoothDevice to connect
     */
    /*
     * ��������connect(BluetoothDevice device) ���ܣ���ʼ����Զ���豸 ������device - Ҫ���ӵ������豸
     * ����ֵ��void
     */
    public synchronized void startConnection(String address) {
        if (D) Log.e(TAG, "startConnection");
        
        mDevice = mAdapter.getRemoteDevice(address);

        // ֹͣ�����������ӵ��߳�
        if (mSendingThread != null) {
            mSendingThread.cancel();
            mSendingThread = null;
        }

        if (mReceivingThread != null) {
            mReceivingThread.cancel();
            mReceivingThread = null;
        }

//        // ��ʼ���������������豸���߳�
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }

        // ��������״̬Ϊ���������豸
//        setState(STATE_CONNECTING);
//        mConnectThread = new ConnectThread(mDevice);
//        mConnectThread.start();
        
        BluetoothSocket tmp = null;
        // ��ȡ���Ӹ��������豸���׽���
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            setState(STATE_NONE);
            Log.e(TAG, "create() failed", e);
            String connectResult = "�޷����������ӣ�";
            mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
        }
        mSocket = tmp;

        // �ر����ڽ��еĲ��ҷ����Է����������ٶ�
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }

        if (mSocket != null) {
            // ���������׽���
            try {
                mSocket.connect();
                Log.e(TAG, mDevice.getName() + "�Ѿ��ɹ����ӣ����Դ������ݣ�");
                String connectResult = "�Ѿ��ɹ�����!���Դ������ݣ�";
                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                setState(STATE_CONNECTED);
            } catch (IOException connectException) {
                setState(STATE_NONE);
                Log.e(TAG, mDevice.getName() + "δ���ӳɹ������ܴ������ݣ�");
                String connectResult = "δ���ӳɹ������ܴ������ݣ�";
                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException closeException) {
                    Log.e(TAG, "unable to close() socket during connection failure", closeException);
                    Log.e(TAG, mDevice.getName() + "�����޷��رգ�");
                }
                return;
            }
        }

        // ����ͨ���߳�
        communicate();

    }

    /**
     * Stop all threads
     */
    /*
     * ��������stop() ���ܣ�ֹͣ�����߳� �������� ����ֵ��void
     */
    public synchronized void stopConnection() {
        if (D) Log.e(TAG, "stopConnection");

        // ��ֹͣ��������ͨ�ŵ��߳�
        if (mSendingThread != null) {
            mSendingThread.cancel();
            mSendingThread = null;
        }

        if (mReceivingThread != null) {
            mReceivingThread.cancel();
            mReceivingThread = null;
        }
        
//        // ��ֹͣ�������ӵ��߳�
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
        
        // �ر��׽���
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        
        // // ���ڽ�������һ̨�豸��ֹͣ���������߳�
        // if (mAcceptThread != null) {
        // mAcceptThread.cancel();
        // mAcceptThread = null;
        // }
        setState(STATE_NONE);
    }
    
    private synchronized void communicate() {
        if (D) Log.e(TAG, "communicate");

        if (getState() == STATE_CONNECTED) {

//            // ��ʼ���������������豸���߳�
//            if (mConnectThread != null) {
//                mConnectThread.cancel();
//                mConnectThread = null;
//            }

            // ֹͣ��������ͨ�ŵ��߳�
            if (mSendingThread != null) {
                mSendingThread.cancel();
                mSendingThread = null;
            }

            if (mReceivingThread != null) {
                mReceivingThread.cancel();
                mReceivingThread = null;
            }

            // // ���ڽ�������һ̨�豸��ֹͣ���������߳�
            // if (mAcceptThread != null) {
            // mAcceptThread.cancel();
            // mAcceptThread = null;
            // }

            // ��ʼ���ݽ��պͷ����߳�
            mReceivingThread = new ReceivingThread();
            mReceivingThread.start();
            mSendingThread = new SendingThread();
            mSendingThread.start();

            // ��������״̬Ϊͨ����
            setState(STATE_COMMUNICATING);

            // ���ѷ��͵���Ϣ�����UI�������
            mHandler.obtainMessage(MESSAGE_DEVICE_NAME, -1, -1, mDevice.getName()).sendToTarget();
        } else {
        }

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted (or
     * until cancelled). �����������������߳� ��Ӧ����Android�����豸��Ϊ�ͻ��ˣ��ò��ϸ��߳�
     */
    // private class AcceptThread extends Thread {
    // // ���ط����׽���
    // private final BluetoothServerSocket mmServerSocket;
    //
    // public AcceptThread() {
    // BluetoothServerSocket tmp = null;
    //
    // // �½�һ�����������׽���
    // try {
    // //��������
    // tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
    // } catch (IOException e) {
    // Log.e(TAG, "listen() failed", e);
    // }
    // mmServerSocket = tmp;
    // }
    //
    // public void run() {
    // if (D) Log.e(TAG, "BEGIN mAcceptThread" + this);
    // setName("AcceptThread");
    // BluetoothSocket socket = null;
    //
    // // ������δ����ʱ���������׽���
    // while (mState != STATE_CONNECTED) {
    // try {
    // // This is a blocking call and will only return on a
    // // successful connection or an exception
    // socket = mmServerSocket.accept();
    // } catch (IOException e) {
    // Log.e(TAG, "accept() failed", e);
    // break;
    // }
    //
    // // If a connection was accepted
    // if (socket != null) {
    // synchronized (BluetoothService.this) {
    // switch (mState) {
    // case STATE_LISTEN:
    // case STATE_CONNECTING:
    // // Situation normal. Start the connected thread.
    //// connected(socket, socket.getRemoteDevice());
    // break;
    // case STATE_NONE:
    // case STATE_CONNECTED:
    // // Either not ready or already connected. Terminate new socket.
    // try {
    // socket.close();
    // } catch (IOException e) {
    // Log.e(TAG, "Could not close unwanted socket", e);
    // }
    // break;
    // }
    // }
    // }
    // }
    // if (D) Log.i(TAG, "END mAcceptThread");
    // }
    //
    // public void cancel() {
    // if (D) Log.e(TAG, "cancel " + this);
    // try {
    // mmServerSocket.close();
    // } catch (IOException e) {
    // Log.e(TAG, "close() of server failed", e);
    // }
    // }
    //
    // }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * 
     * @param socket
     *            The BluetoothSocket on which the connection was made
     * @param device
     *            The BluetoothDevice that has been connected
     */
    /*
     * ��������connected(BluetoothSocket socket, BluetoothDevice device)
     * ���ܣ���ʼ�����������ӵ��߳� ������device - �����ӵ������豸 socket - �������ӵ��׽��� ����ֵ��void
     */
   

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    /*
     * ������ConnectThread ���ܣ������������ⷢ�����豸ʱ���߳�����
     */
//    private class ConnectThread extends Thread {
//        private final BluetoothDevice mmDevice;
//
//        public ConnectThread(BluetoothDevice device) {
//            Log.e(TAG, "*** create a ConnectThread ***");
//            
//            mmDevice = device;
//        }
//
//        public void run() {
//            BluetoothSocket tmp = null;
//            // ��ȡ���Ӹ��������豸���׽���
//            try {
//                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
//            } catch (IOException e) {
//                Log.e(TAG, "create() failed", e);
//                String connectResult = "�޷����������ӣ�";
//                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                
//            }
//            mSocket = tmp;
//
//            // �ر����ڽ��еĲ��ҷ����Է����������ٶ�
//            if (mAdapter.isDiscovering()) {
//                mAdapter.cancelDiscovery();
//            }
//
//            if (mSocket != null) {
//                // ���������׽���
//                try {
//                    mSocket.connect();
//                    Log.e(TAG, mDevice.getName() + "�Ѿ��ɹ����ӣ����Դ������ݣ�");
//                    String connectResult = "�Ѿ��ɹ�����!���Դ������ݣ�";
//                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                    setState(STATE_CONNECTED);
//                } catch (IOException connectException) {
//                    Log.e(TAG, mDevice.getName() + "δ���ӳɹ������ܴ������ݣ�");
//                    String connectResult = "δ���ӳɹ������ܴ������ݣ�";
//                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                    try {
//                        mSocket.close();
//                        mSocket = null;
//                    } catch (IOException closeException) {
//                        Log.e(TAG, "unable to close() socket during connection failure", closeException);
//                        Log.e(TAG, mDevice.getName() + "�����޷��رգ�");
//                    }
//                    return;
//                }
//            }
//
//            // �����������������߳�
//            synchronized (BluetoothService.this) {
//                mConnectThread = null;
//            }
//
//            // ����ͨ���߳�
//            communicate();
//
//        }
//
//        /**
//         * ȡ���߳�
//         */
//        public void cancel() {
//            if (mSocket != null) {
//                try {
//                    mSocket.close();
//                    mSocket = null;
//                } catch (IOException e) {
//                    Log.e(TAG, "close() of connect socket failed", e);
//                }
//            }
//        }
//
//    }

    /**
     * @author billhu Thread for keep receiving data. This thread runs during a
     *         connection with a remote device. It handles all incoming and
     *         outgoing transmissions.
     */
    private class ReceivingThread extends Thread {
        private boolean isCancel = false; // �߳��Ƿ�ȡ����־
        private final InputStream mmInStream; // �������������ֽ���

        public ReceivingThread() {
            if(D) Log.e(TAG, "*** create a ReceivingThread ***");
            
            InputStream tmpIn = null;
            try {
                // create I/O streams for receiving data
                tmpIn = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "ReceivingThread cannot get the InputStream!");
            }
            mmInStream = tmpIn;
        }

        public void run() {

            if(D) Log.e(TAG, "���������߳��Ѿ�������");

            byte[] readBuffer = new byte[1024];
            int bytes = 0;
            StringBuilder readMsg = new StringBuilder();

            // keep looping to listen for received message
            while (!isCancel) {
                try {
                    for (int i = 0; i < 1; i++) {
                        bytes = mmInStream.read(readBuffer); // read bytes from
                                                             // input buffer
                        String readMessage = new String(readBuffer, 0, bytes);
                        readMsg = readMsg.append(readMessage);
                    }

//                     Log.e("received_data", readMsg.toString());

                    /*
                     * �������ݽ��Э��
                     */

                    /*
                     * ��֤�����ַ����ԡ�#����ͷ�����������ɾ����#��ǰ���ַ� ���硰9*#01����Ϊ��#012��
                     * ����������߳��յ��ĵ�һ�����ݶ�ʧ"*"������Բ����������յ�"#12345#12345*"����Ҫ 
                     *  ʹ��if (readMsg.ndexOf("#") > 0) { readMsg.delete(0, readMsg.lastIndexOf("#")); } ɾ����
                     *  ����ᶪʧ���ݣ������յ�"#12345*#12345*"����
                     */
                    if (readMsg.indexOf("#") > 0) {
                        readMsg.delete(0, readMsg.indexOf("#"));
                    }

                    /*
                     * ɾ����*���ڡ�#��֮ǰ���ַ����еĶ����ַ���
                     * ���硰6789*#0123456789*����Ϊ��#0123456789*��
                     */
                    if (readMsg.indexOf("*") >= 0 && readMsg.indexOf("*") < readMsg.indexOf("#")) {
                        readMsg.delete(0, readMsg.indexOf("*") + 1);
                    }

                    /*
                     * ѭ����ȡ�ַ�������ȡ����#���͡�*��֮�������
                     */
                    while (readMsg.indexOf("#") >= 0 && readMsg.indexOf("*") > 0
                            && readMsg.indexOf("#") < readMsg.indexOf("*")) {
                        mHandler.obtainMessage(MESSAGE_RECEIVED, bytes, -1,
                                readMsg.substring(readMsg.indexOf("#"), readMsg.indexOf("*") + 1)).sendToTarget();
                        readMsg.delete(readMsg.indexOf("#"), readMsg.indexOf("*") + 1);
                    }

                    /*
                     * ��֤�����ʣ���ַ����ԡ�#����ͷ�����������ɾ����#��ǰ���ַ� ���硰9*#01����Ϊ��#012��
                     */
                    if (readMsg.indexOf("#") > 0) {
                        readMsg.delete(0, readMsg.indexOf("#"));
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);  
                    String connectResult = "��������ͨ���ѶϿ���";
                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                }
            }
        }

        /**
         * ȡ���߳�
         */
        private void cancel() {
            isCancel = true;
        }

    }

    /**
     * @author billhu Thread for keeping sending data out. This thread runs
     *         during a connection with a remote device. It handles all outgoing
     *         transmissions.
     */
    private class SendingThread extends Thread {
        private boolean isCancel = false; // �߳��Ƿ�ȡ����־
        private final OutputStream mmOutStream;

        public SendingThread() {
            Log.e(TAG, "*** create a SendingThread ***");

            OutputStream tmpOut = null;
            try {
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "SendingThread cannot get the OuputStream!");
            }
            mmOutStream = tmpOut;
        }

        public void run() {
            if(D) Log.e(TAG, "���������߳��Ѿ�������");

            String sendingMessage;
            byte[] writeBuffer;

            while (!isCancel) {

                switch (commandType) {
                case COMMAND_START_NAVI:
                    sendingMessage = "#0," + START_NAVI + ",*\n";
                    break;
                    
                case COMMAND_STOP_NAVI:
                    sendingMessage = "#0," + STOP_NAVI + ",*\n";
                    break;
                    
                case COMMAND_SET_A_REQUEST:
                    sendingMessage = "#0," + SET_A_REQUEST + ",*\n";
                    break;
                    
                case COMMAND_SET_A_AFFIRM:
                    sendingMessage = "#0," + SET_A_AFFIRM + ",*\n";
                    break;
                    
                case COMMAND_SET_B_REQUEST:
                    sendingMessage = "#0," + SET_B_REQUEST + ",*\n";
                    break;
                    
                case COMMAND_SET_B_AFFIRM:
                    sendingMessage = "#0," + SET_B_AFFIRM + ",*\n";
                    break;
                    
                case COMMAND_TURN_LEFT:
                	sendingMessage = "#0," + TURN_LEFT + ",*\n";
                	break;
                	
                case COMMAND_TURN_RIGHT:
                	sendingMessage = "#0," + TURN_RIGHT + ",*\n";
                	break;
                	
                case COMMAND_HEADLAND_POINT_1:
                	sendingMessage = "#0," + HEADLAND_POINT_1 + ",*\n";
                	break;
                	
                case COMMAND_HEADLAND_POINT_2:
                	sendingMessage = "#0," + HEADLAND_POINT_2 + ",*\n";
                	break;

                case COMMAND_SPECIFIED:
                    sendingMessage = "#0," + sendCommand + ",*\n";
                    break;

                default:
                    sendingMessage = "#0," + HEARTBEAT + ",*\n";
                    break;
                }

                writeBuffer = sendingMessage.getBytes();

                long start = System.currentTimeMillis();

                try {
                    // mmOutStream.flush();
                    mmOutStream.write(writeBuffer);
                    mmOutStream.flush();

                    // ���ѷ��͵���Ϣ�����UI�������
                    mHandler.obtainMessage(MESSAGE_SENT, -1, -1, sendingMessage).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Writing Exception!");
                }

                long end = System.currentTimeMillis();

                if (end - start < 250) {
                    try {
                        Thread.sleep(250 - end + start);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * ȡ���߳�
         */
        private void cancel() {
            isCancel = true;
        }

    }

    /**
     * Write data to outputStream via bluetooth socket. ����ָ���ַ���
     * 
     * @param str
     */
    public void writeOutputStream(String str) {
        OutputStream outStream;
        String message;
        byte[] msgBuffer;
        if (mSocket != null) {
            try {
                outStream = mSocket.getOutputStream();
                message = str;
                msgBuffer = message.getBytes();
                try {
                    outStream.write(msgBuffer);
                } catch (IOException e) {
                    Log.e(TAG, "ON RESUME: Exception during write.", e);
                }
            } catch (IOException e) {
                Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
            }
        }
    }

}
