package top.lyfzn.music.douyinquick;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class MyService extends Service {
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener ocm;
    private Long time;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        channel = new NotificationChannel("DouYin", getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(), "DouYin").build();
        startForeground(1, notification);


        initFloatingWindow();

        final String PATH = Environment.getExternalStorageDirectory().getPath();
        Toast.makeText(this, "欢迎使用抖音快速服务：服务已创建，删除进程即可销毁服务", Toast.LENGTH_SHORT).show();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        time = System.currentTimeMillis();
        ocm = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Log.e("======", "onPrimaryClipChanged: " );
                if (System.currentTimeMillis() - time > 200) {
                    time = System.currentTimeMillis();
                    ClipData data = clipboardManager.getPrimaryClip();
                    ClipData.Item item = data != null ? data.getItemAt(0) : null;
                    if (item == null) {
                        Toast.makeText(MyService.this, "该链接不是有效连接(DouYinQuick)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String shareUrl = item.getText().toString();
                    if (shareUrl.contains("v.douyin.com") || shareUrl.contains("tiktok")) {
                        final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
                        progressDialog.setMessage("正在解析....");
                        progressDialog.setCancelable(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//针对安卓8.0对全局弹窗适配
                            progressDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
                        } else {
                            progressDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        }
                        progressDialog.show();

                        final Douyin douyin = new Douyin("https://api.douyin.wtf/api?url="+shareUrl+"&minimal=false", new Douyin.DYCallBack() {
                            @Override
                            public void HttpSuccessDo(final Douyin douyin, boolean error) {
                                progressDialog.hide();
                                if (!error) {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(getApplicationContext())
                                            .setTitle("DouYinQuick")
                                            .setMessage("检测到抖音分享视频:\n" + douyin.getUser_name() + "(" + System.currentTimeMillis() + ").mp4")
                                            .setCancelable(true);

                                    if (douyin.isHas_long()) {
                                        ab.setNeutralButton("完整视频下载", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                String path = PATH + "/DouyinQuick/" + douyin.getUser_name() + "(" + System.currentTimeMillis() + ")" + douyin.getQuantity_name() + ".mp4";
                                                if (isFileExcited(path)) {//去重复处理
                                                    Toast.makeText(MyService.this, "视频已存在", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                //创建下载任务,downloadUrl就是下载链接
                                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(douyin.getLong_video()));
                                                // 指定下载路径和下载文件名
                                                request.setDestinationInExternalPublicDir("/DouyinQuick/", douyin.getUser_name() + "(" + System.currentTimeMillis() + ")" + douyin.getQuantity_name() + ".mp4");
                                                // 获取下载管理器
                                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                                // 将下载任务加入下载队列，否则不会进行下载
                                                downloadManager.enqueue(request);
                                                Toast.makeText(MyService.this, "开始下载：" + douyin.getUser_name() + "(" + System.currentTimeMillis() + ")" +
                                                        douyin.getQuantity_name() + ".mp4", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    AlertDialog alertDialog = ab.create();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//针对安卓8.0对全局弹窗适配
                                        alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
                                    } else {
                                        alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                                    }
                                    alertDialog.show();

                                } else {
                                    Toast.makeText(MyService.this, "该链接不是有效连接(DouYinQuick)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                douyin.cancel();
                            }
                        });
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(ocm);
    }

    private void initFloatingWindow() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_launcher_background);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP|Gravity.END;
        wm.addView(imageView, layoutParams);
    }

    private boolean isFileExcited(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
