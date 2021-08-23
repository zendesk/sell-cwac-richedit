package com.futuresimple.base.richedit.ui;

import com.futuresimple.base.richedit.text.EffectsHandler;
import com.futuresimple.base.richedit.text.HtmlParsingListener;
import com.futuresimple.base.richedit.text.style.BulletSpan;
import com.futuresimple.base.richedit.text.style.RichTextUnderlineSpan;
import com.futuresimple.base.richedit.text.style.URLSpan;
import com.futuresimple.base.richedit.text.style.UnorderedListSpan;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomSpannableEditText extends FixedSelectionEditText implements HtmlParsingListener {

  private final Set<String> mImagesToLoad = new HashSet<>();

  private CustomSpansState mLastState = null;
  private boolean mStateRestored;

  public final CustomSpansState getLastState() {
    return mLastState;
  }

  public final boolean isStateRestored() {
    return mStateRestored;
  }

  public CustomSpannableEditText(final Context context) {
    super(context);
  }

  public CustomSpannableEditText(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomSpannableEditText(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  private void applyBulletLists(final List<BulletListHolder> bulletListHolders) {
    for (final BulletListHolder bulletListHolder : bulletListHolders) {
      final UnorderedListSpan bulletList = new UnorderedListSpan();
      getText().setSpan(bulletList, bulletListHolder.getStart(), bulletListHolder.getEnd(), Spanned.SPAN_PARAGRAPH);
      for (final BulletHolder bulletHolder : bulletListHolder.getBulletHolders()) {
        final BulletSpan bullet = new BulletSpan(bulletHolder.getRadius(), bulletHolder.getGap(), bulletHolder.getColor(), bulletHolder.isWantColor());
        getText().setSpan(bullet, bulletHolder.getStart(), bulletHolder.getEnd(), Spanned.SPAN_PARAGRAPH);
        bulletList.addItem(bullet);
      }
    }
  }

  private void applyLinks(final List<LinkHolder> linkHolders) {
    for (final LinkHolder linkHolder : linkHolders) {
      getText().setSpan(
          new URLSpan(linkHolder.getLink(), linkHolder.getTitle(), linkHolder.getTarget()),
          linkHolder.getStart(), linkHolder.getEnd(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      );
    }
  }

  private void applyUnderlines(final List<UnderlineHolder> underlineHolders) {
    for (final UnderlineHolder underlineHolder : underlineHolders) {
      getText().setSpan(new RichTextUnderlineSpan(), underlineHolder.getStart(), underlineHolder.getEnd(), underlineHolder.getMode());
    }
  }

  @Override
  public void onRestoreInstanceState(final Parcelable state) {
    if (!(state instanceof CustomSpansState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    final CustomSpansState customState = (CustomSpansState) state;
    if (getEditableText().length() == 0) {
      super.onRestoreInstanceState(customState.getSuperState());
    }

    applyBulletLists(customState.getBulletListHolders());
    applyLinks(customState.getLinkHolders());
    applyUnderlines(customState.getUnderlineHolders());

    mLastState = null;
    mStateRestored = true;
  }

  private List<BulletListHolder> removeAllBulletLists() {
    final List<BulletListHolder> bulletListHolders = new ArrayList<>();
    final UnorderedListSpan[] bulletLists = getText().getSpans(0, getText().length(), UnorderedListSpan.class);
    for (final UnorderedListSpan bulletList : bulletLists) {
      final BulletListHolder bulletListHolder = new BulletListHolder(getText().getSpanStart(bulletList), getText().getSpanEnd(bulletList));
      for (final BulletSpan bulletSpan : bulletList.getItems()) {
        bulletListHolder.addBulletHolder(new BulletHolder(
            bulletSpan.getBulletRadius(),
            bulletSpan.getGapWidth(),
            bulletSpan.getColor(),
            bulletSpan.isWantColor(),
            getText().getSpanStart(bulletSpan),
            getText().getSpanEnd(bulletSpan)
        ));
        getText().removeSpan(bulletSpan);
      }
      bulletListHolders.add(bulletListHolder);
      getText().removeSpan(bulletList);
    }

    return bulletListHolders;
  }

  private List<LinkHolder> removeAllLinks() {
    final List<LinkHolder> linkHolders = new ArrayList<>();
    final URLSpan[] links = getText().getSpans(0, getText().length(), URLSpan.class);
    for (final URLSpan link : links) {
      linkHolders.add(new LinkHolder(link.getUrl(), link.getTitle(), link.getTarget(), getText().getSpanStart(link), getText().getSpanEnd(link)));
      getText().removeSpan(link);
    }

    return linkHolders;
  }

  private List<UnderlineHolder> removeAllUnderlines() {
    final List<UnderlineHolder> underlineHolders = new ArrayList<>();
    final RichTextUnderlineSpan[] underlines = getText().getSpans(0, getText().length(), RichTextUnderlineSpan.class);
    for (final RichTextUnderlineSpan underline : underlines) {
      underlineHolders.add(new UnderlineHolder(getText().getSpanFlags(underline), getText().getSpanStart(underline), getText().getSpanEnd(underline)));
      getText().removeSpan(underline);
    }

    return underlineHolders;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    // do not call removeAllLinks() inline!!!
    // All "removing" operations have to be done before calling super.onSaveInstanceState() !
    final List<BulletListHolder> bulletListHolders = removeAllBulletLists();
    final List<LinkHolder> linkHolders = removeAllLinks();
    final List<UnderlineHolder> underlineHolders = removeAllUnderlines();

    mStateRestored = false;
    mLastState = new CustomSpansState(super.onSaveInstanceState(), bulletListHolders, linkHolders, underlineHolders);
    return mLastState;
  }

  private void nullLayouts() {
    //This is ugly hack to call private method "nullLayouts" from TextView
    setEllipsize(TruncateAt.END);
    setEllipsize(null);
  }

  @Override
  public final void onImageFound(final String source) {
    mImagesToLoad.add(source);
  }

  @Override
  public final void onParsingFinished() {
    for (final String imageUri : mImagesToLoad) {
      Picasso.get()
          .load(imageUri)
          .into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, LoadedFrom from) {
              post(new Runnable() {
                @Override
                public void run() {
                  final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                  EffectsHandler.applyLoadedImageSpan(getText(), imageUri, getMeasuredWidth(), drawable);
                  nullLayouts();
                }
              });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
              post(new Runnable() {
                @Override
                public void run() {
                  EffectsHandler.applyImageLoadingFailedImageSpan(getText(), getResources(), imageUri);
                  nullLayouts();
                }
              });
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
          });
    }

    mImagesToLoad.clear();
  }

  static final class CustomSpansState extends BaseSavedState {

    private final List<BulletListHolder> mBulletListHolders = new ArrayList<>();
    private final List<LinkHolder> mLinkHolders = new ArrayList<>();
    private final List<UnderlineHolder> mUnderlineHolders = new ArrayList<>();

    public CustomSpansState(final Parcel source) {
      super(source);

      readBulletLists(source);
      readLinks(source);
      readUnderlines(source);
    }

    public CustomSpansState(final Parcelable superState, final List<BulletListHolder> bulletListHolders, final List<LinkHolder> linkHolders, final List<UnderlineHolder> underlineHolders) {
      super(superState);

      if (bulletListHolders != null) {
        mBulletListHolders.addAll(bulletListHolders);
      }

      if (linkHolders != null) {
        mLinkHolders.addAll(linkHolders);
      }

      if (underlineHolders != null) {
        mUnderlineHolders.addAll(underlineHolders);
      }
    }

    private void readBulletLists(final Parcel source) {
      final int bulletListsCount = source.readInt();
      if (bulletListsCount > 0) {
        for (int i = 0; i < bulletListsCount; i++) {

          final BulletListHolder bulletListHolder = new BulletListHolder(source.readInt(), source.readInt());

          final int bulletsCount = source.readInt();

          if (bulletsCount > 0) {
            for (int j = 0; j < bulletsCount; j++) {
              bulletListHolder.addBulletHolder(new BulletHolder(
                  source.readInt(),
                  source.readInt(),
                  source.readInt(),
                  source.readInt() != 0,
                  source.readInt(),
                  source.readInt()
              ));
            }
          }

          mBulletListHolders.add(bulletListHolder);
        }
      }
    }

    private void readLinks(final Parcel source) {
      final int linksCount = source.readInt();
      if (linksCount > 0) {
        for (int i = 0; i < linksCount; i++) {
          mLinkHolders.add(new LinkHolder(
              source.readString(),
              source.readString(),
              source.readString(),
              source.readInt(),
              source.readInt()
          ));
        }
      }
    }

    private void readUnderlines(final Parcel source) {
      final int underlinesCount = source.readInt();
      if (underlinesCount > 0) {
        for (int i = 0; i < underlinesCount; i++) {
          mUnderlineHolders.add(new UnderlineHolder(
              source.readInt(),
              source.readInt(),
              source.readInt()
          ));
        }
      }
    }

    public final List<BulletListHolder> getBulletListHolders() {
      return mBulletListHolders;
    }

    public final List<LinkHolder> getLinkHolders() {
      return mLinkHolders;
    }

    public final List<UnderlineHolder> getUnderlineHolders() {
      return mUnderlineHolders;
    }

    private void writeBulletLists(final Parcel destination) {
      destination.writeInt(mBulletListHolders.size());
      for (final BulletListHolder bulletListHolder : mBulletListHolders) {
        destination.writeInt(bulletListHolder.getStart());
        destination.writeInt(bulletListHolder.getEnd());
        destination.writeInt(bulletListHolder.getBulletHolders().size());
        for (final BulletHolder bulletHolder : bulletListHolder.getBulletHolders()) {
          destination.writeInt(bulletHolder.getRadius());
          destination.writeInt(bulletHolder.getGap());
          destination.writeInt(bulletHolder.getColor());
          destination.writeInt(bulletHolder.isWantColor() ? 1 : 0);
          destination.writeInt(bulletHolder.getStart());
          destination.writeInt(bulletHolder.getEnd());
        }
      }
    }

    private void writeLinks(final Parcel destination) {
      destination.writeInt(mLinkHolders.size());
      for (final LinkHolder linkHolder : mLinkHolders) {
        destination.writeString(linkHolder.getLink());
        destination.writeString(linkHolder.getTitle());
        destination.writeString(linkHolder.getTarget());
        destination.writeInt(linkHolder.getStart());
        destination.writeInt(linkHolder.getEnd());
      }
    }

    private void writeUnderlines(final Parcel destination) {
      destination.writeInt(mUnderlineHolders.size());
      for (final UnderlineHolder underlineHolder : mUnderlineHolders) {
        destination.writeInt(underlineHolder.getMode());
        destination.writeInt(underlineHolder.getStart());
        destination.writeInt(underlineHolder.getEnd());
      }
    }

    @Override
    public void writeToParcel(final Parcel destination, final int flags) {
      super.writeToParcel(destination, flags);

      writeBulletLists(destination);
      writeLinks(destination);
      writeUnderlines(destination);
    }

    public static final Parcelable.Creator<CustomSpansState> CREATOR =
        new Parcelable.Creator<CustomSpansState>() {

          @Override
          public final CustomSpansState createFromParcel(final Parcel source) {
            return new CustomSpansState(source);
          }

          @Override
          public final CustomSpansState[] newArray(final int size) {
            return new CustomSpansState[size];
          }
        };
  }

  static class SpanHolder {
    private final int mStart;
    private final int mEnd;

    public SpanHolder(final int start, final int end) {
      mStart = start;
      mEnd = end;
    }

    public final int getStart() {
      return mStart;
    }

    public final int getEnd() {
      return mEnd;
    }

    public final int getSize() {
      return mEnd - mStart;
    }
  }

  static final class LinkHolder extends SpanHolder {
    private final String mLink;
    private final String mTitle;
    private final String mTarget;

    public LinkHolder(final String link, final String title, final String target, final int start, final int end) {
      super(start, end);
      mLink = link;
      mTitle = title;
      mTarget = target;
    }

    public final String getLink() {
      return mLink;
    }

    public final String getTitle() {
      return mTitle;
    }

    public final String getTarget() {
      return mTarget;
    }
  }

  static final class BulletHolder extends SpanHolder {

    private final int mRadius;
    private final int mColor;
    private final int mGap;
    private final boolean mWantColor;

    public BulletHolder(final int radius, final int gap, final int color, final boolean wantColor, final int start, final int end) {
      super(start, end);
      mRadius = radius;
      mColor = color;
      mGap = gap;
      mWantColor = wantColor;
    }

    public final int getRadius() {
      return mRadius;
    }

    public final int getColor() {
      return mColor;
    }

    public final int getGap() {
      return mGap;
    }

    public final boolean isWantColor() {
      return mWantColor;
    }
  }

  static final class BulletListHolder extends SpanHolder {
    private List<BulletHolder> mBulletHolders = new ArrayList<>();

    public BulletListHolder(int start, int end) {
      super(start, end);
    }

    public final void addBulletHolder(final BulletHolder bulletHolder) {
      if (bulletHolder != null) {
        mBulletHolders.add(bulletHolder);
      }
    }

    public final List<BulletHolder> getBulletHolders() {
      return mBulletHolders;
    }
  }

  static final class UnderlineHolder extends SpanHolder {

    private int mMode;

    public UnderlineHolder(final int mode, final int start, final int end) {
      super(start, end);
      mMode = mode;
    }

    public final int getMode() {
      return mMode;
    }
  }
}
