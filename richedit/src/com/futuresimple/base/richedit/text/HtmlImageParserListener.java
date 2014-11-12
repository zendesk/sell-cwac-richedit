package com.futuresimple.base.richedit.text;

import android.graphics.Bitmap;

public interface HtmlImageParserListener {
  public void onImageLoadingStarted(final int start, final int end, final String source);
  public void onImageLoadingFailed(final int start, final int end, final String source);
  public void onImageLoaded(final int start, final int end, final String source, final Bitmap bitmap);
  public void onImageLoadingCancelled(final int start, final int end, final String source);
}