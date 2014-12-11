package com.futuresimple.base.richedit.ui;

import com.futuresimple.base.richedit.text.EffectsHandler;
import com.futuresimple.base.richedit.text.HtmlParsingListener;
import com.futuresimple.base.richedit.text.style.URLSpan;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomSpannableEditText extends FixedSelectionEditText implements ImageLoadingListener, HtmlParsingListener {

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

  private void applyLinks(final List<LinkHolder> linkHolders) {
    for (final LinkHolder linkHolder : linkHolders) {
      getText().setSpan(
          new URLSpan(linkHolder.getLink(), linkHolder.getTitle(), linkHolder.getTarget()),
          linkHolder.getLinkStart(), linkHolder.getLinkEnd(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      );
    }
  }

  @Override
  public void onRestoreInstanceState(final Parcelable state) {
    if (!(state instanceof CustomSpansState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    final CustomSpansState customState = (CustomSpansState) state;
    super.onRestoreInstanceState(customState.getSuperState());

    applyLinks(customState.getLinkHolders());

    mLastState = null;
    mStateRestored = true;
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

  @Override
  public Parcelable onSaveInstanceState() {
    // do not call removeAllLinks() inline!!!
    // All "removing" operations have to be done before calling super.onSaveInstanceState() !
    final List<LinkHolder> linkHolders = removeAllLinks();

    mStateRestored = false;
    mLastState = new CustomSpansState(super.onSaveInstanceState(), linkHolders);
    return mLastState;
  }

  @Override
  public final void onLoadingStarted(final String source, final View view) {
    // nothing to do
  }

  @Override
  public final void onLoadingFailed(final String source, final View view, final FailReason failReason) {
    EffectsHandler.applyImageLoadingFailedImageSpan(getText(), getResources(), source);
  }

  @Override
  public final void onLoadingComplete(final String source, final View view, final Bitmap bitmap) {
    final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
    EffectsHandler.applyLoadedImageSpan(getText(), source, getMeasuredWidth(), drawable);
  }

  @Override
  public final void onLoadingCancelled(final String source, final View view) {
    EffectsHandler.applyImageLoadingFailedImageSpan(getText(), getResources(), source);
  }

  @Override
  public final void onImageFound(final String source) {
    mImagesToLoad.add(source);
  }

  @Override
  public final void onParsingFinished() {
    for (final String imageUri : mImagesToLoad) {
      // 0x0 size means unknown
      // also! do not pass uri parameter below (when creating NonViewAware object)
      final NonViewAware nonViewAware = new NonViewAware(new ImageSize(0, 0), ViewScaleType.CROP);
      ImageLoader.getInstance().displayImage(imageUri, nonViewAware, this);
    }

    mImagesToLoad.clear();
  }

  static final class CustomSpansState extends BaseSavedState {

    private final List<LinkHolder> mLinkHolders = new ArrayList<>();

    public CustomSpansState(final Parcel source) {
      super(source);

      readLinks(source);
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

    public CustomSpansState(final Parcelable superState, final List<LinkHolder> linkHolders) {
      super(superState);

      if (linkHolders != null) {
        mLinkHolders.addAll(linkHolders);
      }
    }

    public final List<LinkHolder> getLinkHolders() {
      return mLinkHolders;
    }

    private void writeLinks(final Parcel destination) {
      destination.writeInt(mLinkHolders.size());
      for (final LinkHolder linkHolder : mLinkHolders) {
        destination.writeString(linkHolder.getLink());
        destination.writeString(linkHolder.getTitle());
        destination.writeString(linkHolder.getTarget());
        destination.writeInt(linkHolder.getLinkStart());
        destination.writeInt(linkHolder.getLinkEnd());
      }
    }

    @Override
    public void writeToParcel(final Parcel destination, final int flags) {
      super.writeToParcel(destination, flags);

      writeLinks(destination);
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

  static final class LinkHolder {
    private final String mLink;
    private final String mTitle;
    private final String mTarget;
    private final int mLinkStart;
    private final int mLinkEnd;

    public LinkHolder(final String link, final String title, final String target, final int linkStart, final int linkEnd) {
      mLink = link;
      mTitle = title;
      mTarget = target;
      mLinkStart = linkStart;
      mLinkEnd = linkEnd;
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

    public final int getLinkStart() {
      return mLinkStart;
    }

    public final int getLinkEnd() {
      return mLinkEnd;
    }
  }
}
