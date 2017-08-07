// Generated code from Butter Knife. Do not modify!
package com.android.citic.appmanager;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class Operator$$ViewBinder<T extends com.android.citic.appmanager.Operator> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296261, "field 'account'");
    target.account = finder.castView(view, 2131296261, "field 'account'");
    view = finder.findRequiredView(source, 2131296262, "field 'pass'");
    target.pass = finder.castView(view, 2131296262, "field 'pass'");
    view = finder.findRequiredView(source, 2131296263, "field 'btn'");
    target.btn = finder.castView(view, 2131296263, "field 'btn'");
  }

  @Override public void unbind(T target) {
    target.account = null;
    target.pass = null;
    target.btn = null;
  }
}
