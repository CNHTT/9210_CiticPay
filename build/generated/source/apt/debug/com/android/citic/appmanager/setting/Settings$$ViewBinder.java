// Generated code from Butter Knife. Do not modify!
package com.android.citic.appmanager.setting;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class Settings$$ViewBinder<T extends com.android.citic.appmanager.setting.Settings> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296356, "field 'mGrid'");
    target.mGrid = finder.castView(view, 2131296356, "field 'mGrid'");
    view = finder.findRequiredView(source, 2131296355, "field 'rootLayout'");
    target.rootLayout = finder.castView(view, 2131296355, "field 'rootLayout'");
  }

  @Override public void unbind(T target) {
    target.mGrid = null;
    target.rootLayout = null;
  }
}
