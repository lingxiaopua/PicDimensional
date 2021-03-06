package com.lingxiaosuse.picture.tudimension.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.camera.lingxiao.common.rxbus.RxBus;
import com.camera.lingxiao.common.app.BaseActivity;
import com.camera.lingxiao.common.app.ContentValue;
import com.camera.lingxiao.common.utills.SpUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.rxbusevent.DrawerChangeEvent;
import com.lingxiaosuse.picture.tudimension.utils.StringUtils;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;
import com.lingxiaosuse.picture.tudimension.widget.SettingCardView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    Toolbar toolbar;

    @BindView(R.id.card_update)
    SettingCardView cardUpdate;
    @BindView(R.id.card_daly)
    SettingCardView cardDaly;
    @BindView(R.id.card_cache)
    SettingCardView cardCache;
    @BindView(R.id.card_share)
    SettingCardView cardShare;
    @BindView(R.id.card_skin)
    SettingCardView cardSkin;
    @BindView(R.id.card_model)
    SettingCardView cardModel;
    @BindView(R.id.card_comparse)
    SettingCardView cardComparse;
    @BindView(R.id.card_animator)
    SettingCardView cardAnimator;

    private String[] mDrawerStr;
    private boolean[] checkedItems;
    String[] resolutionItems = {"显示720p的图片","显示1080p的图片","显示原图(可能会卡顿)"};
    String[] mAnimatorItems = {"弹性的动画","先快后慢的动画","匀速动画","甩头动画","不知名动画"};
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        initToolbar();
        getCacheSize();
        boolean isCheck = SpUtils
                .getBoolean(this,
                        ContentValue.IS_CHECK, true);
        cardUpdate.setChecked(isCheck);
        boolean isCheck1 = SpUtils.getBoolean(this, ContentValue.IS_OPEN_DAILY, true);
        cardDaly.setChecked(isCheck1);

        int position = SpUtils.getInt(this,ContentValue.PIC_RESOLUTION,0);
        cardComparse.setMessage(resolutionItems[position]);
    }

    @Override
    protected void initData() {
        super.initData();
        mDrawerStr = getResources().getStringArray(R.array.drawer_string);
        checkedItems = new boolean[mDrawerStr.length];
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
                        cardCache.setMessage(getDataSize(size));
                    }
                });
            }
        }).start();

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
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

    @OnClick({R.id.card_update, R.id.card_cache, R.id.card_share, R.id.card_skin, R.id.card_daly,R.id.card_model,R.id.card_comparse,R.id.card_animator})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_update:
                cardUpdate.setChecked(!cardUpdate.getChecked());
                SpUtils.putBoolean(UIUtils.getContext(), ContentValue.IS_CHECK, cardUpdate.getChecked());
                break;
            case R.id.card_cache:
                clearCache();
                cardCache.setMessage("缓存清理完毕");
                break;
            case R.id.card_share:
                showShare();
                break;
            case R.id.card_skin:
                //换肤
                StartActivity(SkinActivity.class,false);
                break;
            case R.id.card_daly:
                cardDaly.setChecked(!cardDaly.getChecked());
                SpUtils.putBoolean(UIUtils.getContext(),
                        ContentValue.IS_OPEN_DAILY, cardDaly.getChecked());
                break;
            case R.id.card_model:
                String val = SpUtils.getString(UIUtils.getContext(), ContentValue.DRAWER_MODEL,"");
                String[] posStr = val.split(",");
                for (int i = 0; i < posStr.length; i++) {
                    if (StringUtils.isNumeric(posStr[i]) && !posStr[i].isEmpty()){
                        int pos = Integer.valueOf(posStr[i]);
                        checkedItems[pos] = true;
                    }
                }
                showMultiChoiceDia("请选择模块"
                        ,mDrawerStr,checkedItems,SettingsActivity.this);
                break;
            case R.id.card_comparse:
                int position = SpUtils.getInt(this,ContentValue.PIC_RESOLUTION,0);
                showSingleChoiceDia("选择显示图片质量",position,SettingsActivity.this);
                break;
            case R.id.card_animator:
                int checked = SpUtils.getInt(this,ContentValue.ANIMATOR_TYPE,0);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setIcon(android.R.drawable.alert_dark_frame);
                builder.setTitle("选择动画");
                builder.setSingleChoiceItems(mAnimatorItems, checked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SpUtils.putInt(UIUtils.getContext(),ContentValue.ANIMATOR_TYPE,i);
                        cardAnimator.setMessage(mAnimatorItems[i]);
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                break;
        }
    }


    public void showSingleChoiceDia(String title,int checked,Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.alert_dark_frame);
        builder.setTitle(title);
        builder.setSingleChoiceItems(resolutionItems, checked, (dialogInterface, i) -> {
            SpUtils.putInt(UIUtils.getContext(),ContentValue.PIC_RESOLUTION,i);
            cardComparse.setMessage(resolutionItems[i]);
            dialogInterface.dismiss();
        });
        builder.show();
    }

    /**
     * 将long转换为单位
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
     * 清除fresco的缓存
     */
    private void clearCache() {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearMemoryCaches();
        imagePipeline.clearDiskCaches();
        // combines above two lines
        imagePipeline.clearCaches();
    }

    private void showShare() {
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不得了的应用：http://tudimension-1252348761.coscd.myqcloud.com/version/tudimension-armeabi-v7a-release.apk");
        startActivity(Intent.createChooser(textIntent, "分享"));
    }

    public void showMultiChoiceDia(String title,final String[] items,final boolean[] checkedItems,Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.alert_dark_frame);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String checkedStr = "";
                for (int i = 0; i < items.length; i++) {
                    if (checkedItems[i]) {
                        checkedStr +=i+",";
                    }
                }
                SpUtils.putString(UIUtils.getContext(),ContentValue.DRAWER_MODEL,checkedStr);
                RxBus.getInstance().post(new DrawerChangeEvent(checkedStr));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
