package com.lingxiaosuse.picture.tudimension.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.global.ContentValue;
import com.lingxiaosuse.picture.tudimension.utils.SpUtils;
import com.lingxiaosuse.picture.tudimension.utils.ToastUtils;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {
    @BindView(R.id.toolbar_setting)
    Toolbar toolbar;
    @BindView(R.id.switch_upload)
    SwitchCompat switchCompat;
    @BindView(R.id.tv_clear_size)
    TextView clearSize;
    @BindView(R.id.tv_update_wifi)
    TextView textWifi;

    @BindView(R.id.tv_daily_switch)
    TextView textDaily;  //启动页面的图片
    @BindView(R.id.switch_daily)
    SwitchCompat switchDaily;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        initToolbar();
        getCacheSize();
        boolean isCheck = SpUtils.getBoolean(this,ContentValue.IS_CHECK,true);
        switchCompat.setChecked(isCheck);
        if (switchCompat.isChecked()){
            textWifi.setText("WIFI情况下自动检测更新");
        }else {
            textWifi.setText("WIFI情况下不检测更新");
        }

        boolean isCheck1 = SpUtils.getBoolean(this,ContentValue.IS_OPEN_DAILY,true);
        switchDaily.setChecked(isCheck1);
        if (switchDaily.isChecked()){
            textDaily.setText("开启启动页每日图片");
        }else {
            textDaily.setText("关闭启动页每日图片");
        }
    }

    private void getCacheSize() {
        //子线程中计算,放在主线程可能会造成卡顿
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fresco.getImagePipelineFactory().getMainFileCache().trimToMinimum();
                final long size = Fresco.getImagePipelineFactory().getMainFileCache().getSize();
                UIUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        clearSize.setText(getDataSize(size));
                    }
                });
            }
        }).start();

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setTitle("设置");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
    @OnClick({R.id.rl_update,R.id.rl_clear,R.id.rl_share,R.id.rl_checkout,R.id.rl_daily})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.rl_update:
                switchCompat.setChecked(!switchCompat.isChecked());
                if (switchCompat.isChecked()){
                    textWifi.setText("WIFI情况下自动检测更新");
                }else {
                    textWifi.setText("WIFI情况下不检测更新");
                }
                SpUtils.putBoolean(UIUtils.getContext(), ContentValue.IS_CHECK,switchCompat.isChecked());
                break;
            case R.id.rl_clear:
                clearCache();
                clearSize.setText("缓存清理完毕");
                break;
            case R.id.rl_share:
                showShare();
                break;
            case R.id.rl_checkout:
                //换肤
                ToastUtils.show("更换皮肤");
                break;
            case R.id.rl_daily:
                switchDaily.setChecked(!switchDaily.isChecked());
                if (switchDaily.isChecked()){
                    textDaily.setText("开启启动页每日图片");
                }else {
                    textDaily.setText("关闭启动页每日图片");
                }
                SpUtils.putBoolean(UIUtils.getContext(),
                        ContentValue.IS_OPEN_DAILY,switchDaily.isChecked());
                break;
        }
    }
    /**
     *将long转换为单位
     */
    public String getDataSize(long size) {
        if (size < 0) {
            size = 0;
        }
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "bytes";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "缓存无需清理";
        }
    }

    /**
     *清除fresco的缓存
     */
    private void clearCache(){
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearMemoryCaches();
        imagePipeline.clearDiskCaches();
        // combines above two lines
        imagePipeline.clearCaches();
    }

    private void showShare(){
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不得了的应用：http://tudimension-1252348761.coscd.myqcloud.com/version/tudimension-armeabi-v7a-release.apk");
        startActivity(Intent.createChooser(textIntent, "分享"));
    }
}
