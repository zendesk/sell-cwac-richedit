package com.futuresimple.base.richedit.text.style;

import android.os.Parcel;

public class URLSpan extends android.text.style.URLSpan {

  public final String title;
  public final String target;

  public URLSpan(String url, String title, String target) {
    super(url);
    this.title = title;
    this.target = target;
  }

  public URLSpan(Parcel src) {
    super(src);
    title = src.readString();
    target = src.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(title);
    dest.writeString(target);
  }
}
