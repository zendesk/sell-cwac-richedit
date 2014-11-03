package com.futuresimple.base.richedit.text;

import com.commonsware.cwac.richedit.Effect;
import com.commonsware.cwac.richedit.Selection;

import android.text.Spannable;

import java.util.List;

public class EffectsHandler {

  private final Effect mParentEffect;

  public EffectsHandler(final Effect effect) {
    mParentEffect = effect;
  }

  private void handleEmptySelectionOnSingleSpan(final Spannable text, final Object effect, final int cursorPos) {
    final int spanStart = text.getSpanStart(effect);
    final int spanEnd = text.getSpanEnd(effect);

    text.removeSpan(effect);

    if (spanStart == cursorPos) {

      // case: |[...] => |(...]
      text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

    } else if ((spanStart < cursorPos) && (cursorPos < spanEnd)) {

      // case: [...|...] => [...)|(...]
      text.setSpan(mParentEffect.newEffect(), spanStart, cursorPos, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
      text.setSpan(mParentEffect.newEffect(), cursorPos, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

    } else if (cursorPos == spanEnd) {

      // case: [...]| => [...)|
      text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }
  }

  public final void applyToSelection(final Spannable text, final Selection selection) {
    final List effects = mParentEffect.getAllEffectsFrom(text, selection);

    if (effects.isEmpty()) {
      text.setSpan(mParentEffect.newEffect(), selection.start, selection.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      return;
    }

    if (selection.isEmpty()) {
      if (effects.size() == 2) {

        // user has placed cursor between two spans
        // with the same style. so we will just
        // concatenate those spans.
        // case: [)|(] => []

        final int start = text.getSpanStart(effects.get(0));
        final int end = text.getSpanEnd(effects.get(1));
        text.removeSpan(effects.get(0));
        text.removeSpan(effects.get(1));
        text.setSpan(mParentEffect.newEffect(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      } else {

        // user has placed cursor near/inside the span

        handleEmptySelectionOnSingleSpan(text, effects.get(0), selection.end);
      }

    } else {
      final int firstSpanStart = text.getSpanStart(effects.get(0));
      final int lastSpanEnd = text.getSpanEnd(effects.get(effects.size()-1));

      // remove all previously added spans
      for (final Object effect : effects) {
        text.removeSpan(effect);
      }

      // handle uncovered span-parts

      if (firstSpanStart < selection.start) {
        text.setSpan(mParentEffect.newEffect(), firstSpanStart, selection.start, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
      }

      if (selection.end < lastSpanEnd) {
        text.setSpan(mParentEffect.newEffect(), selection.end, lastSpanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
      }
    }
  }

}
