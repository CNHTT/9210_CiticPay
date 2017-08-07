// Generated code from Butter Knife. Do not modify!
package com.android.citic.appmanager.history;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class HistoryTrans$$ViewBinder<T extends com.android.citic.appmanager.history.HistoryTrans> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296382, "field 'lv_trans'");
    target.lv_trans = finder.castView(view, 2131296382, "field 'lv_trans'");
    view = finder.findRequiredView(source, 2131296383, "field 'view_nodata'");
    target.view_nodata = view;
    view = finder.findRequiredView(source, 2131296380, "field 'search_edit'");
    target.search_edit = finder.castView(view, 2131296380, "field 'search_edit'");
    view = finder.findRequiredView(source, 2131296381, "field 'search'");
    target.search = finder.castView(view, 2131296381, "field 'search'");
    view = finder.findRequiredView(source, 2131296379, "field 'z'");
    target.z = finder.castView(view, 2131296379, "field 'z'");
  }

  @Override public void unbind(T target) {
    target.lv_trans = null;
    target.view_nodata = null;
    target.search_edit = null;
    target.search = null;
    target.z = null;
  }
}
