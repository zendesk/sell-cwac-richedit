package com.futuresimple.base.richedit.text;

import com.commonsware.cwac.richedit.Effect;
import com.commonsware.cwac.richedit.R;
import com.commonsware.cwac.richedit.Selection;
import com.futuresimple.base.richedit.text.style.ResizableImageSpan;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;

public class EffectsHandler {

  public static final String CODE_ANCHOR_OPEN = "{{";
  public static final String CODE_ANCHOR_CLOSE = "}}";

  public static final String CODE_IMAGE_OPEN = CODE_ANCHOR_OPEN + "img:";

  private final Effect mParentEffect;

  public EffectsHandler(final Effect effect) {
    mParentEffect = effect;
  }

  public static int getEffectRelatedFlag(final int flags) {

    if ((flags & Spannable.SPAN_INCLUSIVE_INCLUSIVE) == Spannable.SPAN_INCLUSIVE_INCLUSIVE) {
      return Spannable.SPAN_INCLUSIVE_INCLUSIVE;
    }

    if ((flags & Spannable.SPAN_INCLUSIVE_EXCLUSIVE) == Spannable.SPAN_INCLUSIVE_EXCLUSIVE) {
      return Spannable.SPAN_INCLUSIVE_EXCLUSIVE;
    }

    if ((flags & Spannable.SPAN_EXCLUSIVE_INCLUSIVE) == Spannable.SPAN_EXCLUSIVE_INCLUSIVE) {
      return Spannable.SPAN_EXCLUSIVE_INCLUSIVE;
    }

    if ((flags & Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) == Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) {
      return Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
    }

    throw new IllegalStateException("Not supported span flags: " + flags);
  }

  private int openFromTheLeft(final int mode) {
    if (isOpenFromTheLeft(mode)) {
      return mode;
    } else {
      switch (mode) {

      case Spannable.SPAN_EXCLUSIVE_INCLUSIVE:
        return Spannable.SPAN_INCLUSIVE_INCLUSIVE;

      case Spannable.SPAN_EXCLUSIVE_EXCLUSIVE:
        return Spannable.SPAN_INCLUSIVE_EXCLUSIVE;

      default:
        throw new IllegalStateException("Not supported span mode: " + mode);
      }
    }
  }

  private int closeFromTheLeft(final int mode) {
    if (isOpenFromTheLeft(mode)) {
      switch (mode) {

      case Spannable.SPAN_INCLUSIVE_EXCLUSIVE:
        return Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

      case Spannable.SPAN_INCLUSIVE_INCLUSIVE:
        return Spannable.SPAN_EXCLUSIVE_INCLUSIVE;

      default:
        throw new IllegalStateException("Not supported span mode: " + mode);
      }
    } else {
      return mode;
    }

  }

  private int openFromTheRight(final int mode) {
    if (isOpenFromTheRight(mode)) {
      return mode;
    } else {
      switch (mode) {

      case Spannable.SPAN_INCLUSIVE_EXCLUSIVE:
        return Spannable.SPAN_INCLUSIVE_INCLUSIVE;

      case Spannable.SPAN_EXCLUSIVE_EXCLUSIVE:
        return Spannable.SPAN_EXCLUSIVE_INCLUSIVE;

      default:
        throw new IllegalStateException("Not supported span mode: " + mode);
      }
    }
  }

  private int closeFromTheRight(final int mode) {
    if (isOpenFromTheRight(mode)) {
      switch (mode) {

      case Spannable.SPAN_EXCLUSIVE_INCLUSIVE:
        return Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

      case Spannable.SPAN_INCLUSIVE_INCLUSIVE:
        return Spannable.SPAN_INCLUSIVE_EXCLUSIVE;

      default:
        throw new IllegalStateException("Not supported span mode: " + mode);
      }
    } else {
      return mode;
    }
  }

  private int[] closeInside(final int mode) {
    switch (mode) {

    case Spannable.SPAN_EXCLUSIVE_EXCLUSIVE:
      // case: (...|...) => (...)|(...)
      return new int[] {Spannable.SPAN_EXCLUSIVE_EXCLUSIVE, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE};

    case Spannable.SPAN_INCLUSIVE_INCLUSIVE:
      // case: [...|...] => [...)|(...]
      return new int[] {Spannable.SPAN_INCLUSIVE_EXCLUSIVE, Spannable.SPAN_EXCLUSIVE_INCLUSIVE};

    case Spannable.SPAN_INCLUSIVE_EXCLUSIVE:
      // case: [...|...) => [...)|(...)
      return new int[] {Spannable.SPAN_INCLUSIVE_EXCLUSIVE, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE};

    case Spannable.SPAN_EXCLUSIVE_INCLUSIVE:
      // case: (...|...] => (...)|(...]
      return new int[] {Spannable.SPAN_EXCLUSIVE_EXCLUSIVE, Spannable.SPAN_EXCLUSIVE_INCLUSIVE};

    default:
      throw new IllegalStateException("Not supported span mode: " + mode);
    }
  }

  private void handleEmptySelectionOnSingleSpan(final Spannable text, final Object effect, final int cursorPos) {
    final int spanStart = text.getSpanStart(effect);
    final int spanEnd = text.getSpanEnd(effect);
    final int spanMode = getEffectRelatedFlag(text.getSpanFlags(effect));

    text.removeSpan(effect);

    // do not recreate empty text span
    if (spanStart != spanEnd) {
      if (spanStart == cursorPos) {

        if (isOpenFromTheLeft(spanMode)) {

          // case: |[...] => |(...]
          text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, closeFromTheLeft(spanMode));

        } else {

          // case: |(...] => |[...]
          text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, openFromTheLeft(spanMode));

        }


      } else if ((spanStart < cursorPos) && (cursorPos < spanEnd)) {

        // case: split span into two parts closed in the middle
        final int[] newModes = closeInside(spanMode);
        text.setSpan(mParentEffect.newEffect(), spanStart, cursorPos, newModes[0]);
        text.setSpan(mParentEffect.newEffect(), cursorPos, spanEnd, newModes[1]);

      } else if (cursorPos == spanEnd) {

        if (isOpenFromTheRight(spanMode)) {

          // case: [...]| => [...)|
          text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, closeFromTheRight(spanMode));

        } else {

          // case: [...)| => [...]|
          text.setSpan(mParentEffect.newEffect(), spanStart, spanEnd, openFromTheRight(spanMode));

        }

      }
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

        // do not recreate empty text spans
        if (start != end) {
          text.setSpan(mParentEffect.newEffect(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
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

  private boolean isOpenFromTheLeft(final int mode) {
    return mode == Spannable.SPAN_INCLUSIVE_INCLUSIVE || mode == Spannable.SPAN_INCLUSIVE_EXCLUSIVE;
  }

  public static boolean isOpenFromTheRight(final int mode) {
    return mode == Spannable.SPAN_INCLUSIVE_INCLUSIVE || mode == Spannable.SPAN_EXCLUSIVE_INCLUSIVE;
  }

  private boolean presentOnCursor(final Spannable text, final int cursorPos, final Object effect) {
    final int effectStart = text.getSpanStart(effect);
    final int effectEnd = text.getSpanEnd(effect);
    final int effectMode = getEffectRelatedFlag(text.getSpanFlags(effect));

    return (effectStart < cursorPos && cursorPos < effectEnd)
        || (isOpenFromTheLeft(effectMode) && effectStart == cursorPos)
        || (isOpenFromTheRight(effectMode) && effectEnd == cursorPos);
  }

  public final boolean presentInsideSelection(final Spannable text, final Selection selection) {
    final List effects = mParentEffect.getAllEffectsFrom(text, selection);

    if (effects.isEmpty()) {
      return false;
    }

    if (selection.isEmpty()) {
      return presentOnCursor(text, selection.end, effects.get(0));
    }

    final int affectedStart = text.getSpanStart(effects.get(0));
    final int affectedEnd = text.getSpanEnd(effects.get(effects.size() - 1));

    final int startMode = getEffectRelatedFlag(text.getSpanFlags(effects.get(0)));
    final int endMode = getEffectRelatedFlag(text.getSpanFlags(effects.get(effects.size() - 1)));

    final Selection intersection = selection.getIntersection(new Selection(affectedStart, affectedEnd));

    if (intersection.isEmpty()) {
      return (isOpenFromTheLeft(startMode) && intersection.end == affectedStart)
          || (isOpenFromTheRight(endMode) && intersection.end == affectedEnd);
    }

    return true;
  }

  public static void applyImageLoadingFailedImageSpan(final Spannable text, final Resources resources, final int start, final int end, final String imageUri) {
    applyLoadedImageSpan(text, start, end, imageUri, resources.getDrawable(R.drawable.image_broken));
  }

  public static void applyImageLoadingFailedImageSpan(final Spannable text, final Resources resources, final String imageUri) {
    applyLoadedImageSpan(text, imageUri, null, resources.getDrawable(R.drawable.image_broken));
  }

  public static void applyLoadedImageSpan(final Spannable text, final int start, final int end, final String imageUri, Drawable drawable) {
    if (drawable == null) {
      throw new IllegalArgumentException("Drawable was not specified!");
    }

    // remove previously set image spans
    final ImageSpan[] imageSpans = text.getSpans(start, end, ImageSpan.class);
    for (final ImageSpan imageSpan : imageSpans) {
      text.removeSpan(imageSpan);
    }

    applySingleImageSpan(text, start, end, imageUri, null, drawable);
  }

  public static void applyLoadedImageSpan(final Spannable text, final String imageUri, final Integer maxWidth, Drawable drawable) {
    if (drawable == null) {
      throw new IllegalArgumentException("Drawable was not specified!");
    }

    // remove and reset previously set image spans
    final List<ImageSpan> anchoredSpans = new ArrayList<>();
    final ImageSpan[] allImageSpans = text.getSpans(0, text.length(), ImageSpan.class);
    for (final ImageSpan imageSpan : allImageSpans) {
      if (imageSpan.getSource().equalsIgnoreCase(imageUri)) {
        anchoredSpans.add(imageSpan);
      }
    }
    for (final ImageSpan imageSpan : anchoredSpans) {
      final int start = text.getSpanStart(imageSpan);
      final int end = text.getSpanEnd(imageSpan);
      text.removeSpan(imageSpan);
      applySingleImageSpan(text, start, end, imageUri, maxWidth, drawable);
    }
  }

  public static void applyDummyImage(final Spannable text, final int start, final int end, final String source, final Resources resources) {
    applySingleImageSpan(text, start, end, source, null, resources.getDrawable(R.drawable.dummy));
  }

  public static void applySingleImageSpan(final Spannable text, final int start, final int end, final String source, final Integer maxWidth, final Drawable drawable) {
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    text.setSpan((maxWidth == null) ? new ImageSpan(drawable, source) : new ResizableImageSpan(drawable, source, maxWidth), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public static void extendSelectionToTheLineWidth(final CharSequence str, final Selection selection) {
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

  public static void removeEmptyLines(final Editable str, final Selection selection) {

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

  public static String buildImageAnchor(final String source) {
    return CODE_IMAGE_OPEN + (TextUtils.isEmpty(source) ? null : source) + CODE_ANCHOR_CLOSE;
  }

  public static boolean hasImageAnchors(final String text) {
    return text.length() >= getShortestImageAnchorLength() && text.contains(CODE_IMAGE_OPEN) && text.contains(CODE_ANCHOR_CLOSE);
  }

  public static int getShortestImageAnchorLength() {
    return (CODE_IMAGE_OPEN + CODE_ANCHOR_CLOSE).length();
  }

  public static List<ImageAnchor> getImageAnchors(final String text) {
    final List<ImageAnchor> imageAnchors = new ArrayList<>();
    int open = text.indexOf(CODE_IMAGE_OPEN);
    while (open >= 0) {
      final int offset = open + CODE_IMAGE_OPEN.length();
      final int close = text.indexOf(CODE_ANCHOR_CLOSE, offset);
      if (close >= offset) {
        imageAnchors.add(new ImageAnchor(
            text.substring(open, close + CODE_ANCHOR_CLOSE.length()),
            open
        ));
      } else {
        break;
      }
      open = text.indexOf(CODE_IMAGE_OPEN, offset);
    }
    return imageAnchors;
  }

  public static final class ImageAnchor {
    private final String mAnchor;
    private final int mStart;
    private final int mEnd;

    public ImageAnchor(final String anchor, final int start) {
      if (!EffectsHandler.hasImageAnchors(anchor)) {
        throw new IllegalArgumentException("No image anchor found in '" + anchor + "'!");
      }

      mAnchor = anchor;
      mStart = start;
      mEnd = start + anchor.length();
    }

    public final String getSource() {
      return mAnchor.substring(CODE_IMAGE_OPEN.length(), mAnchor.length() - CODE_ANCHOR_CLOSE.length());
    }

    public final int getStart() {
      return mStart;
    }

    public final int getEnd() {
      return mEnd;
    }
  }

}
