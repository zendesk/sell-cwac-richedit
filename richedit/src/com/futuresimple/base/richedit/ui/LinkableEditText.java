package com.futuresimple.base.richedit.ui;

import com.futuresimple.base.richedit.text.style.URLSpan;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class LinkableEditText extends EditText {

  public LinkableEditText(final Context context) {
    super(context);
  }

  public LinkableEditText(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public LinkableEditText(final Context context, final AttributeSet attrs, final int defStyle) {
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
    if (!(state instanceof LinksState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    final LinksState linksState = (LinksState) state;
    super.onRestoreInstanceState(linksState.getSuperState());

    applyLinks(linksState.getLinkHolders());
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
    // All URLSpans have to be removed before calling super.onSaveInstanceState() !
    final List<LinkHolder> linkHolders = removeAllLinks();
    return new LinksState(super.onSaveInstanceState(), linkHolders);
  }

  static final class LinksState extends BaseSavedState {

    private final List<LinkHolder> mLinkHolders = new ArrayList<>();

    public LinksState(final Parcel source) {
      super(source);

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

    public LinksState(final Parcelable superState, final List<LinkHolder> linkHolders) {
      super(superState);
      if (linkHolders != null) {
        mLinkHolders.addAll(linkHolders);
      }
    }

    public final List<LinkHolder> getLinkHolders() {
      return mLinkHolders;
    }


    @Override
    public void writeToParcel(final Parcel destination, final int flags) {
      super.writeToParcel(destination, flags);

      destination.writeInt(mLinkHolders.size());
      for (final LinkHolder linkHolder : mLinkHolders) {
        destination.writeString(linkHolder.getLink());
        destination.writeString(linkHolder.getTitle());
        destination.writeString(linkHolder.getTarget());
        destination.writeInt(linkHolder.getLinkStart());
        destination.writeInt(linkHolder.getLinkEnd());
      }
    }

    public static final Parcelable.Creator<LinksState> CREATOR =
        new Parcelable.Creator<LinksState>() {

          @Override
          public final LinksState createFromParcel(final Parcel source) {
            return new LinksState(source);
          }

          @Override
          public final LinksState[] newArray(final int size) {
            return new LinksState[size];
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
