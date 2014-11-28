package com.commonsware.cwac.richedit;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.ListSpan;
import com.futuresimple.base.richedit.text.style.NumberSpan;
import com.futuresimple.base.richedit.text.style.OrderedListSpan;
import com.futuresimple.base.richedit.text.style.UnorderedListSpan;

import android.text.Editable;
import android.text.Spannable;

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

  private void removeEmptyLines(final Editable str, final Selection selection) {

    // EXAMPLE:
    //
    // The list for
    // aaa\n
    // \n
    // bbb\n
    // \n
    // ccc\n
    //
    // will look like
    // * aaa\n
    // * bbb\n
    // * ccc\n

    for (int i = selection.start; i < selection.end; i++) {
      if ((str.charAt(i) == '\n') && (str.charAt(i+1) == '\n')) {
        str.replace(i, i + 1, "");
        selection.end--;
      }
    }
  }

  private void extendListSelectionProperly(final CharSequence str, final Selection selection) {
    // in case when we have partly selected the line
    // we need to start the list from it's beginning
    if (selection.start > 0) {
      for (int i = selection.start; i >= 0; i--) {
        if (str.charAt(i) == '\n') {
          if (i == selection.start) {
            continue;
          }
          selection.setStart((i == selection.start) ? i : i + 1);
          break;
        }
        if (i == 0) {
          selection.setStart(0);
        }
      }
    }

    // the same approach we will use for the end of selection
    for (int i = selection.end; i < str.length(); i++) {
      if (str.charAt(i) == '\n') {
        selection.setEnd(i);
        break;
      }
      if (i == str.length() - 1) {
        selection.setEnd(str.length() - 1);
      }
    }
  }

  @Override
  void applyToSelection(final RichEditText editor, final ListSpan.Type listSpan) {
    final Selection selection = new Selection(editor);
    final Editable str = editor.getText();

    removeEmptyLines(str, selection);
    extendListSelectionProperly(str, selection);
    removeListSpans(selection, str);

    if (listSpan != null) {
      final ListSpan span = listSpan == ListSpan.Type.ORDERED
          ? new OrderedListSpan()
          : new UnorderedListSpan();

      str.setSpan(span, selection.start, selection.end, SPAN_EXCLUSIVE_EXCLUSIVE);
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
    if (span instanceof OrderedListSpan) {
      OrderedListSpan.newList();
    }

    int start = selection.start;
    int end = selection.end;

    for (int i = start; i <= end; i++) {
      if (str.charAt(i) == '\n') {
        final Object itemSpan =
            (span instanceof OrderedListSpan)
                ? new NumberSpan(OrderedListSpan.getNextListItemIndex())
                : (span instanceof UnorderedListSpan)
                    ? new BulletSpan()
                    : null;

        if (itemSpan != null) {
          str.setSpan(itemSpan, start, i, SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        start = i + 1;
      }
    }
  }

}
