// Generated code from Butter Knife. Do not modify!
package com.android.citic.appmanager.trans;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class Resultctl$$ViewBinder<T extends com.android.citic.appmanager.trans.Resultctl> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296296, "field 'detail'");
    target.detail = finder.castView(view, 2131296296, "field 'detail'");
    view = finder.findRequiredView(source, 2131296295, "field 'title'");
    target.title = finder.castView(view, 2131296295, "field 'title'");
    view = finder.findRequiredView(source, 2131296294, "field 'face'");
    target.face = finder.castView(view, 2131296294, "field 'face'");
  }

  @Override public void unbind(T target) {
    target.detail = null;
    target.title = null;
    target.face = null;
  }
}
