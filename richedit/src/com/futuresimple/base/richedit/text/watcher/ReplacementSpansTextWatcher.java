package com.futuresimple.base.richedit.text.watcher;

import com.futuresimple.base.richedit.text.style.util.SpansUtil;

import android.text.Editable;
import android.text.Spannable;
import android.text.style.ReplacementSpan;

public final class ReplacementSpansTextWatcher extends BaseRichTextWatcher {
  private boolean mRemovingReplacementSpan;

  @Override
  public final void beforeTextRemoving(final Spannable s, final String toRemove, final int position) {
    mRemovingReplacementSpan = false;
    final int length = toRemove.length();
    if (length == 1) {
      final ReplacementSpan[] spans = s.getSpans(position - 1, position - 1, ReplacementSpan.class);
      mRemovingReplacementSpan = (spans.length == 1) && (s.getSpanEnd(spans[0]) == position + 1);
    } else {
      // -1 : do not touch neighbour from the right
      SpansUtil.removeAllSpansFrom(s, position + 1, position + length - 1, ReplacementSpan.class);
    }
  }

  @Override
  public final void onTextRemoved(final Editable s, final String removed, final int pos) {
    if (mRemovingReplacementSpan) {
      final ReplacementSpan[] spans = s.getSpans(pos - 1, pos - 1, ReplacementSpan.class);
      if (spans.length == 1) {
        final int start = s.getSpanStart(spans[0]);
        final int end = s.getSpanEnd(spans[0]);
        s.removeSpan(spans[0]);
        s.delete(start, end);
      }
    }
  }
}