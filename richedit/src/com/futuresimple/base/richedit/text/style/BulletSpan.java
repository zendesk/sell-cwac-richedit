/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futuresimple.base.richedit.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

public class BulletSpan implements LeadingMarginSpan {
  public static final int STANDARD_GAP_WIDTH = 35;
  private static final int BULLET_RADIUS = 7;

  private final int mBulletRadius;
  private final int mGapWidth;
  private final int mColor;

  private final boolean mWantColor;

  private static Path sBulletPath = null;

  public BulletSpan() {
    this(BULLET_RADIUS, STANDARD_GAP_WIDTH, 0, false);
  }

  public BulletSpan(final int bulletRadius, final int gapWidth) {
    this(bulletRadius, gapWidth, 0, false);
  }

  public BulletSpan(final int bulletRadius, final int gapWidth, final int color) {
    this(bulletRadius, gapWidth, color, true);
  }

  public BulletSpan(final int bulletRadius, final int gapWidth, final int color, final boolean wantColor) {
    mBulletRadius = bulletRadius;
    mGapWidth = gapWidth;
    mColor = color;
    mWantColor = wantColor;
  }

  public final int getBulletRadius() {
    return mBulletRadius;
  }

  public final int getGapWidth() {
    return mGapWidth;
  }

  public final int getColor() {
    return mColor;
  }

  public final boolean isWantColor() {
    return mWantColor;
  }

  @Override
  public int getLeadingMargin(boolean first) {
    return mBulletRadius + 2 * mGapWidth;
  }

  @Override
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
      int top, int baseline, int bottom,
      CharSequence text, int start, int end,
      boolean first, Layout l) {

    x += getGapWidth();

    if (((Spanned) text).getSpanStart(this) == start) {
      Paint.Style style = p.getStyle();
      int oldColor = 0;

      if (mWantColor) {
        oldColor = p.getColor();
        p.setColor(mColor);
      }

      p.setStyle(Paint.Style.FILL);

      if (c.isHardwareAccelerated()) {
        if (sBulletPath == null) {
          sBulletPath = new Path();
          // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
          sBulletPath.addCircle(0.0f, 0.0f, 1.0f * mBulletRadius, Direction.CW);
        }

        c.save();
        c.translate(x + dir * mBulletRadius, (top + bottom) / 2.0f);
        c.drawPath(sBulletPath, p);
        c.restore();
      } else {
        c.drawCircle(x + dir * mBulletRadius, (top + bottom) / 2.0f, mBulletRadius, p);
      }

      if (mWantColor) {
        p.setColor(oldColor);
      }

      p.setStyle(style);
    }
  }
}