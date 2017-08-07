package com.android.citic.lib.stagedview.adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.libapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by zhouqiang on 2017/3/17.
 */

public class StagedAdapter extends BaseAdapter{

    public interface Constants{
        static final String TAG = "StagedAdapter" ;
        static final String KEY_IV = "key_iv" ;
        static final String KEY_TV = "key_tv" ;
        static final String KEY_IV2 = "key_iv2" ;
    }

    private ArrayList<HashMap<String,Object>> mData = new ArrayList<>();
    private HashMap<String,Object> map = new HashMap<>();
    private Context mContext ;

    public StagedAdapter(Context c , ArrayList<HashMap<String,Object>> l){
        this.mContext = c ;
        this.mData = l ;
        mRandom = new Random();
        mBackgroundColors = new ArrayList<Integer>();
        mBackgroundColors.add(R.color.staged_orange);
        mBackgroundColors.add(R.color.staged_green);
        mBackgroundColors.add(R.color.staged_blue);
        mBackgroundColors.add(R.color.staged_yellow);
        mBackgroundColors.add(R.color.staged_grey);
    }

    @Override
    public int getCount() {
        return mData.size() ;
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        map = mData.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.staged_list_item, null);
            holder = new ViewHolder();
            holder.txtLineOne = (DynamicHeightTextView) convertView.findViewById(R.id.staged_item_text);
            holder.imageView = (ImageView) convertView.findViewById(R.id.staged_item_img);
//            holder.bgLayout = (RelativeLayout) convertView.findViewById(R.id.panel_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        double positionHeight = getPositionRatio(position);
//        int backgroundIndex = position >= mBackgroundColors.size() ?
//                position % mBackgroundColors.size() : position;
//        convertView.setBackgroundResource(mBackgroundColors.get(backgroundIndex));
//        holder.txtLineOne.setHeightRatio(positionHeight);
//        holder.bgLayout.setBackgroundResource((int)map.get(Constants.KEY_IV2));
        holder.txtLineOne.setText(map.get(Constants.KEY_TV).toString());
        holder.imageView.setBackgroundResource((int)map.get(Constants.KEY_IV));
        return convertView;
    }


    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;
    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
            Log.d(Constants.TAG, "getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5 the width
    }

    static class ViewHolder{
        private DynamicHeightTextView txtLineOne;
        private ImageView imageView ;
//        private RelativeLayout bgLayout ;
    }
}
