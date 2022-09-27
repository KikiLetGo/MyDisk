package com.elexlab.mydisk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.SettingDataSource;
import com.elexlab.mydisk.pojo.Setting;
import com.elexlab.mydisk.ui.gallery.PhoneGalleryDialog;
import com.elexlab.mydisk.ui.settings.SettingActivity;
import com.elexlab.mydisk.utils.EasyLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fbMultiDevice = findViewById(R.id.fbMultiDevice);
        fbMultiDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhoneGallery();

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this,  navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        test();

        AlbumManager.getInstance().preloadAlbums(this);
    }

    private void test(){
        String deviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
        EasyLog.d("deviceName",deviceName);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //有权限，可以进行获取联系人操作
            } else {
                Toast.makeText(this, "sorry", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showPhoneGallery(){
        //1、初始化Dialog
        PhoneGalleryDialog dialog=new PhoneGalleryDialog(this,R.style.DialogTheme);
        //获取Dialogwindow对象
        Window window=dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置动画
        window.setWindowAnimations(R.style.dialog_menu_animStyle);
        //设置对话框大小
        window.getDecorView().setPadding(0,0,0,0);
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        //设置宽度和高度
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height=WindowManager.LayoutParams.WRAP_CONTENT;
        //显示Dialog
        dialog.show();
    }
}