package com.futuresimple.base.richedit.text.style.util;

import android.text.Spannable;

import java.util.Comparator;

public class SpansComparator<T> implements Comparator<T> {

  private final Spannable mSpannable;

  public SpansComparator(final Spannable spannable) {
    mSpannable = spannable;
  }

  @Override
  public final int compare(T lhs, T rhs) {
    return mSpannable.getSpanStart(lhs) - mSpannable.getSpanStart(rhs);
  }

}
