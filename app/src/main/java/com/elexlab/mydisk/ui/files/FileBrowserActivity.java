package com.elexlab.mydisk.ui.files;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.elexlab.mydisk.R;
import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.manager.PhoneManager;
import com.elexlab.mydisk.ui.home.FileListFragment;
import com.elexlab.mydisk.utils.CommonUtil;
import com.elexlab.mydisk.utils.FragmentUtils;
import com.elexlab.mydisk.utils.EasyLog;

public class FileBrowserActivity extends FragmentActivity {
    public static void startActivity(Context context){
        PhoneManager.getInstance().setDevice(CommonUtil.getDeviceId(HeroLib.getInstance().appContext));

        Intent intent = new Intent(context,FileBrowserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_browser);

        String device = getIntent().getStringExtra("device");
        EasyLog.d("FileBrowserActivity","device:"+device);
        if(device == null){
            device =  CommonUtil.getDeviceId(HeroLib.getInstance().appContext);
        }
        final FileListFragment fileListFragment = new FileListFragment();
        FragmentUtils.switchFragment(this,R.id.flContainer,null,fileListFragment,false);


    }
}
