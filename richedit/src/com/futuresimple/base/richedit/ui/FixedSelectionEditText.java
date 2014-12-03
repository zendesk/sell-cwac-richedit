package com.futuresimple.base.richedit.ui;

import android.content.Context;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class FixedSelectionEditText extends EditText {
  public FixedSelectionEditText(Context context) {
    super(context);
  }

  public FixedSelectionEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FixedSelectionEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    checkSelection(selStart, selEnd);
    int selectionStart = getSelectionStart();
    int selectionEnd = getSelectionEnd();
    super.onSelectionChanged(selectionStart, selectionEnd);
    onFixedSelectionChanged(selectionStart, selectionEnd);
  }

  private void checkSelection(int start, int end) {
    ReplacementSpan[] spans = getText().getSpans(start, end,
        ReplacementSpan.class);
    if (spans.length > 0) {
      int spanStart = getText().getSpanStart(spans[0]);
      int spanEnd = getText().getSpanEnd(spans[0]);
      // Selection is inside replacement span.
      if (start == end) {
        if (start - spanStart < spanEnd - start) {
          setSelection(spanStart);
        } else {
          setSelection(spanEnd);
        }
      } else if (spanStart < start || spanEnd > end) {
        setSelection(spanStart, spanEnd);
      }
    }
  }

  /*
   * Android has a bug which allows user to select part of the ReplacementSpan.
   * Override this method to get correct selection values
   */
  protected void onFixedSelectionChanged(int selStart, int selEnd) {
  }
}
