package com.commonsware.cwac.richedit;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_EXCLUSIVE_INCLUSIVE;

import com.futuresimple.base.richedit.text.EffectsHandler;
import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.ListSpan;
import com.futuresimple.base.richedit.text.style.ListSpan.Type;
import com.futuresimple.base.richedit.text.style.NumberSpan;
import com.futuresimple.base.richedit.text.style.OrderedListSpan;
import com.futuresimple.base.richedit.text.style.UnorderedListSpan;

import android.text.Editable;
import android.text.Spannable;

import java.util.ArrayList;
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
    final Editable str = editor.getText();

    // remove empty lines inside selection
    EffectsHandler.removeEmptyLines(str, selection);

    // select all lines from begin to the end
    EffectsHandler.extendSelectionToTheLineWidth(str, selection);

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
    return getAllListMarks(text, selection);
  }

  private static List<ListSpan> getAllListMarks(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, ListSpan.class));
  }

  private static void removeListMarks(final Spannable str, final Selection selection) {
    final List<ListSpan> lists = getAllListMarks(str, selection);
    for (final ListSpan span : lists) {
      str.removeSpan(span);
    }
  }

  private void removeListSpans(final Selection selection, final Spannable str) {
    removeListMarks(str, selection);

    for (final BulletSpan span : str.getSpans(selection.start, selection.end, BulletSpan.class)) {
      str.removeSpan(span);
    }

    for (final NumberSpan span : str.getSpans(selection.start, selection.end, NumberSpan.class)) {
      str.removeSpan(span);
    }
  }

  private void applyItemSpan(final Spannable str, final int start, final int end, final ListSpan span, final int spanType) {
    final Object itemSpan =
        (span.getListType() == Type.ORDERED)
            ? new NumberSpan(OrderedListSpan.getNextListItemIndex())
            : (span.getListType() == Type.UNORDERED)
                ? new BulletSpan()
                : null;

    if (itemSpan != null) {
      str.setSpan(itemSpan, start, end, spanType);
    }
  }

  private void applyListItemSpan(final Editable str, final Selection selection, final ListSpan span) {
    if (span.getListType() == Type.ORDERED) {
      OrderedListSpan.newList();
    }

    if (selection.isEmpty()) {
      applyItemSpan(str, selection.start, selection.end, span, SPAN_EXCLUSIVE_INCLUSIVE);
    } else {
      int start = selection.start;
      int end = selection.end;

      for (int i = start; i <= end; i++) {
        if (str.charAt(i) == '\n') {
          applyItemSpan(str, start, i, span, SPAN_EXCLUSIVE_EXCLUSIVE);
          start = i + 1;
        }
      }
    }
  }

  public static void sanitizeBulletLists(final Spannable str) {
    removeListMarks(str, new Selection(0, str.length()));

    final List<BulletSpan> bullets = EffectsHandler.getSpansByOrder(str, new Selection(0, str.length()), BulletSpan.class);

    final List<Integer> starts = new ArrayList<>();
    final List<Integer> ends = new ArrayList<>();

    if (bullets.size() > 0) {
      if (bullets.size() == 1) {
        str.setSpan(new UnorderedListSpan(), str.getSpanStart(bullets.get(0)), str.getSpanEnd(bullets.get(0)), SPAN_EXCLUSIVE_EXCLUSIVE);
      } else {
        for (int i = 0; i < bullets.size(); i++) {
          if (i == 0) {
            starts.add(str.getSpanStart(bullets.get(i)));
          } else if (i == bullets.size() - 1) {
            ends.add(str.getSpanEnd(bullets.get(i)));
          } else {
            final int currentStart = str.getSpanStart(bullets.get(i));
            final int previousEnd = str.getSpanEnd(bullets.get(i - 1));
            if (currentStart - previousEnd > 1) {
              ends.add(previousEnd);
              starts.add(currentStart);
            }
          }
        }
      }
    }

    // starts and ends sizes has to be equal
    for (int k = 0; k < ends.size(); k++) {
      str.setSpan(new UnorderedListSpan(), starts.get(k), ends.get(k), SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }
}
