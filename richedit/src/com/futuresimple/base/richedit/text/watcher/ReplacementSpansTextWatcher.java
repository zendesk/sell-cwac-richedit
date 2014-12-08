package com.futuresimple.base.richedit.text.watcher;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

public final class ReplacementSpansTextWatcher extends BaseRichTextWatcher {
  private boolean mRemovingReplacementSpan;

  @Override
  public final void beforeTextChanged(final Spannable s, final int start, final int end) {
    mRemovingReplacementSpan = false;
    if (!TextUtils.isEmpty(getRemovedText()) && (end - start == 1)) {
      final ReplacementSpan[] spans = s.getSpans(start, start, ReplacementSpan.class);
      mRemovingReplacementSpan = (spans.length > 0) && (s.getSpanEnd(spans[0]) == end);
    }
  }

  @Override
  public final void afterTextChanged(final Editable s) {
    if (mRemovingReplacementSpan) {
      final ReplacementSpan[] spans = s.getSpans(getStart(), getStart(), ReplacementSpan.class);
      if (spans.length > 0) {
        final int start = s.getSpanStart(spans[0]);
        final int end = s.getSpanEnd(spans[0]);
        s.removeSpan(spans[0]);
        s.delete(start, end);
      }
    }
  }
}