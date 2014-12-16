package com.futuresimple.base.richedit.text.style;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class RichTextUnderlineSpan extends CharacterStyle implements UpdateAppearance {

  // We need this class to resolve conflict with the Android Spell Checker.
  // ASC uses underline to highlight words when typing. So our RichEditText
  // also handles it like turning underline on. Now we will turn underline
  // for the instance of RichTextUnderlineSpan only.

  public RichTextUnderlineSpan() {
  }

  @Override
  public final void updateDrawState(final TextPaint ds) {
    ds.setUnderlineText(true);
  }
}
