// Generated code from Butter Knife. Do not modify!
package com.android.citic.appmanager;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class CiticPay$$ViewBinder<T extends com.android.citic.appmanager.CiticPay> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296284, "field 'settings'");
    target.settings = finder.castView(view, 2131296284, "field 'settings'");
    view = finder.findRequiredView(source, 2131296279, "field 'wp_main_pagernum'");
    target.wp_main_pagernum = finder.castView(view, 2131296279, "field 'wp_main_pagernum'");
    view = finder.findRequiredView(source, 2131296278, "field 'wp_main_pager'");
    target.wp_main_pager = finder.castView(view, 2131296278, "field 'wp_main_pager'");
  }

  @Override public void unbind(T target) {
    target.settings = null;
    target.wp_main_pagernum = null;
    target.wp_main_pager = null;
  }
}
