package com.futuresimple.base.richedit.text.watcher;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;

public abstract class BaseRichTextWatcher implements TextWatcher {

  private String mRemovedText;
  private String mAddedText;
  private int mStart;

  @Override
  public final void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    mRemovedText = (count > 0) ? s.toString().substring(start, start + count) : null;
    if (!TextUtils.isEmpty(mRemovedText)) {
      beforeTextRemoving((SpannableStringBuilder) s, mRemovedText, start);
    }
  }

  @Override
  public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    mAddedText = (count > 0) ? s.toString().substring(start, start + count) : null;
    mStart = start;
  }

  @Override
  public final void afterTextChanged(final Editable s) {
    if (!TextUtils.isEmpty(mRemovedText)) {
      onTextRemoved(s, mRemovedText, mStart);
    }
    if (!TextUtils.isEmpty(mAddedText)) {
      onTextAdded(s, mAddedText, mStart);
    }
  }

  public void beforeTextRemoving(final Spannable s, final String toRemove, final int position) {
  }

  public void onTextRemoved(final Editable s, final String removed, final int start) {
  }

  public void onTextAdded(final Editable s, final String added, final int start) {
  }
}
