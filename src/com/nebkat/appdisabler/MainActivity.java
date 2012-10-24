package com.nebkat.appdisabler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Window;
import java.util.Collections;
import java.util.List;

public class MainActivity extends SherlockListActivity {

    private static final String WARNING_DIALOG_SHOWN_KEY = "warning_dialog_shown";

    private PackageManager mPackageManager;

    private AppsAdapter mAppsAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_list);

        mPackageManager = getPackageManager();
        mAppsAdapter = new AppsAdapter(this, R.layout.app_list_item);
        mAppsAdapter.setNotifyOnChange(true);

        setListAdapter(mAppsAdapter);

        AsyncTask<Void, Void, Void> refreshAppsTask = new AsyncTask<Void, Void, Void>(){

            @Override
            protected void onPreExecute() {
                setSupportProgressBarIndeterminateVisibility(true);
                setSupportProgressBarIndeterminate(true);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void result) {
                restore();
                setSupportProgressBarIndeterminateVisibility(false);
                setSupportProgressBarIndeterminate(false);
                super.onPostExecute(result);
            }

            @Override
            protected Void doInBackground(Void... params) {
                refreshApps();
                return null;
            }
        };
        refreshAppsTask.execute(null, null, null);

        if (!RootUtils.hasRoot()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_no_root_title).setMessage(R.string.dialog_no_root_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.create().show();
        }

        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(WARNING_DIALOG_SHOWN_KEY, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_warning_title).setMessage(R.string.dialog_warning_message)
                    .setPositiveButton(android.R.string.ok, null).create().show();
            builder.create().show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(WARNING_DIALOG_SHOWN_KEY, true).commit();
        }
    }

    private void restore() {
        for (int i = 0; i < getListAdapter().getCount(); i++) {
            ApplicationInfo info = (ApplicationInfo) getListAdapter().getItem(i);
            int enabled = mPackageManager.getApplicationEnabledSetting(info.packageName);
            if (enabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    enabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                getListView().setItemChecked(i, true);
            }
        }
    }

    private void refreshApps() {
        final List<ApplicationInfo> apps = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(mPackageManager));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAppsAdapter.clear();

                for (ApplicationInfo app : apps) {
                    mAppsAdapter.add(app);
                }
            }
        });
    }

    private class AppsAdapter extends ArrayAdapter<ApplicationInfo> {

        private final LayoutInflater mInfaltor;

        public AppsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            mInfaltor = LayoutInflater.from(context);

        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ApplicationInfo info = getItem(position);

            if(convertView == null) {
                convertView = mInfaltor.inflate(R.layout.app_list_item, parent, false);
            }

            final View item = convertView;

            ImageView icon = (ImageView) item.findViewById(R.id.icon);
            TextView title = (TextView) item.findViewById(R.id.title);
            TextView packageName = (TextView) item.findViewById(R.id.package_name);

            icon.setImageDrawable(info.loadIcon(mPackageManager));
            title.setText(info.loadLabel(mPackageManager));
            packageName.setText(info.packageName);


            item.setTag(info.packageName);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean enabled = ((Checkable) item).isChecked();

                    new UpdateApplicationEnabledSettingTask((String)item.getTag(), enabled, position).execute(null, null, null);
                }
            });

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private class UpdateApplicationEnabledSettingTask extends AsyncTask<Void, Void, Integer> {
        private String mPackageName;
        private boolean mEnabled;
        private int mListItemPosition;

        public UpdateApplicationEnabledSettingTask(String packageName, boolean enabled, int listItemPosition) {
            super();
            mPackageName = packageName;
            mEnabled = enabled;
            mListItemPosition = listItemPosition;
        }

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
            setSupportProgressBarIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer result) {
            setSupportProgressBarIndeterminateVisibility(false);
            setSupportProgressBarIndeterminate(false);

            switch (result) {
                case AppDisabler.SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    break;
                case AppDisabler.ERROR_UNKNOWN:
                    Toast.makeText(MainActivity.this, R.string.error_unknown, Toast.LENGTH_LONG).show();
                    break;
                case AppDisabler.ERROR_ROOT:
                    Toast.makeText(MainActivity.this, R.string.error_root, Toast.LENGTH_LONG).show();
                    break;
                case AppDisabler.ERROR_NO_ROOT:
                    Toast.makeText(MainActivity.this, R.string.error_no_root, Toast.LENGTH_LONG).show();
                    break;

            }

            boolean disabled = mPackageManager.getApplicationEnabledSetting(mPackageName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    mPackageManager.getApplicationEnabledSetting(mPackageName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;

            getListView().setItemChecked(mListItemPosition, disabled);

            super.onPostExecute(result);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return AppDisabler.setApplicationEnabledSetting(MainActivity.this, mPackageName, mEnabled);
        }
    }
}

