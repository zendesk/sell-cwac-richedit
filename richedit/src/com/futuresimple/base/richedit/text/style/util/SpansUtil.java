package com.futuresimple.base.richedit.text.style.util;

import com.commonsware.cwac.richedit.Selection;

import android.text.Spannable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpansUtil {

  public static <T> void removeAllSpansFrom(final Spannable s, final int start, final int end, final Class<T> kind) {
    final T[] spans = s.getSpans(start, end, kind);
    for (final T span : spans) {
      s.removeSpan(span);
    }
  }

  public static <T> List<T> getSpans(final Spannable s, final Selection selection, final Class<T> kind) {
    return Arrays.asList(s.getSpans(selection.start, selection.end, kind));
  }

  public static <T> List<T> getSpansByOrder(final Spannable s, final Selection selection, final Class<T> kind) {
    final List<T> spansList = getSpans(s, selection, kind);
    Collections.sort(spansList, new SpansComparator<T>(s));
    return spansList;
  }

  public static <T> T getLastSpanAt(final Spannable s, final Selection selection, final Class<T> kind) {
    return getLastSpanAt(s, selection.start, selection.end, kind);
  }

  public static <T> T getLastSpanAt(final Spannable s, final int start, final int end, final Class<T> kind) {
    final List<T> spans = getSpansByOrder(s, new Selection(start, end), kind);
    return spans.isEmpty() ? null : spans.get(spans.size() - 1);
  }

  public static int getSpanSize(final Spannable s, final Object span) {
    return s.getSpanEnd(span) - s.getSpanStart(span);
  }
}
