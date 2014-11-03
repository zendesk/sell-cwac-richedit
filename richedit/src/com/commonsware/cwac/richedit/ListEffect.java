package com.commonsware.cwac.richedit;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.ListSpan;
import com.futuresimple.base.richedit.text.style.NumberSpan;
import com.futuresimple.base.richedit.text.style.OrderedListSpan;
import com.futuresimple.base.richedit.text.style.UnorderedListSpan;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import java.util.Arrays;
import java.util.List;

public class ListEffect extends Effect<ListSpan.Type, ListSpan> {

  @Override
  boolean existsInSelection(final RichEditText editor) {
    return valueInSelection(editor) != null;
  }

  @Override
  ListSpan.Type valueInSelection(final RichEditText editor) {
    final List<ListSpan> effects = getAllEffectsFrom(editor.getText(), new Selection(editor));
    if (effects.isEmpty()) {
      return null;
    } else {
      return effects.get(0).getListType();
    }
  }

  @Override
  void applyToSelection(final RichEditText editor, final ListSpan.Type listSpan) {
    final Selection selection = new Selection(editor);
    final Spannable str = editor.getText();

    removeListSpans(selection, str);

    if (listSpan != null) {
      final ListSpan span = listSpan == ListSpan.Type.ORDERED
          ? new OrderedListSpan()
          : new UnorderedListSpan();

      str.setSpan(span, selection.start, selection.end, SPAN_INCLUSIVE_INCLUSIVE);
      applyListItemSpan(str, selection, span);
    }
  }

  @Override
  public final ListSpan newEffect() {
    throw new UnsupportedOperationException("newEffect() is not supported in ListEffect!");
  }

  @Override
  public final List<ListSpan> getAllEffectsFrom(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, ListSpan.class));
  }

  private void removeListSpans(final Selection selection, final Spannable str) {
    for (final ListSpan span : getAllEffectsFrom(str, selection)) {
      str.removeSpan(span);
    }

    for (final BulletSpan span : str.getSpans(selection.start, selection.end, BulletSpan.class)) {
      str.removeSpan(span);
    }

    for (final NumberSpan span : str.getSpans(selection.start, selection.end, NumberSpan.class)) {
      str.removeSpan(span);
    }
  }

  private void applyListItemSpan(final Spannable str, final Selection selection, final ListSpan span) {
    int start = selection.start;
    int end = selection.end;
    if (end > 0 && str.charAt(end - 1) != '\n') {
      ((SpannableStringBuilder) str).append('\n');
      end++;
    }
    if (span instanceof OrderedListSpan) {
      OrderedListSpan.newList();
    }
    for (int i = start; i < end; i++) {
      if (str.charAt(i) == '\n') {
        if (i + 1 < end && str.charAt(i + 1) == '\n') continue;
        Object itemSpan = null;
        if (span instanceof OrderedListSpan) {
          itemSpan = new NumberSpan(OrderedListSpan.getNextListItemIndex());
        } else if (span instanceof UnorderedListSpan) {
          itemSpan = new BulletSpan();
        }
        if (itemSpan != null) {
          str.setSpan(itemSpan, start, i, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        start = i + 1;
      }
    }
  }

}
