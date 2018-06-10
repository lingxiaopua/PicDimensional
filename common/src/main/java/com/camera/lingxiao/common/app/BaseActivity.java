package com.camera.lingxiao.common.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.camera.lingxiao.common.R;
import com.camera.lingxiao.common.utills.SpUtils;
import com.trello.rxlifecycle2.components.RxActivity;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.zackratos.ultimatebar.UltimateBar;

import java.io.File;

public abstract class BaseActivity extends RxAppCompatActivity {

    private PackageManager mPmanager;
    private int versionCode;
    private Button cancle,ensure;

    private ImageView fanImage;
    private View dialogView;
    private AlertDialog mDialog;
    private int mProgress;
    public UltimateBar ultimateBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在界面未初始化之前调用的初始化窗口
        initWindows();
        //半透明
        ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.colorPrimary),
                100);
        ActivityController.addActivity(this);
    }

    /**
     * 初始化
     */
    private void initWindows() {

    }
    /**
     * 初始化相关参数
     */
    protected boolean initArgs(Bundle bundle){
        return true;
    }

    /**
     * 得到当前界面的资源文件id
     */
    protected abstract int getContentLayoutId();

    /**
     * 初始化控件
     */
    protected void initWidget(){
        //ButterKnife.bind(this);
    }

    /**
     *  初始化数据
     */
    protected void initData(){

    }
    public void StartActivity(Class clzz,boolean isFinish){
        startActivity(new Intent(getApplicationContext(),clzz));
        if (isFinish){
            finish();
        }
    }

    /**
     *检查更新
     */
    public boolean checkUpdate(){

        mPmanager = getPackageManager();
        int serverVersion = SpUtils
                .getInt(BaseActivity.this, ContentValue.VERSION_CODE,1);
        try {
            PackageInfo info = mPmanager.getPackageInfo(getPackageName(),0);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionCode = serverVersion;
        }
        if (versionCode<serverVersion){
            //服务器上面有新版本
            String url = SpUtils.getString(BaseActivity.this,ContentValue.DOWNLOAD_URL,"");
            showDialog(url);
            return true;
        }else {
            return false;
        }
    }

    private void showDialog(final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("检测到新版本");
        builder.setMessage(SpUtils.getString(this,ContentValue.VERSION_DES,""));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //下载
                //showDownLoadDia();
                //downLoadApk(url);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    /**
     *下载成功后安装
     */
    private void installApk(File file) {
        if(Build.VERSION.SDK_INT>=24) {//判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(this, "com.lingxiaosuse.picture.tudimension.fileprovider", file);//在AndroidManifest中的android:authorities值
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            startActivity(install);
        } else{
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(install);
        }
    }

    /**
     *设置toolbar的返回键
     */
    public void setToolbarBack(Toolbar toolbar){
        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setTitle("");

    }

    //跳转到网页
    public void goToInternet(Context context, String marketUrl){
        Uri uri = Uri.parse(marketUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(this);
    }
}
