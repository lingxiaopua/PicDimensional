package com.lingxiaosuse.picture.tudimension.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.camera.lingxiao.common.app.ContentValue;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.modle.SearchResultModle;

import java.util.List;

/**
 * Created by lingxiao on 2017/9/30.
 */

public class SearchRecyAdapter extends BaseRecycleAdapter{

    public SearchRecyAdapter(List mList,int headCont,int footCount) {
        super(mList,headCont,footCount);
    }
    @Override
    public void bindData(BaseViewHolder holder, int position, List mList) {
        try{
            List<SearchResultModle.WallPaper> wallPapers = mList;
            SimpleDraweeView simpleDraweeView = (SimpleDraweeView) holder.getView(R.id.iv_meizi_img);
            TextView textView = (TextView) holder.getView(R.id.tv_meizi_des);
            String url = wallPapers.get(position).getImg();
            if (!url.contains("http://")){
                url = "http://img0.adesk.com/download/"+url;
            }
            Uri uri = Uri.parse(url+ ContentValue.imgRule);
            simpleDraweeView.setImageURI(uri);
            if (wallPapers.get(position).getDesc().isEmpty()){
                textView.setVisibility(View.GONE);
            }else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(wallPapers.get(position).getDesc());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.hot_item;
    }

    @Override
    public int getHeadLayoutId() {
        return 0;
    }
}
