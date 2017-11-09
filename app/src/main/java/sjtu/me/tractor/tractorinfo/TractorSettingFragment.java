package sjtu.me.tractor.tractorinfo;

import java.util.List;
import java.util.Map;

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
import android.graphics.Color;
import android.icu.text.DateFormat.Field;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.AlertDialogUtil;

/**
 * @author BillHu ����Fragment��ͼ
 */
public class TractorSettingFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

    public static final String TAG = "TractorSettingFragment";
    public static final boolean D = true;

    private static MyApplication mApp;
    private LoaderManager loaderManager;
    private Loader<Cursor> loader;
    private List<Map<String, String>> tractorList;
    private TractorListAdapter tractorListAdapter;
    private ListView lstTractor;

    public TractorSettingFragment() {
        super();
        // TODO Auto-generated constructor stub
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

        // ��ȡApplicationȫ�ֱ���
        mApp = (MyApplication) getActivity().getApplication();

        loader = new MyAsyncLoader(this.getActivity());
        loaderManager = getActivity().getLoaderManager();
        Log.e(TAG, "is loaderManager null ? " + (loaderManager == null));
        loaderManager.initLoader(1002, null, this);
        notifyDataChange();

        // ͨ��intent�����ȵõ��������ĳ������ݣ��޸ĳ������ʱ�ش��������ݣ�
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
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
        super.onDestroy();
        if (D) {
            Log.e(TAG, "++++ ON DESTROY ++++");
        }
    }

    private void initViews(View v) {
        lstTractor = (ListView) v.findViewById(R.id.lstTractor);
        lstTractor.setOnCreateContextMenuListener(longPressListener);
        v.findViewById(R.id.btnAddTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnEditTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnRemoveTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnClearTractorInfo).setOnClickListener(this);
        v.findViewById(R.id.btnSaveTractorInfo).setOnClickListener(this);

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
        tractorListAdapter = new TractorListAdapter(getActivity(), tractorList);
        lstTractor.setAdapter(tractorListAdapter);
        tractorListAdapter.notifyDataSetChanged();
        // ÿ��loader���֮�������ͷ�database��Դ�Լ������ݿ�ѹ��
        mApp.getDatabaseManager().releaseDataBase();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (D) {
            Log.e(TAG, "++++ ON LOAD RESET ++++");
        }
    }

    public List<Map<String, String>> updateListData(Cursor cursor) {
        return DatabaseManager.cursorToListAddListNumber(cursor);
    }

    public void notifyDataChange() {
        loaderManager.getLoader(1002).onContentChanged();
    }

    // ����ɾ�����ݼ���������д��������
    OnCreateContextMenuListener longPressListener = new OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            // ���ô�����ʾ��info���Ա�ʾ��ʱ����Ĳ˵���λ�ã��ݴ˿��Բ�ѯ���ݿ��Ӧ���ֵĳ�����Ϣ
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            AlertDialog.Builder longPressAlertBuilder = new AlertDialog.Builder(getActivity());
            longPressAlertBuilder.setTitle(getString(R.string.alert_title_tractor_longpress));
            longPressAlertBuilder.setIcon(R.drawable.alert);

            // ���������Ի�ȡѡ�е���Ŀ�ĳ��������ƣ����ɽ�����һ������
            int listposition = info.position;
            Map<String, String> map = tractorList.get(listposition);
            final String name = map.get("tName");

            longPressAlertBuilder.setPositiveButton(getString(R.string.delete_tractor),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mApp.getDatabaseManager().deleteTractorByName(name);
                            notifyDataChange();
                        }
                    });

            longPressAlertBuilder.setNegativeButton(getString(R.string.carset_cansel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            longPressAlertBuilder.setNeutralButton(getString(R.string.carset_edit),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ��ѡ���޸ģ��򷵻��޸�ҳ�沢������������

                            // Cursor resultCursor =
                            // mApp.getDatabaseManager().queryFieldWithPointsByName(name);
                            // Map<String,String> map =
                            // DatabaseManager.cursor2Map(resultCursor);
                            // String[] s = new String[map.size()];
                            // Intent intent = new Intent();
                            // intent.setClass(CarListActivity.this,
                            // CarSetActivity.class);
                            // for(String key : map.keySet()) {
                            // intent.putExtra(key, map.get(key));
                            // }
                            // startActivity(intent);
                        }
                    });

            String message = getString(R.string.tractor_alert_info_longpressinfo_1) + map.get("tName").toString()
                    + getString(R.string.tractor_alert_info_longpressinfo_2);
            longPressAlertBuilder.setMessage(message);
            longPressAlertBuilder.show();
        }

    };

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
            Cursor cursor = mApp.getDatabaseManager().queryTractorByName("");
            Log.e(TAG, cursor.toString());
            return cursor;
        }

    }

}
