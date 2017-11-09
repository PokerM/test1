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
 * 蓝牙服务类
 */
public class BluetoothService {

    // private static String address = "87:BA:58:D3:9C:5A"; //BILLHU COMPUTER
    // private static String address = "20:16:06:22:85:58"; //BLUETOOTH SJTU01
    // private static String address = "6E:95:38:2A:A9:D3"; //LZT COMPUTER
    // private static String address = "F4:8B:32:7E:D5:58"; //BILLHU CELLPHONE
    // private static String address = "20:16:06:23:05:67"; //BLUETOOTH SJTU00

    private static final String TAG = "BluetoothService"; // 定义蓝牙通讯服务的标签
    private static final boolean D = true; // 日志入口开关
    // private static final String NAME = "BluetoothService"; //定义蓝牙服务的名字
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 蓝牙的UUID号

    // 蓝牙连接当前状态类型
    public static final int STATE_NONE = 0; // 未连接蓝牙设备
    public static final int STATE_LISTEN = 1; // 未连接蓝牙设备
    public static final int STATE_CONNECTING = 2; // 启动外发的连接
    public static final int STATE_CONNECTED = 3; // 已连接蓝牙设备
    public static final int STATE_COMMUNICATING = 4; // 蓝牙设备正在通信中

    // handler消息类型
    public static final int MESSAGE_RECEIVED = 0;
    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_CONNECT_RESULT = 3;

    // 发送命令类型
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

    //发送指令
    public static final String HEARTBEAT = "11111"; // 没有任何操作时发送的指令
    public static final String START_NAVI = "10000"; // 启动导航指令
    public static final String STOP_NAVI = "20000"; // 停止导航指令
    public static final String TURN_RIGHT = "30001"; // 右转指令
	public static final String TURN_LEFT = "30002"; // 左转指令
    public static final String SET_A_REQUEST = "40003"; // 请求设置A点指令
    public static final String SET_A_AFFIRM = "40013"; // 确认收到B点指令
    public static final String SET_B_REQUEST = "40004"; // 请求设置A点指令
    public static final String SET_B_AFFIRM = "40014";  // 确认收到B点指令
    
    public static final String HEADLAND_POINT_1 = "50001";	// 请求设置地头点指令
    public static final String HEADLAND_POINT_2 = "50002";

    private BluetoothAdapter mAdapter; // 定义蓝牙适配器
    private Handler mHandler; // 定义处理器
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    // private AcceptThread mAcceptThread; //服务器侦听蓝牙连接线程
    // private ConnectThread mConnectThread; // 蓝牙连接线程
    private ReceivingThread mReceivingThread; // 接收数据线程
    private SendingThread mSendingThread; // 发送数据线程
    private int commandType;
    private int mState; // 蓝牙连接状态
    private String sendCommand; // 自定义发送指令


    /**
     * @param mHandler 消息处理器
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
     * 设置发送命令类型
     * 
     * @param commandType
     *            发送命令类型
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
     * 设置发送命令
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
     * 不作为服务器端侦听，所以用不上该函数
     */
    // public synchronized void start() {
    // if (D)
    // Log.e(TAG, "start");
    //
    // // 停止所有尝试蓝牙连接的线程
    // if (mConnectThread != null) {
    // mConnectThread.cancel();
    // mConnectThread = null;
    // }
    //
    // // 停止所有正在连接的线程
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
    // // 开始监听蓝牙服务套接字的线程
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
     * 方法名：connect(BluetoothDevice device) 功能：开始连接远程设备 参数：device - 要连接的蓝牙设备
     * 返回值：void
     */
    public synchronized void startConnection(String address) {
        if (D) Log.e(TAG, "startConnection");
        
        mDevice = mAdapter.getRemoteDevice(address);

        // 停止所有正在连接的线程
        if (mSendingThread != null) {
            mSendingThread.cancel();
            mSendingThread = null;
        }

        if (mReceivingThread != null) {
            mReceivingThread.cancel();
            mReceivingThread = null;
        }

//        // 开始连接所给的蓝牙设备的线程
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }

        // 设置蓝牙状态为正在连接设备
//        setState(STATE_CONNECTING);
//        mConnectThread = new ConnectThread(mDevice);
//        mConnectThread.start();
        
        BluetoothSocket tmp = null;
        // 获取连接给定蓝牙设备的套接字
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            setState(STATE_NONE);
            Log.e(TAG, "create() failed", e);
            String connectResult = "无法创建长连接！";
            mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
        }
        mSocket = tmp;

        // 关闭正在进行的查找服务以防降低连接速度
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }

        if (mSocket != null) {
            // 连接蓝牙套接字
            try {
                mSocket.connect();
                Log.e(TAG, mDevice.getName() + "已经成功连接！可以传送数据！");
                String connectResult = "已经成功连接!可以传送数据！";
                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                setState(STATE_CONNECTED);
            } catch (IOException connectException) {
                setState(STATE_NONE);
                Log.e(TAG, mDevice.getName() + "未连接成功！不能传送数据！");
                String connectResult = "未连接成功！不能传送数据！";
                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException closeException) {
                    Log.e(TAG, "unable to close() socket during connection failure", closeException);
                    Log.e(TAG, mDevice.getName() + "连接无法关闭！");
                }
                return;
            }
        }

        // 开启通信线程
        communicate();

    }

    /**
     * Stop all threads
     */
    /*
     * 方法名：stop() 功能：停止所有线程 参数：无 返回值：void
     */
    public synchronized void stopConnection() {
        if (D) Log.e(TAG, "stopConnection");

        // 先停止所有正在通信的线程
        if (mSendingThread != null) {
            mSendingThread.cancel();
            mSendingThread = null;
        }

        if (mReceivingThread != null) {
            mReceivingThread.cancel();
            mReceivingThread = null;
        }
        
//        // 再停止请求连接的线程
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
        
        // 关闭套接字
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        
        // // 由于仅需连接一台设备，停止其余连接线程
        // if (mAcceptThread != null) {
        // mAcceptThread.cancel();
        // mAcceptThread = null;
        // }
        setState(STATE_NONE);
    }
    
    private synchronized void communicate() {
        if (D) Log.e(TAG, "communicate");

        if (getState() == STATE_CONNECTED) {

//            // 开始连接所给的蓝牙设备的线程
//            if (mConnectThread != null) {
//                mConnectThread.cancel();
//                mConnectThread = null;
//            }

            // 停止所有正在通信的线程
            if (mSendingThread != null) {
                mSendingThread.cancel();
                mSendingThread = null;
            }

            if (mReceivingThread != null) {
                mReceivingThread.cancel();
                mReceivingThread = null;
            }

            // // 由于仅需连接一台设备，停止其余连接线程
            // if (mAcceptThread != null) {
            // mAcceptThread.cancel();
            // mAcceptThread = null;
            // }

            // 开始数据接收和发送线程
            mReceivingThread = new ReceivingThread();
            mReceivingThread.start();
            mSendingThread = new SendingThread();
            mSendingThread.start();

            // 设置蓝牙状态为通信中
            setState(STATE_COMMUNICATING);

            // 将已发送的信息分享给UI程序组件
            mHandler.obtainMessage(MESSAGE_DEVICE_NAME, -1, -1, mDevice.getName()).sendToTarget();
        } else {
        }

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted (or
     * until cancelled). 蓝牙服务器端侦听线程 此应用中Android蓝牙设备作为客户端，用不上该线程
     */
    // private class AcceptThread extends Thread {
    // // 本地服务套接字
    // private final BluetoothServerSocket mmServerSocket;
    //
    // public AcceptThread() {
    // BluetoothServerSocket tmp = null;
    //
    // // 新建一个监听服务套接字
    // try {
    // //开启监听
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
    // // 当蓝牙未连接时监听服务套接字
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
     * 方法名：connected(BluetoothSocket socket, BluetoothDevice device)
     * 功能：开始管理蓝牙连接的线程 参数：device - 已连接的蓝牙设备 socket - 蓝牙连接的套接字 返回值：void
     */
   

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    /*
     * 类名：ConnectThread 功能：当尝试连接外发连接设备时此线程运行
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
//            // 获取连接给定蓝牙设备的套接字
//            try {
//                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
//            } catch (IOException e) {
//                Log.e(TAG, "create() failed", e);
//                String connectResult = "无法创建长连接！";
//                mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                
//            }
//            mSocket = tmp;
//
//            // 关闭正在进行的查找服务以防降低连接速度
//            if (mAdapter.isDiscovering()) {
//                mAdapter.cancelDiscovery();
//            }
//
//            if (mSocket != null) {
//                // 连接蓝牙套接字
//                try {
//                    mSocket.connect();
//                    Log.e(TAG, mDevice.getName() + "已经成功连接！可以传送数据！");
//                    String connectResult = "已经成功连接!可以传送数据！";
//                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                    setState(STATE_CONNECTED);
//                } catch (IOException connectException) {
//                    Log.e(TAG, mDevice.getName() + "未连接成功！不能传送数据！");
//                    String connectResult = "未连接成功！不能传送数据！";
//                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
//                    try {
//                        mSocket.close();
//                        mSocket = null;
//                    } catch (IOException closeException) {
//                        Log.e(TAG, "unable to close() socket during connection failure", closeException);
//                        Log.e(TAG, mDevice.getName() + "连接无法关闭！");
//                    }
//                    return;
//                }
//            }
//
//            // 重新设置蓝牙连接线程
//            synchronized (BluetoothService.this) {
//                mConnectThread = null;
//            }
//
//            // 开启通信线程
//            communicate();
//
//        }
//
//        /**
//         * 取消线程
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
        private boolean isCancel = false; // 线程是否取消标志
        private final InputStream mmInStream; // 蓝牙接收数据字节流

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

            if(D) Log.e(TAG, "接收数据线程已经启动！");

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
                     * 接收数据解包协议
                     */

                    /*
                     * 保证接收字符串以“#”开头，如果不是则删除“#”前面字符 例如“9*#01”变为“#012”
                     * （如果开启线程收到的第一个数据丢失"*"，则忽略不处理，例如收到"#12345#12345*"，不要 
                     *  使用if (readMsg.ndexOf("#") > 0) { readMsg.delete(0, readMsg.lastIndexOf("#")); } 删除，
                     *  否则会丢失数据，例如收到"#12345*#12345*"。）
                     */
                    if (readMsg.indexOf("#") > 0) {
                        readMsg.delete(0, readMsg.indexOf("#"));
                    }

                    /*
                     * 删除“*”在“#”之前的字符串中的多余字符串
                     * 例如“6789*#0123456789*”变为“#0123456789*”
                     */
                    if (readMsg.indexOf("*") >= 0 && readMsg.indexOf("*") < readMsg.indexOf("#")) {
                        readMsg.delete(0, readMsg.indexOf("*") + 1);
                    }

                    /*
                     * 循环读取字符串，并取出“#”和“*”之间的数据
                     */
                    while (readMsg.indexOf("#") >= 0 && readMsg.indexOf("*") > 0
                            && readMsg.indexOf("#") < readMsg.indexOf("*")) {
                        mHandler.obtainMessage(MESSAGE_RECEIVED, bytes, -1,
                                readMsg.substring(readMsg.indexOf("#"), readMsg.indexOf("*") + 1)).sendToTarget();
                        readMsg.delete(readMsg.indexOf("#"), readMsg.indexOf("*") + 1);
                    }

                    /*
                     * 保证处理后剩余字符串以“#”开头，如果不是则删除“#”前面字符 例如“9*#01”变为“#012”
                     */
                    if (readMsg.indexOf("#") > 0) {
                        readMsg.delete(0, readMsg.indexOf("#"));
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);  
                    String connectResult = "接收数据通信已断开！";
                    mHandler.obtainMessage(MESSAGE_CONNECT_RESULT, -1, -1, connectResult).sendToTarget();
                }
            }
        }

        /**
         * 取消线程
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
        private boolean isCancel = false; // 线程是否取消标志
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
            if(D) Log.e(TAG, "发送数据线程已经启动！");

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

                    // 将已发送的信息分享给UI程序组件
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
         * 取消线程
         */
        private void cancel() {
            isCancel = true;
        }

    }

    /**
     * Write data to outputStream via bluetooth socket. 发送指定字符串
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
