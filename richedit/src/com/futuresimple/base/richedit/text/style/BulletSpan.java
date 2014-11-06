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
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;

public class BulletSpan extends android.text.style.BulletSpan {

  private static final int STANDARD_BULLET_RADIUS = 6;
  private static final int STANDARD_GAP_WIDTH = 35;

  private static Path sBulletPath = null;

  private final int bulletRadius;
  private final int color;
  private final int gapWidth;
  private boolean mWantColor = false;

  public BulletSpan() {
    super();
    bulletRadius = STANDARD_BULLET_RADIUS;
    gapWidth = STANDARD_GAP_WIDTH;
    color = 0;
  }

  public BulletSpan(int bulletRadius) {
    super();
    this.bulletRadius = bulletRadius;
    gapWidth = STANDARD_GAP_WIDTH;
    color = 0;
  }

  public BulletSpan(int bulletRadius, int gapWidth) {
    super(gapWidth);
    this.bulletRadius = bulletRadius;
    this.gapWidth = gapWidth;
    this.color = 0;
  }

  public BulletSpan(int bulletRadius, int gapWidth, int color) {
    super(gapWidth, color);
    this.bulletRadius = bulletRadius;
    this.gapWidth = gapWidth;
    this.color = color;
    mWantColor = true;
  }

  public BulletSpan(Parcel src) {
    super(src);
    this.bulletRadius = src.readInt();
    this.gapWidth = src.readInt();
    this.color = src.readInt();
  }

  @Override
  public int getLeadingMargin(boolean first) {
    return 3 * bulletRadius + gapWidth;
  }

  @Override
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
      int top, int baseline, int bottom, CharSequence text, int start, int end,
      boolean first, Layout l) {
    if (((Spanned) text).getSpanStart(this) == start) {
      Paint.Style style = p.getStyle();
      int oldcolor = 0;

      if (mWantColor) {
        oldcolor = p.getColor();
        p.setColor(color);
      }

      p.setStyle(Paint.Style.FILL);

      if (c.isHardwareAccelerated()) {
        if (sBulletPath == null) {
          sBulletPath = new Path();
          // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
          sBulletPath.addCircle(0.0f, 0.0f, 1.2f * bulletRadius, Direction.CW);
        }

        c.save();
        c.translate(x + dir * 2 * bulletRadius, (top + bottom) / 2.0f);
        c.drawPath(sBulletPath, p);
        c.restore();
      } else {
        c.drawCircle(x + dir * bulletRadius, (top + bottom) / 2.0f, bulletRadius, p);
      }

      if (mWantColor) {
        p.setColor(oldcolor);
      }

      p.setStyle(style);
    }
  }
}
