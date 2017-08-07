package com.android.citic.lib.poplist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

/**
 * zhouqiang 20170313
 */

public class PopupListAdapter extends BaseAdapter {

    ArrayList<PopupView> items = new ArrayList<PopupView>();

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = items.get(i).getPopupView();
        return view;
    }

    public void setItems(ArrayList<PopupView> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
