package com.commonsware.cwac.richedit;

import static android.text.Spanned.SPAN_PARAGRAPH;

import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.ListSpan;
import com.futuresimple.base.richedit.text.style.ListSpan.Type;
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
    final Selection selection = new Selection(editor);
    final List<ListSpan> effects = getAllEffectsFrom(editor.getText(), selection);
    if (effects.isEmpty() || (selection.isEmpty() && editor.getText().getSpanEnd(effects.get(effects.size() - 1)) == selection.end)) {
      return null;
    } else {
      return effects.get(0).getListType();
    }
  }

  private int fixParagraphBoundaries(final Editable s, int pos, final int increment) {
    if (pos > 0 && pos < s.length()) {
      final int anchor = (increment > 0) ? s.length() : 0;
      for (;;) {
        if (s.charAt(pos - 1) == '\n') {
          break;
        } else {
          pos += increment;
          if (pos == anchor) {
            break;
          }
        }
      }
    }

    return pos;
  }

  public final void applyToSelection(final Editable s, final Selection selection, final ListSpan.Type type) {

    int start = fixParagraphBoundaries(s, selection.start, -1);

    removeListMarks(s, selection);

    if (type != null) {
      final ListSpan span = type == ListSpan.Type.ORDERED
          ? new OrderedListSpan()
          : new UnorderedListSpan();

      int end = fixParagraphBoundaries(s, selection.isEmpty() ? selection.end + 1 : selection.end, 1);

      s.setSpan(span, start, end, SPAN_PARAGRAPH);
      applyListItemSpan(s, new Selection(start, end), span);
    }
  }

  @Override
  void applyToSelection(final RichEditText editor, final ListSpan.Type listSpan) {
    applyToSelection(editor.getText(), new Selection(editor), listSpan);
  }

  @Override
  public final ListSpan newEffect() {
    throw new UnsupportedOperationException("newEffect() is not supported in ListEffect!");
  }

  @Override
  public final List<ListSpan> getAllEffectsFrom(final Spannable text, final Selection selection) {
    return getAllListMarks(text, selection);
  }

  private static List<ListSpan> getAllListMarks(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, ListSpan.class));
  }

  public static void removeListMarks(final Spannable str, final Selection selection) {
    final List<ListSpan> lists = getAllListMarks(str, selection);
    for (final ListSpan span : lists) {
      str.removeSpan(span);
      for (final Object item : span.getItems()) {
        str.removeSpan(item);
      }
    }
  }

  private static void applyItemSpan(final Spannable str, final int start, final int end, final ListSpan span, final int spanType) {
    if (end <= str.getSpanEnd(span)) {
      final Object itemSpan =
          (span.getListType() == Type.ORDERED)
              ? new NumberSpan(OrderedListSpan.getNextListItemIndex())
              : (span.getListType() == Type.UNORDERED)
                  ? new BulletSpan()
                  : null;

      if (itemSpan != null) {
        str.setSpan(itemSpan, start, end, spanType);
        span.addItem(itemSpan);
      }
    }
  }

  private void applyListItemSpan(final Editable str, final Selection selection, final ListSpan span) {
    if (span.getListType() == Type.ORDERED) {
      OrderedListSpan.newList();
    }

    if (selection.isEmpty()) {
      applyItemSpan(str, selection.start, selection.end + 1, span, SPAN_PARAGRAPH);
    } else {
      int start = selection.start;
      int end = selection.end;

      int lookup = (end > 0 && str.charAt(end - 1) == '\n')
          ? end - 1 : end;

      for (int i = start; i <= lookup && i < str.length(); i++) {
        if (str.charAt(i) == '\n') {
          applyItemSpan(str, start, i + 1, span, SPAN_PARAGRAPH);
          start = i + 1;
        }
      }
    }
  }

}
