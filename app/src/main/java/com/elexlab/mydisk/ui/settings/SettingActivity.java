package com.elexlab.mydisk.ui.settings;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.elexlab.mydisk.R;
import com.elexlab.mydisk.datasource.DataCondition;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.SettingDataSource;
import com.elexlab.mydisk.pojo.Setting;
import com.elexlab.mydisk.ui.BaseActivity;

public class SettingActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    }

    public void configSettings(View view){
        EditText etEndPoint = findViewById(R.id.etEndPoint);
        EditText etAk = findViewById(R.id.etAk);
        EditText etSk = findViewById(R.id.etSk);
        EditText etBucketName = findViewById(R.id.etBucketName);

        String endpoint = etEndPoint.getText().toString();
        String ak = etAk.getText().toString();
        String sk = etSk.getText().toString();
        String bucketName = etBucketName.getText().toString();


        SettingDataSource settingDataSource = new SettingDataSource();
        DataCondition dataCondition = new DataCondition();
        dataCondition.addParam("id","ONLY");
        settingDataSource.getData(new DataSourceCallback<Setting>() {
            @Override
            public void onSuccess(Setting set, String... extraParams) {
                if(set == null){
                    final Setting setting = new Setting(endpoint,ak,sk,bucketName);

                    settingDataSource.addData(setting, new DataSourceCallback<Setting>() {
                        @Override
                        public void onSuccess(Setting setting, String... extraParams) {
                            Toast.makeText(SettingActivity.this,"配置成功",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errMsg, int code) {
                            Toast.makeText(SettingActivity.this,"配置失败："+errMsg,Toast.LENGTH_SHORT).show();
                            restart();
                        }
                    });
                }else {
                    set.setEndpoint(endpoint);
                    set.setAk(ak);
                    set.setSk(sk);
                    set.setBucketName(bucketName);
                    settingDataSource.updateData(set, new DataSourceCallback<Setting>() {
                        @Override
                        public void onSuccess(Setting setting, String... extraParams) {
                            Toast.makeText(SettingActivity.this,"配置成功",Toast.LENGTH_SHORT).show();
                            restart();
                        }

                        @Override
                        public void onFailure(String errMsg, int code) {
                            Toast.makeText(SettingActivity.this,"配置失败："+errMsg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        }, dataCondition, Setting.class);
    }

    private void restart(){
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
    }
}
