package com.shortvideo.parser;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Intent intent_service;
    private Button stop, about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stop = findViewById(R.id.stop_finish);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent_service);
                Toast.makeText(MainActivity.this, "即将退出", Toast.LENGTH_LONG).show();
                MainActivity.this.finish();
                System.exit(0);
            }
        });

        about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                ab.setTitle("关于")
                        .setMessage("作者：zbfzn\n" +
                                "GitHub:https://github.com/zbfzn\n" +
                                "项目地址：https://github.com/zbfzn/douyin-quick\n" +
                                "使用帮助：请赋予相关权限，视频下载在DouyinQuick文件夹下，使用用户昵称+视频id命名。")
                        .setCancelable(true);
                AlertDialog ad = ab.create();
                ad.show();
            }
        });
        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE).request(new OnPermissionCallback() {
            @Override
            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        openDY();
                    } else {
                        //若没有权限，提示获取.
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }

                } else {
                    //SDK在23以下，不用管.
                    openDY();
                }
            }
        });


    }

    void openDY() {
        intent_service = new Intent();
        intent_service.setAction("com.douyinquick.com");
        intent_service.setPackage(getPackageName());
        ContextCompat.startForegroundService(this, intent_service);
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.ss.android.ugc.trill");
            if (intent == null) {
                intent = packageManager.getLaunchIntentForPackage("com.ss.android.ugc.aweme");
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "打开抖音出错！请允许打开抖音或者安装抖音app", Toast.LENGTH_SHORT).show();
            this.finish();
            System.exit(0);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (intent_service != null) {
            stopService(intent_service);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须同意所有权限才能使用本软件", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    /////
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(MainActivity.this)) {
                            openDY();
                        } else {
                            //若没有权限，提示获取.
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        //SDK在23以下，不用管.
                        openDY();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "即将退出", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
