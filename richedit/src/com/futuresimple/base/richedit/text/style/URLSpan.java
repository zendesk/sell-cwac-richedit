package com.futuresimple.base.richedit.text.style;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.style.ClickableSpan;
import android.view.View;

public class URLSpan extends ClickableSpan {

  private final String mUrl;
  private final String mTitle;
  private final String mTarget;

  public URLSpan(final String url, final String title, final String target) {
    mUrl = url;
    mTitle = title;
    mTarget = target;
  }

  public final String getUrl() {
    return mUrl;
  }

  public final String getTitle() {
    return mTitle;
  }

  public final String getTarget() {
    return mTarget;
  }

  @Override
  public final void onClick(final View widget) {
    final Uri uri = Uri.parse(mUrl);
    final Context context = widget.getContext();
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
    context.startActivity(intent);
  }
}
