package top.lyfzn.music.douyinquick;


import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Douyin {
    private String user_name;
    private DYTask dYtask;
    private int user_count = 0;

    public boolean isHas_long() {
        return has_long;
    }

    private boolean has_long = false;

    public String getQuantity_name() {
        return quantity_name;
    }

    private String quantity_name = "";

    public String getLong_video() {
        return long_video;
    }

    private void setLong_video(String long_video, String quantity_name) {
        this.long_video = long_video;
        if (!this.long_video.equals("")) {
            this.has_long = true;
            this.quantity_name = quantity_name;
        }
    }

    private String long_video;
    private DYCallBack callBack;

    public Douyin(String share_url, DYCallBack callBack) {
        this.callBack = callBack;
        DYTask task = new DYTask();
        dYtask = task;
        task.execute(share_url);

    }

    public void cancel() {
        if (dYtask != null) {
            dYtask.cancel(true);
        }
    }

    public String getUser_name() {
        return user_name;
    }


    class DYTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return new HttpUtil().HttpRequest(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject object = JSON.parseObject(s);
                JSONObject video_data = object.getJSONObject("video_data");
                String nwm_video_url_hq = video_data.getString("nwm_video_url_HQ");
                setLong_video(nwm_video_url_hq,"");
                user_name = object.getJSONObject("author").getString("nickname");
            } catch (Exception e) {
                callBack.HttpSuccessDo(Douyin.this, true);
                return;
            }
            callBack.HttpSuccessDo(Douyin.this, false);
        }
    }


    public interface DYCallBack {
        void HttpSuccessDo(Douyin douyin, boolean error);//获取抖音页面内容后做的事
    }
}
