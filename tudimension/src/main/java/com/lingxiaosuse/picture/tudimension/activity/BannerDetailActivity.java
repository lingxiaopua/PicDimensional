package com.lingxiaosuse.picture.tudimension.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.adapter.BannerRecycleAdapter;
import com.lingxiaosuse.picture.tudimension.modle.BannerModle;
import com.lingxiaosuse.picture.tudimension.retrofit.BannerInterface;
import com.lingxiaosuse.picture.tudimension.retrofit.RetrofitHelper;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BannerDetailActivity extends BaseActivity {
    // 控制ToolBar的变量
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;

    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    @BindView(R.id.swip_banner)
    SwipeRefreshLayout swipBanner;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private int skip = 0;
    private List<BannerModle.ResBean.WallpaperBean> picList = new ArrayList<>();
    private ArrayList<String> picUrlList = new ArrayList<>();
    private ArrayList<String> IdList = new ArrayList<>();
    @BindView(R.id.main_iv_placeholder)
    SimpleDraweeView mIvPlaceholder; // 大图片

    @BindView(R.id.main_abl_app_bar)
    AppBarLayout mAblAppBar; // 整个可以滑动的AppBar


    @BindView(R.id.main_tb_toolbar)
    Toolbar mTbToolbar; // 工具栏


    @BindView(R.id.rv_banner)
    RecyclerView recyclerView;

    private RecyclerView.LayoutManager mLayoutManager;
    private BannerRecycleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_detail);
        ButterKnife.bind(this);
        ultimateBar.setTransparentBar(
                ContextCompat.getColor(UIUtils.getContext(),
                        R.color.colorPrimary),
                0);
        // AppBar的监听
        /*mAblAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
                //handleAlphaOnTitle(percentage);
                handleToolbarTitleVisibility(percentage);
            }
        });*/
        // or 网格布局，可以设置列数和方向，是否反向显示
        mLayoutManager = new GridLayoutManager(this, 3,
                LinearLayoutManager.VERTICAL, false);
        initIntentValue(); //接受intent参数

        //initParallaxValues(); // 自动滑动效果
    }

    /**
     * 用于根据传递过来的值初始化控件
     */
    private void initIntentValue() {
        try {
            String url = getIntent().getStringExtra("url");
            String message = getIntent().getStringExtra("desc");
            final String id = getIntent().getStringExtra("id");
            String title = getIntent().getStringExtra("title");
            final String type = getIntent().getStringExtra("type");
            Uri uri = Uri.parse(url);
            mIvPlaceholder.setImageURI(uri);
            mAdapter = new BannerRecycleAdapter(picList,
                    UIUtils.getContext()
            );
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
            getDataFromServere(type, id);
            mTbToolbar.setTitle(title);
            setSupportActionBar(mTbToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            mAdapter.setRefreshListener(new BannerRecycleAdapter.onLoadmoreListener() {
                @Override
                public void onLoadMore(int position) {
                    if (skip < 300) {
                        skip += 30;
                        getDataFromServere(type, id);
                    } else {
                        skip = 0;
                        mAdapter.isFinish(true);
                    }
                }
            });

            swipBanner.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getDataFromServere(type,id);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getDataFromServere(final String type, final String id) {
        RetrofitHelper
                .getInstance(this)
                .getInterface(BannerInterface.class)
                .bannerModle(type, id, 30, skip, false, "hot")
                .enqueue(new Callback<BannerModle>() {
                    @Override
                    public void onResponse(Call<BannerModle> call, Response<BannerModle> response) {
                        List<BannerModle.ResBean.WallpaperBean> mBeanList =
                                response.body().getRes().getWallpaper();
                        picList.addAll(mBeanList);
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setOnItemClickListener(new BannerRecycleAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position, Uri uri) {
                                if (null == picUrlList) {
                                    return;
                                }
                                picUrlList.clear();
                                IdList.clear();
                                for (int i = 0; i < picList.size(); i++) {
                                    if (picUrlList != null) {
                                        picUrlList.add(picList.get(i).getImg());
                                    }
                                    IdList.add(picList.get(i).getId());
                                }
                                Intent intent = new Intent(UIUtils.getContext(),
                                        ImageLoadingActivity.class);
                                intent.putExtra("position", position);
                                intent.putExtra("itemCount", mAdapter.getItemCount());
                                intent.putExtra("id", picList.get(position).getId());
                                intent.putStringArrayListExtra("picList", picUrlList);
                                intent.putStringArrayListExtra("picIdList", IdList);
                                startActivity(intent);
                            }
                        });

                        if (swipBanner.isRefreshing()){
                            swipBanner.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<BannerModle> call, Throwable t) {
                        if (swipBanner.isRefreshing()){
                            swipBanner.setRefreshing(false);
                        }
                    }
                });
    }

    /*// 设置自动滑动的动画效果
    private void initParallaxValues() {
        CollapsingToolbarLayout.LayoutParams petDetailsLp =
                (CollapsingToolbarLayout.LayoutParams) mIvPlaceholder.getLayoutParams();

        *//*CollapsingToolbarLayout.LayoutParams petBackgroundLp =
                (CollapsingToolbarLayout.LayoutParams) mFlTitleContainer.getLayoutParams();*//*

        petDetailsLp.setParallaxMultiplier(0.9f);
        petBackgroundLp.setParallaxMultiplier(0.3f);

        mIvPlaceholder.setLayoutParams(petDetailsLp);
        //mFlTitleContainer.setLayoutParams(petBackgroundLp);
    }*/

    // 处理ToolBar的显示
    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!mIsTheTitleVisible) {
                /*startAlphaAnimation(mTvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(imageBack, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);*/
                mIsTheTitleVisible = true;
            }
        } else {
            if (mIsTheTitleVisible) {
                /*startAlphaAnimation(mTvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(imageBack, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);*/
                mIsTheTitleVisible = false;
            }
        }
    }

    /*// 控制Title的显示
    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mLlTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mLlTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }*/

    // 设置渐变的动画
    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
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
}
