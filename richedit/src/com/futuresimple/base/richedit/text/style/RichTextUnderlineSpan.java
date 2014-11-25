package com.futuresimple.base.richedit.text.style;

import android.text.style.UnderlineSpan;

public class RichTextUnderlineSpan extends UnderlineSpan {

  // We need this class to resolve conflict with the Android Spell Checker.
  // ASC uses underline to highlight words when typing. So our RichEditText
  // also handles it like turning underline on. Now we will turn underline
  // for the instance of RichTextUnderlineSpan only.

}
