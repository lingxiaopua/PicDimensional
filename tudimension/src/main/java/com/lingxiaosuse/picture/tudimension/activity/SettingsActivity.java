package com.lingxiaosuse.picture.tudimension.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.lingxiaosuse.picture.tudimension.R;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        initToolbar();
        getCacheSize();
    }

    private void getCacheSize() {
        Fresco.getImagePipelineFactory().getMainFileCache().trimToMinimum();
        long size = Fresco.getImagePipelineFactory().getMainFileCache().getSize();
        clearSize.setText(getDataSize(size));
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
    @OnClick({R.id.rl_update,R.id.rl_clear,R.id.rl_share})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.rl_update:
                switchCompat.setChecked(!switchCompat.isChecked());
                if (switchCompat.isChecked()){
                    textWifi.setText("WIFI情况下自动下载安装包");
                }else {
                    textWifi.setText("WIFI情况下不自动下载安装包");
                }
                break;
            case R.id.rl_clear:
                clearCache();
                clearSize.setText("缓存清理完毕");
                break;
            case R.id.rl_share:
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
}
