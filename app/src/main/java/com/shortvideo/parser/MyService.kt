package com.shortvideo.parser

import android.app.AlertDialog
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MyService : Service() {
    private var clipboardManager: ClipboardManager? = null
    private var ocm: ClipboardManager.OnPrimaryClipChangedListener? = null
    private val SERVICE_ID = 1000
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    private val mParserMap: MutableMap<String, ShortVideoParser> = HashMap()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var channel: NotificationChannel? = null
        channel = NotificationChannel(
            "DouYin",
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(applicationContext, "DouYin").build()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(SERVICE_ID, notification)
        } else {
            startForeground(
                SERVICE_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }
        mParserMap["douyin"] = Tiktok()
        mParserMap["tiktok"] = Tiktok()
        initFloatingWindow()
        Toast.makeText(
            this,
            "欢迎使用抖音快速服务：服务已创建，删除进程即可销毁服务",
            Toast.LENGTH_SHORT
        ).show()
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        var time = System.currentTimeMillis()
        ocm = ClipboardManager.OnPrimaryClipChangedListener {
            Log.e("======", "onPrimaryClipChanged: ")
            if (System.currentTimeMillis() - time > 1000) {
                time = System.currentTimeMillis()
                val data: ClipData? = clipboardManager!!.primaryClip
                val item: ClipData.Item? = data?.getItemAt(0)
                if (item == null) {
                    Toast.makeText(this@MyService, "该链接不是有效连接", Toast.LENGTH_SHORT).show()
                    return@OnPrimaryClipChangedListener
                }
                val shareUrl: String = item.text.toString()
                if (shareUrl.contains("v.douyin.com") || shareUrl.contains("tiktok")) {
                    val progressDialog = ProgressDialog(applicationContext)
                    progressDialog.setMessage("正在解析....")
                    progressDialog.setCancelable(false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //针对安卓8.0对全局弹窗适配
                        progressDialog.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                    } else {
                        progressDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                    }
                    progressDialog.show()
                    val parser = findParser(shareUrl)
                    GlobalScope.launch(Dispatchers.IO) {
                        val downloadUrl = parser?.getDownloadUrl(shareUrl)
                        GlobalScope.launch(Dispatchers.Main){
                            progressDialog.dismiss()
                            if (!TextUtils.isEmpty(downloadUrl)) {
                                val ab = AlertDialog.Builder(applicationContext)
                                    .setTitle("DouYinQuick")
                                    .setMessage("""检测到抖音分享视频:(${System.currentTimeMillis()}).mp4""".trimIndent())
                                    .setCancelable(true)
                                ab.setNeutralButton("完整视频下载") { dialog, which ->
                                    dialog.dismiss()
                                    //创建下载任务,downloadUrl就是下载链接
                                    val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(downloadUrl))
                                    // 指定下载路径和下载文件名
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  "${System.currentTimeMillis()}.mp4")
                                    // 获取下载管理器
                                    val downloadManager: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                    // 将下载任务加入下载队列，否则不会进行下载
                                    downloadManager.enqueue(request)
                                    Toast.makeText(this@MyService, "开始下载", Toast.LENGTH_LONG).show()
                                }
                                val alertDialog = ab.create()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //针对安卓8.0对全局弹窗适配
                                    alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                                } else {
                                    alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                                }
                                alertDialog.show()
                            } else {
                                Toast.makeText(this@MyService, "该链接不是有效连接", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
        clipboardManager!!.addPrimaryClipChangedListener(ocm)
    }

    private fun findParser(url : String): ShortVideoParser? {
        for (key in mParserMap.keys) {
            if(url.contains(key))
                return mParserMap[key]
        }
        return null
    }

    private fun initFloatingWindow() {
        val wm: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.ic_launcher_background)
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
            1,
            1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP or Gravity.END
        wm.addView(imageView, layoutParams)
    }

    private fun isFileExcited(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}