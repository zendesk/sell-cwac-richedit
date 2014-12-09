package com.futuresimple.base.richedit.text.style.util;

import android.text.Spannable;

public class SpansUtil {

  public static <T> void removeAllSpansFrom(final Spannable s, final int start, final int end, final Class<T> kind) {
    final T[] spans = s.getSpans(start, end, kind);
    for (final T span : spans) {
      s.removeSpan(span);
    }
  }

}
