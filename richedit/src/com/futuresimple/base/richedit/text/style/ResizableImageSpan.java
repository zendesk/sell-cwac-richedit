package com.futuresimple.base.richedit.text.style;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Subclass of ImageSpan that resizes images automatically to fit the container's width, and then
 * re-calculate the size of the image to let TextView know how much space it needs to display
 * the resized image.
 */
public class ResizableImageSpan extends ImageSpan {

  private WeakReference<Drawable> mDrawableRef;
  private int mContainerWidth;

  public ResizableImageSpan(final Drawable drawable, final String source, final int containerWidth) {
    super(drawable, source);
    mContainerWidth = containerWidth;
  }

  @Override
  public final int getSize(final Paint paint, final CharSequence text, final int start, final int end, final Paint.FontMetricsInt fm) {
    final Drawable d = getCachedDrawable();
    final Rect rect = getResizedDrawableBounds(d);

    if (fm != null) {
      fm.ascent = -rect.bottom;
      fm.descent = 0;

      fm.top = fm.ascent;
      fm.bottom = 0;
    }

    return rect.right;
  }

  private Rect getResizedDrawableBounds(final Drawable drawable) {
    if (drawable == null || drawable.getIntrinsicWidth() == 0) {
      return new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }
    int scaledHeight;

    if (drawable.getIntrinsicWidth() > mContainerWidth) {
      // Image is larger than the container's width, scale down to fit the container.
      int scaledWidth = mContainerWidth;
      scaledHeight = drawable.getIntrinsicHeight() * scaledWidth / drawable.getIntrinsicWidth();
      drawable.setBounds(0, 0, scaledWidth, scaledHeight);
    }

    return drawable.getBounds();
  }

  private Drawable getCachedDrawable() {
    final WeakReference<Drawable> wr = mDrawableRef;
    Drawable drawable = null;

    if (wr != null) {
      drawable = wr.get();
    }

    if (drawable == null) {
      drawable = getDrawable();
      mDrawableRef = new WeakReference<>(drawable);
    }

    return drawable;
  }

  public final Bitmap getCachedBitmap() {
    final Drawable drawable = getCachedDrawable();
    if (drawable != null && drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    return null;
  }

  public void setWidth(int width) {
    mContainerWidth = width;
  }
}
