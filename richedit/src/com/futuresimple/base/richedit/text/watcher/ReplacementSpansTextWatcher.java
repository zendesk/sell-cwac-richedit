package com.futuresimple.base.richedit.text.watcher;

import android.text.Editable;
import android.text.Spannable;
import android.text.style.ReplacementSpan;

public final class ReplacementSpansTextWatcher extends BaseRichTextWatcher {
  private boolean mRemovingReplacementSpan;

  @Override
  public void beforeTextRemoving(Spannable s, String toRemove, int position) {
    mRemovingReplacementSpan = false;
    if (toRemove.length() == 1) {
      final ReplacementSpan[] spans = s.getSpans(position, position, ReplacementSpan.class);
      mRemovingReplacementSpan = (spans.length > 0) && (s.getSpanEnd(spans[0]) == position + 1);
    }
  }

  @Override
  public final void onTextRemoved(final Editable s, final String removed, final int start) {
    if (mRemovingReplacementSpan) {
      final ReplacementSpan[] spans = s.getSpans(start, start, ReplacementSpan.class);
      if (spans.length > 0) {
        final int spanStart = s.getSpanStart(spans[0]);
        final int spanEnd = s.getSpanEnd(spans[0]);
        s.delete(spanStart, spanEnd);
        s.removeSpan(spans[0]);
      }
    }
  }
}