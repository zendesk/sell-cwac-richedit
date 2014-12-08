package com.futuresimple.base.richedit.text.watcher;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;

public abstract class BaseRichTextWatcher implements TextWatcher {

  private String mRemovedText;
  private String mAddedText;
  private int mStart;

  public final String getRemovedText() {
    return mRemovedText;
  }

  public final String getAddedText() {
    return mAddedText;
  }

  public final int getStart() {
    return mStart;
  }

  @Override
  public final void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    mRemovedText = (count > 0) ? s.toString().substring(start, start + count) : null;
    beforeTextChanged((SpannableStringBuilder) s, start, start + count);
  }

  @Override
  public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    mAddedText = (count > 0) ? s.toString().substring(start, start + count) : null;
    mStart = start;
    onTextChanged((SpannableStringBuilder) s, start);
  }

  @Override
  public void afterTextChanged(final Editable s) {
  }

  public void beforeTextChanged(final Spannable s, final int start, final int end) {
  }

  public void onTextChanged(final Spannable s, final int start) {
  }

}
