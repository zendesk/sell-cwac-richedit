package com.commonsware.cwac.richedit;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.ListSpan;
import com.futuresimple.base.richedit.text.style.NumberSpan;
import com.futuresimple.base.richedit.text.style.OrderedListSpan;
import com.futuresimple.base.richedit.text.style.UnorderedListSpan;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

public class ListEffect extends Effect<ListSpan.Type> {

  @Override
  boolean existsInSelection(final RichEditText editor) {
    return valueInSelection(editor) != null;
  }

  @Override
  ListSpan.Type valueInSelection(final RichEditText editor) {
    final Selection selection = new Selection(editor);
    final Spannable str = editor.getText();
    final ListSpan[] spans = getSpans(str, selection);

    if (spans.length > 0) {
      return spans[0].getListType();
    }

    return null;
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

      str.setSpan(span, selection.start, selection.end, SPAN_EXCLUSIVE_EXCLUSIVE);
      applyListItemSpan(str, selection, span);
    }
  }

  private void removeListSpans(final Selection selection, final Spannable str) {
    for (final ListSpan span : getSpans(str, selection)) {
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
    if (str.charAt(selection.end - 1) != '\n') {
      ((SpannableStringBuilder) str).append('\n');
      end++;
    }
    if (span instanceof OrderedListSpan) {
      OrderedListSpan.newList();
    }
    for (int i = start; i < end; i++) {
      if (str.charAt(i) == '\n') {
        if (i + 1 < end && str.charAt(i + 1) == '\n') continue;
        Object itemSpan;
        if (span instanceof OrderedListSpan) {
          itemSpan = new NumberSpan(OrderedListSpan.getNextListItemIndex());
        } else {
          itemSpan = new BulletSpan();
        }
        str.setSpan(itemSpan, start, i, SPAN_EXCLUSIVE_EXCLUSIVE);
        start = i + 1;
      }
    }
  }

  private ListSpan[] getSpans(final Spannable str, final Selection selection) {
    return str.getSpans(selection.start, selection.end, ListSpan.class);
  }
}
