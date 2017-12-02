package sjtu.me.tractor.tractorinfo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.utils.AlertDialogUtil;

/**
 * @author BillHu ������ϢFragment��ͼ
 */
public class TractorSettingFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

    public static final String TAG = "TractorSettingFragment";
    public static final boolean D = true;
    private static final int LOADER_ID = 10;
    private static final String QUERY_ALL = "%%";

    private static MyApplication mApp;
    private LoaderManager loaderManager;
    private Loader<Cursor> loader;
    private List<Map<String, String>> tractorList;

    private ListView lstTractor;
    private TractorListAdapter tractorListAdapter;

    private LinearLayout layoutAttributeCollections;

    public TractorSettingFragment() {
        super();
    }

    // �����첽loaderʵ��ʵʱ��أ�ע��һ��Ҫ�Ǿ�̬��
    public static class MyAsyncLoader extends AsyncTaskLoader<Cursor> {
        public MyAsyncLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (takeContentChanged()) {
                forceLoad();
            }
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = mApp.getDatabaseManager().queryTractorByName(QUERY_ALL);
            return cursor;
        }

    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = ((TextView) view.findViewById(R.id.cellTextTractorName)).getText().toString();
            Cursor cursor = mApp.getDatabaseManager().queryTractorByName(name);
            List<Map<String, String>> list = DatabaseManager.cursorToList(cursor);
            if (list != null && list.size() != 0) {
                Map<String, String> map = list.get(0);
                ((TextView) getActivity().findViewById(R.id.txtName)).setText(name);
                ((TextView) getActivity().findViewById(R.id.txtWheelbase)).setText(map.get(TractorInfo.TRACTOR_WHEELBASE) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtLateral)).setText(map.get(TractorInfo.TRACTOR_ANTENNA_LATERAL) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtRear)).setText(map.get(TractorInfo.TRACTOR_ANTENNA_REAR) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtHeight)).setText(map.get(TractorInfo.TRACTOR_ANTENNA_HEIGHT) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtRadius)).setText(map.get(TractorInfo.TRACTOR_MIN_TURNING_RADIUS) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtAngular)).setText(map.get(TractorInfo.TRACTOR_ANGLE_CORRECTION));
                ((TextView) getActivity().findViewById(R.id.txtWidth)).setText(map.get(TractorInfo.TRACTOR_IMPLEMENT_WIDTH) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtLength)).setText(map.get(TractorInfo.TRACTOR_IMPLEMENT_LENGTH) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtOffset)).setText(map.get(TractorInfo.TRACTOR_IMPLEMENT_OFFSET) + getString(R.string.unit_meter));
                ((TextView) getActivity().findViewById(R.id.txtSpace)).setText(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING) + getString(R.string.unit_meter));
            }
        }
    };

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
        View view = inflater.inflate(R.layout.home_fragment_tractor_setting, container, false);
        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) {
            Log.e(TAG, "++++ ON ACTIVITY CREATE ++++");
        }

       /* ��ȡApplicationȫ�ֱ���*/
        mApp = (MyApplication) getActivity().getApplication();

        loader = new MyAsyncLoader(this.getActivity());
        loaderManager = getActivity().getLoaderManager();
        Log.e(TAG, "is loaderManager not null? " + (loaderManager != null));
        loaderManager.initLoader(LOADER_ID, null, this);

        /*�������ݿ������һ��Ĭ�ϵĳ���*/
        Cursor cursor = mApp.getDatabaseManager().getTractorsNameSet();
        List<Map<String, String>> list = DatabaseManager.cursorToList(cursor);
        final String SJTU = "SJTU01";
        boolean flag = false;
        for (Map<String, String> map : list) {
            if (SJTU.equals(map.get(TractorInfo.TRACTOR_NAME))) {
                flag = true;
            }
        }
        if (!flag) {
            String[] tractor = {SJTU, "����ʽ", "����", "700", "2.20", "0.95", "-0.15", "2.12", "2.0", "0.05", "2.50", "0.0", "0.85", "2.50"};
            mApp.getDatabaseManager().insertDataToTractor(tractor);
        }

        /*��������*/
        notifyDataChange();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) {
            Log.e(TAG, "++++ ON START ++++");
        }

        // ��������
        notifyDataChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) {
            Log.e(TAG, "++++ ON RESUME ++++");
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
    public void onStop() {
        super.onStop();
        if (D) {
            Log.e(TAG, "++++ ON STOP ++++");
        }
    }

    @Override
    public void onDestroy() {
        if (D) {
            Log.e(TAG, "++++ ON DESTROY ++++");
        }
        mApp.getDatabaseManager().releaseDataBase();
        super.onDestroy();
    }

    private void initViews(View v) {
        lstTractor = (ListView) v.findViewById(R.id.lstTractor);
        lstTractor.setOnCreateContextMenuListener(longPressListener);
        v.findViewById(R.id.btnAddTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnEditTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnRemoveTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnClearTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnSaveTractorInfo).setOnClickListener(this);
        layoutAttributeCollections = ((LinearLayout) v.findViewById(R.id.layoutAttributeCollections));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddTractorInfo:
                AlertDialog dialogAdd = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.alert_dialog_title_in_tractorinfo))
                        .setMessage(getString(R.string.alert_dialog_message_add_tractor))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(getString(R.string.affirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent("sjtu.me.tractor.tractorinfo.TractorAddingActivity"));
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .create();
                dialogAdd.show();
                AlertDialogUtil.changeDialogTheme(dialogAdd);
                break;

            case R.id.btnRemoveTractorInfo:
            case R.id.btnEditTractorInfo:
                AlertDialog dialogEdit = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.alert_dialog_title_in_tractorinfo))
                        .setMessage(getString(R.string.alert_dialog_message_edit_remove))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(getString(R.string.affirm), null).create();
                dialogEdit.show();
                AlertDialogUtil.changeDialogTheme(dialogEdit);
                break;

            case R.id.btnClearTractorInfo:
                AlertDialog dialogClear = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.alert_dialog_title_in_tractorinfo))
                        .setMessage(getString(R.string.alert_dialog_message_clear_all))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton("ȷ��ɾ��", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                AlertDialog dialogClearAgain = new AlertDialog.Builder(TractorSettingFragment.this.getActivity())
                                        .setTitle(getString(R.string.alert_title_affirm))
                                        .setMessage(getString(R.string.alert_info_clear_all_tractors_affirm))
                                        .setIcon(R.drawable.alert)
                                        .setPositiveButton("�ٴ�ȷ��ɾ��", new android.content.DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mApp.getDatabaseManager().clearAllTractorData();
                                                notifyDataChange();
                                            }
                                        }).setNegativeButton("ȡ��ɾ��", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create();
                                dialogClearAgain.show();
                                AlertDialogUtil.changeDialogTheme(dialogClearAgain);
                            }
                        }).setNegativeButton("ȡ��ɾ��", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();

                dialogClear.show();
                AlertDialogUtil.changeDialogTheme(dialogClear);
                break;

            case R.id.btnSaveTractorInfo:
                AlertDialog saveDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.alert_dialog_title_in_tractorinfo))
                        .setMessage(getString(R.string.alert_message_save_tractorinfo))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(getString(R.string.affirm), null)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                saveDialog.show();
                AlertDialogUtil.changeDialogTheme(saveDialog);
                break;

            default:
                break;
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (D) {
            Log.e(TAG, "++++ ON CREATE LOADER ++++");
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (D) {
            Log.e(TAG, "++++ ON LOAD FINISHED ++++");
        }

        tractorList = updateListData(data);
        if (tractorList != null) {
            layoutAttributeCollections.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "cursor is null");
        }

        tractorListAdapter = new TractorListAdapter(getActivity(), tractorList);
        lstTractor.setAdapter(tractorListAdapter);
        lstTractor.setOnItemClickListener(itemClickListener);
        tractorListAdapter.notifyDataSetChanged();
//        // ÿ��loader���֮�������ͷ�database��Դ�Լ������ݿ�ѹ��
//        mApp.getDatabaseManager().releaseDataBase();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (D) {
            Log.e(TAG, "++++ ON LOAD RESET ++++");
        }
    }

    public List<Map<String, String>> updateListData(Cursor cursor) {
        return DatabaseManager.cursorToList(cursor);
    }

    public void notifyDataChange() {
        loaderManager.getLoader(LOADER_ID).onContentChanged();
    }

    // ����ɾ�����ݼ���������д��������
    OnCreateContextMenuListener longPressListener = new OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            // ���ô�����ʾ��info���Ա�ʾ��ʱ����Ĳ˵���λ�ã��ݴ˿��Բ�ѯ���ݿ��Ӧ���ֵĳ�����Ϣ
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            AlertDialog.Builder longPressAlertBuilder = new AlertDialog.Builder(getActivity());
            longPressAlertBuilder.setTitle(getString(R.string.alert_title_tractor_long_press));
            longPressAlertBuilder.setIcon(R.drawable.alert);

            // ���������Ի�ȡѡ�е���Ŀ�ĳ��������ƣ����ɽ�����һ������
            int position = info.position;
            Map<String, String> map = tractorList.get(position);
            final String name = map.get("tName");

            longPressAlertBuilder.setPositiveButton(getString(R.string.delete_tractor),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mApp.getDatabaseManager().deleteTractorByName(name);
                            notifyDataChange();
                        }
                    });

            longPressAlertBuilder.setNegativeButton(getString(R.string.tractor_info_edit_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            longPressAlertBuilder.setNeutralButton(getString(R.string.tractor_info_edit),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ��ѡ���޸ģ��򷵻��޸�ҳ�沢������������

                            Cursor resultCursor = mApp.getDatabaseManager().queryTractorByName(name);
                            Map<String, String> map = DatabaseManager.cursorToMap(resultCursor);
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), TractorAddingActivity.class);
                            for (String key : map.keySet()) {
                                intent.putExtra(key, map.get(key));
                            }
                            startActivity(intent);
                        }
                    });

            String message = getString(R.string.alert_msg_tractor_info_long_press_1) + map.get(TractorInfo.TRACTOR_NAME).toString()
                    + getString(R.string.alert_msg_tractor_info_long_press_2);
            longPressAlertBuilder.setMessage(message);
            longPressAlertBuilder.show();
        }

    };
}
