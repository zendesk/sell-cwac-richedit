package com.futuresimple.base.richedit.text.style;

import java.util.ArrayList;
import java.util.List;

public class UnorderedListSpan implements ListSpan<BulletSpan> {

  private final List<BulletSpan> mItems = new ArrayList<>();

  @Override
  public Type getListType() {
    return Type.UNORDERED;
  }

  @Override
  public final ListSpan newSpan() {
    return new UnorderedListSpan();
  }

  @Override
  public final void addItem(final BulletSpan bulletSpan) {
    mItems.add(bulletSpan);
  }

  @Override
  public final List<BulletSpan> getItems() {
    return mItems;
  }
}
