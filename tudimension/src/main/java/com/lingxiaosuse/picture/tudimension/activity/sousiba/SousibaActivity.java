package com.lingxiaosuse.picture.tudimension.activity.sousiba;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.camera.lingxiao.common.app.BaseActivity;
import com.camera.lingxiao.common.app.ContentValue;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.adapter.BaseRecycleAdapter;
import com.lingxiaosuse.picture.tudimension.adapter.MzituRecyclerAdapter;
import com.lingxiaosuse.picture.tudimension.modle.MzituModle;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;
import com.lingxiaosuse.picture.tudimension.widget.SmartSkinRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

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

public class SousibaActivity extends BaseActivity {
    @BindView(R.id.toolbar_sousiba)
    Toolbar toolbarSousiba;
    @BindView(R.id.rv_sousiba)
    RecyclerView rvSousiba;
    @BindView(R.id.swip_sousiba)
    SmartSkinRefreshLayout refreshLayout;
    private List<MzituModle> mImgList = new ArrayList<>();  //存放图片地址
    private List<String> mTitleList = new ArrayList<>();  //存放标题
    private List<String> mUrlList = new ArrayList<>();  //存放跳转链接
    private MzituRecyclerAdapter mAdapter;

    private int mPage = 1; //默认加载第一页
    private String nextUrl = "index.html";
    private Elements nextPages;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_sousiba;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setToolbarBack(toolbarSousiba);
        toolbarSousiba.setTitle("搜丝吧");
        refreshLayout.autoRefresh();
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            mImgList.clear();
            getDataFromJsoup();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            mPage++;
            getDataFromJsoup();
        });

        initRecycler();
        getDataFromJsoup();
    }

    private void initRecycler() {
        mAdapter = new MzituRecyclerAdapter(R.layout.list_mzitu,mImgList);
        mAdapter.setTitle(mTitleList);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(UIUtils.getContext(),SousibaDetailActivity.class);
            intent.putExtra("imgurl",mUrlList.get(position));
            startActivity(intent);
        });
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        rvSousiba.setHasFixedSize(true);
        rvSousiba.setLayoutManager(manager);
        rvSousiba.setAdapter(mAdapter);
    }

    private void getDataFromJsoup() {
        new Thread(() -> {
            Connection connection = Jsoup.connect(ContentValue.SOUSIBA_URL + "/guochantaotu/xiuren/"+nextUrl)
                    .timeout(10000);
            Document doc = null;

            //获取首页详细数据
            try {
                Connection.Response response = connection.execute();
                response.cookies();
                doc = connection.get();

                Element elementHome = doc.getElementById("lm_downlist_box");
                Elements elementImgs = elementHome.getElementsByClass("pic");
                Elements elementTitle = elementHome.getElementsByClass("geme_dl_info");
                for (Element elementImg : elementImgs) {
                    MzituModle modle = new MzituModle();
                    //跳转链接
                    final String imgUrl = ContentValue.SOUSIBA_URL+elementImg.getElementsByTag("a").attr("href");
                    //专辑图片
                    final String imgSrc = ContentValue.SOUSIBA_URL+elementImg.select("img").attr("src");
                    modle.setImgUrl(imgSrc);
                    modle.setTitle("");
                    mImgList.add(modle);
                    mUrlList.add(imgUrl);
                    Log.i("sousibaActivity", "跳转链接: " + imgUrl + "  专辑图片：" + imgSrc);

                }

                for (Element element : elementTitle) {
                    //专辑名字
                    final String imgAlt = element.select("a").first().text();
                    Log.i("sousibaActivity", "专辑名字：" + imgAlt);
                    mTitleList.add(imgAlt);

                }
                nextPages = doc.getElementsByClass("lm_pager_box");
                if (mPage == 1){
                    nextUrl = nextPages.select("a").get(1).attr("href");
                    Log.i("sousibaActivity", "链接：" + nextUrl);
                /*}else if (mPage == 2){
                    nextUrl = nextPages.select("a").get(2).attr("href");*/
                }else {
                    nextUrl = nextPages.select("a").get(2).attr("href");
                    Log.i("sousibaActivity", "链接：" + nextUrl);
                }

            } catch (Exception e) {

            } finally {
                UIUtils.runOnUIThread(() -> {
                    refreshLayout.finishLoadMore();
                    refreshLayout.finishRefresh();
                    mAdapter.notifyDataSetChanged();
                });
            }

        }).start();
    }
}
