package com.futuresimple.base.richedit.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

public class NumberSpan implements LeadingMarginSpan, ParcelableSpan {

  public static final int STANDARD_GAP_WIDTH = 30;

  private final int index;
  private final int gapWidth;
  private final int color;
  private boolean wantColor = false;

  public NumberSpan(int index) {
    this.index = index;
    this.gapWidth = STANDARD_GAP_WIDTH;
    this.color = 0;
  }

  public NumberSpan(int index, int gapWidth) {
    this.index = index;
    this.gapWidth = gapWidth;
    this.color = 0;
  }

  public NumberSpan(int index, int gapWidth, int color) {
    this.index = index;
    this.gapWidth = gapWidth;
    this.color = color;
    wantColor = true;
  }

  public NumberSpan(Parcel parcel) {
    this.index = parcel.readInt();
    this.gapWidth = parcel.readInt();
    this.color = parcel.readInt();
  }

  @Override
  public int getLeadingMargin(boolean first) {
    return 2 * gapWidth;
  }

  @Override
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top,
      int baseline, int bottom, CharSequence text, int start, int end, boolean first,
      Layout layout) {
    if (((Spanned) text).getSpanStart(this) == start) {
      Paint.Style style = p.getStyle();

      int oldColor = 0;
      if (wantColor) {
        oldColor = p.getColor();
        p.setColor(color);
      }

      p.setStyle(Paint.Style.FILL);
      c.drawText(index + ".", x, baseline, p);
      c.save();
      c.translate(x * dir, (top + bottom) / 2.0f);
      c.restore();

      if (wantColor) {
        p.setColor(oldColor);
      }

      p.setStyle(style);
    }
  }

  @Override
  public int getSpanTypeId() {
    return ListSpan.SPAN_NUMBER;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(index);
  }
}
