package com.lingxiaosuse.picture.tudimension.adapter;

import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lingxiaosuse.picture.tudimension.R;
import com.lingxiaosuse.picture.tudimension.utils.UIUtils;

import java.util.ArrayList;

/**
 * Created by lingxiao on 2017/8/29.
 */

public class ImageLoadAdapter extends PagerAdapter{
    private ArrayList<String> urlList;
    private SimpleDraweeView image;

    public ImageLoadAdapter(ArrayList<String> urlList){
        this.urlList = urlList;
    }
    @Override
    public int getCount() {
        return urlList==null?0:urlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = UIUtils.inflate(R.layout.pager_load);
        image = view.findViewById(R.id.simple_pager_load);
        Uri uri = Uri.parse(urlList.get(position));
        Log.i("code", "instantiateItem: 图片的地址"+urlList.get(position));
        image.setImageURI(uri);
        /*ViewGroup parent = (ViewGroup) v.getParent();
        if (parent !=null){
            parent.removeAllViews();
        }*/
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
