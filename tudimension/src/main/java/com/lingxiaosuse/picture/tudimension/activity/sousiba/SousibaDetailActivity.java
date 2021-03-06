package com.lingxiaosuse.picture.tudimension.activity.sousiba;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.camera.lingxiao.common.app.BaseActivity;
import com.camera.lingxiao.common.app.ContentValue;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.adapter.BaseRecycleAdapter;
import com.lingxiaosuse.picture.tudimension.adapter.MzituRecyclerAdapter;
import com.lingxiaosuse.picture.tudimension.modle.MzituModle;
import com.lingxiaosuse.picture.tudimension.utils.ToastUtils;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;
import com.lingxiaosuse.picture.tudimension.widget.SmartSkinRefreshLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SousibaDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar_sousiba_detail)
    Toolbar toolbarSousibaDetail;
    @BindView(R.id.rv_sousiba_detail)
    RecyclerView rvSousibaDetail;
    @BindView(R.id.swip_sousiba_detail)
    SmartSkinRefreshLayout refreshLayout;
    private Intent intent;
    private String imgUrl;

    private List<MzituModle> mImgList = new ArrayList<>();
    private MzituRecyclerAdapter mAdapter;
    private String downloadUrl;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_spusiba_detail;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setToolbarBack(toolbarSousibaDetail);
        intent = getIntent();
        imgUrl = intent.getStringExtra("imgurl");
        getDataFromJsoup();
        initRecycler();
    }

    private void initRecycler() {
        refreshLayout.autoRefresh();
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            mImgList.clear();
            getDataFromJsoup();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {

            getDataFromJsoup();
        });


        mAdapter = new MzituRecyclerAdapter(R.layout.list_mzitu,mImgList);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
             /*Intent intent = new Intent(UIUtils.getContext(),SousibaDetailActivity.class);
                intent.putExtra("imgurl",mImgList.get(position));
                startActivity(intent);*/
        });
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.VERTICAL);
        rvSousibaDetail.setHasFixedSize(true);
        rvSousibaDetail.setLayoutManager(manager);
        rvSousibaDetail.setAdapter(mAdapter);
    }

    private void getDataFromJsoup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connection = Jsoup.connect(imgUrl)
                        .timeout(10000);
                Document doc = null;

                //获取首页详细数据
                try {

                    Connection.Response response = connection.execute();
                    response.cookies();
                    doc = connection.get();

                    //Elements elementImgs = doc.getElementsByClass("content");
                    Element elementHome = doc.getElementById("mbtxfont");
                    Elements elementImgs = elementHome.select("img");

                    for (Element elementImg : elementImgs) {
                        //图片链接
                        final String imgUrl = ContentValue.SOUSIBA_URL + elementImg.attr("src");
                        //标题
                        final String imgAlt = ContentValue.SOUSIBA_URL + elementImg.attr("alt");
                        MzituModle modle = new MzituModle();
                        modle.setTitle(imgAlt);
                        modle.setImgUrl(imgUrl);
                        mImgList.add(modle);
                        Log.i("sousibaActivity", "图片链接: " + imgUrl + "  标题：" + imgAlt);

                    }
                    downloadUrl = elementHome.select("a").text();
                    Log.i("sousibaActivity", "下载地址: " + downloadUrl);

                } catch (Exception e) {

                } finally {
                    UIUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                            mAdapter.notifyDataSetChanged();

                            if (mImgList.size()>0){
                                toolbarSousibaDetail.setTitle(mImgList.get(0).getTitle());
                            }

                            ToastUtils.show("下载地址："+downloadUrl);
                        }
                    });
                }

            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_web_share:
                showShare(downloadUrl);
                break;
            case R.id.menu_web_copy:
                copyImgUrl();
                break;
            case R.id.menu_web_browser:
                chooseBrower(downloadUrl);
                break;
            default:
                break;
        }
        return true;
    }

    private void copyImgUrl() {
        ClipboardManager manager = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", downloadUrl);
        // 将ClipData内容放到系统剪贴板里。
        manager.setPrimaryClip(mClipData);
        ToastUtils.show("复制成功!");
    }

    private void chooseBrower(String url){
        Uri uri = Uri.parse(url);
        Intent urlIntent = new Intent();
        urlIntent.setAction(Intent.ACTION_VIEW);
        urlIntent.setData(uri);
        startActivity(Intent.createChooser(urlIntent, "请选择一个浏览器打开"));
    }
    private void showShare(String url){
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(textIntent, "分享"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return true;
    }
}
