package sjtu.me.tractor.field;

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
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.AlertDialogUtil;

/**
 * @author BillHu ����Fragment��ͼ
 */
public class FieldSettingFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

    public static final String TAG = "FieldSettingFragment";
    public static final boolean D = true;
    
    private ListView lstField;
    
    private Button btnImportField;
    private Button btnExportField;
    private Button btnAddField;
    private Button btnRemoveField;
    private Button btnClearAllFields;
    public AlertDialog.Builder alertBuilderClearFields,alertBuilderClearFieldsAffirm;
    
    public static MyApplication mApp;
    private LoaderManager loaderManager;
    private Loader<Cursor> loader;
    private List<Map<String, String>> fieldList;
    private FieldListAdapter fieldListAdapter;

    public FieldSettingFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "++++ ON CREATE ++++");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.e(TAG, "++++ ON CREATE VIEW ++++");
        View view = inflater.inflate(R.layout.home_fragment_field_setting, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D)
            Log.e(TAG, "++++ ON ACTIVITY CREATE ++++");

        // ��ȡApplicationȫ�ֱ���
        mApp = (MyApplication) getActivity().getApplication();
        
        loader = new MyAsyncLoader(this.getActivity());
        loaderManager = getActivity().getLoaderManager();
        loaderManager.initLoader(1001, null, this);
        notifyDataChange();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++++ ON START ++++");
        
        notifyDataChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "++++ ON RESUME ++++");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "++++ ON PAUSE ++++");
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "++++ ON STOP ++++");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.e(TAG, "++++ ON DESTROY ++++");
    }

    private void initViews(View view) {
        lstField = (ListView) view.findViewById(R.id.lstFields);
        lstField.setOnCreateContextMenuListener(longPressListener);
        
        btnImportField = (Button) view.findViewById(R.id.btnImportField);
        btnImportField.setOnClickListener(this);
        
        btnExportField = (Button) view.findViewById(R.id.btnExportField);
        btnExportField.setOnClickListener(this);
        
        btnAddField = (Button) view.findViewById(R.id.btnAddField);
        btnAddField.setOnClickListener(this);
        
        btnRemoveField = (Button) view.findViewById(R.id.btnRemoveField);
        btnRemoveField.setOnClickListener(this);
       
        btnClearAllFields = (Button) view.findViewById(R.id.btnClearAllFields);
        btnClearAllFields.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btnImportField:
            new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.alert_title_field_tip))
            .setMessage(getString(R.string.alert_message_field_import))
            .setPositiveButton("ȷ��", null)
            .show();
            break;
            
        case R.id.btnExportField:
            new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.alert_title_field_tip))
            .setMessage(getString(R.string.alert_message_field_export))
            .setPositiveButton("ȷ��", null)
            .show();
            break;
            
        case R.id.btnAddField:
            AlertDialog dialogAdd = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.alert_title_field_tip))
                    .setMessage(getString(R.string.alert_dialog_message_add_field))
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(getString(R.string.affirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent("sjtu.me.tractor.field.FieldAddingActivity2"));
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
            
        case R.id.btnRemoveField:
            new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.alert_title_field_tip))
            .setMessage(getString(R.string.alert_info_field_tip))
            .setPositiveButton("ȷ��", null)
            .show();
            break;
            
        case R.id.btnClearAllFields:
            alertBuilderClearFields = new AlertDialog.Builder(getActivity());
            alertBuilderClearFields.setTitle(getString(R.string.alert_title));
            alertBuilderClearFields.setMessage(getString(R.string.alert_info_clear_all_fields));
            alertBuilderClearFields.setIcon(R.drawable.alert);
            alertBuilderClearFields.setPositiveButton("ȷ��ɾ��",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    alertBuilderClearFieldsAffirm = new AlertDialog.Builder(FieldSettingFragment.this.getActivity());
                    alertBuilderClearFieldsAffirm.setTitle(getString(R.string.alert_title_affirm));
                    alertBuilderClearFieldsAffirm.setMessage(getString(R.string.alert_info_clear_all_fields_affirm));
                    alertBuilderClearFieldsAffirm.setIcon(R.drawable.alert);
                    alertBuilderClearFieldsAffirm.setPositiveButton("�ٴ�ȷ��ɾ��",new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mApp.getDatabaseManager().clearAllFieldData();
                            notifyDataChange();
                        }
                    });
                    alertBuilderClearFieldsAffirm.setNegativeButton("ȡ��ɾ��",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertBuilderClearFieldsAffirm.show();
                }
            });
            alertBuilderClearFields.setNegativeButton("ȡ��ɾ��",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertBuilderClearFields.show();
            break;

        default:
            break;
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (D)   Log.e(TAG, "++++ ON CREATE LOADER ++++");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (D)   Log.e(TAG, "++++ ON LOAD FINISHED ++++");
        
        fieldList = updateListData(data);
        fieldListAdapter = new FieldListAdapter(getActivity(), fieldList);
        lstField.setAdapter(fieldListAdapter);
        fieldListAdapter.notifyDataSetChanged();
        // ÿ��loader���֮�������ͷ�database��Դ�Լ������ݿ�ѹ��
        mApp.getDatabaseManager().releaseDataBase();
        
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (D)   Log.e(TAG, "++++ ON LOAD RESET ++++");
    }
    
    public List<Map<String, String>> updateListData(Cursor cursor) {
        return DatabaseManager.cursorToListAddListNumber(cursor);
    }
    
    public void notifyDataChange() {
        loaderManager.getLoader(1001).onContentChanged();
    }
    
  //����ɾ�����ݼ���������д��������
    OnCreateContextMenuListener longPressListener = new OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            //���ô�����ʾ��info���Ա�ʾ��ʱ����Ĳ˵���λ�ã��ݴ˿��Բ�ѯ���ݿ��Ӧ���ֵĳ�����Ϣ
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            AlertDialog.Builder longPressAlertBuilder = new AlertDialog.Builder(getActivity());
            longPressAlertBuilder.setTitle(getString(R.string.alert_title_field_longpress));
            longPressAlertBuilder.setIcon(R.drawable.alert);
            
            //���������Ի�ȡѡ�е���Ŀ�ĳ��������ƣ����ɽ�����һ������
            int listposition = info.position;
            Map<String, String> map = fieldList.get(listposition);
            final String name = map.get("fName");
            
            longPressAlertBuilder.setPositiveButton(getString(R.string.delete_field), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mApp.getDatabaseManager().deleteFieldByName(name);
                    notifyDataChange();
                }
            });
            
            longPressAlertBuilder.setNegativeButton(getString(R.string.carset_cansel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            
            longPressAlertBuilder.setNeutralButton(getString(R.string.carset_edit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //��ѡ���޸ģ��򷵻��޸�ҳ�沢������������
                   
//                    Cursor resultCursor = mApp.getDatabaseManager().queryFieldWithPointsByName(name);
//                    Map<String,String> map = DatabaseManager.cursor2Map(resultCursor);
//                    String[] s = new String[map.size()];
//                    Intent intent = new Intent();
//                    intent.setClass(CarListActivity.this, CarSetActivity.class);
//                    for(String key : map.keySet()) {
//                        intent.putExtra(key, map.get(key));
//                    }
//                    startActivity(intent);
                }
            });
            
            String message = getString(R.string.field_alert_info_longpressinfo_1) + map.get("fName") +
                    getString(R.string.field_alert_info_longpressinfo_2);
            longPressAlertBuilder.setMessage(message);
            longPressAlertBuilder.show();
        }
        
    };
    
    // �����첽loaderʵ��ʵʱ��أ�ע��һ��Ҫ�Ǿ�̬��
    public static class MyAsyncLoader extends AsyncTaskLoader<Cursor> {
        MyAsyncLoader(Context context) {
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
            Cursor cursor = mApp.getDatabaseManager().queryFieldByName("");
            Log.e(TAG, cursor.toString());
            return cursor;
        }
        
    }
}
