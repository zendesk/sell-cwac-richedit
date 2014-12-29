package com.futuresimple.base.richedit.text.style;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListSpan<TItem> implements ListSpan<TItem> {

  private final List<TItem> mItems = new ArrayList<>();

  @Override
  public final void addItem(final TItem item) {
    mItems.add(item);
  }

  @Override
  public final List<TItem> getItems() {
    return mItems;
  }

  @Override
  public final int size() {
    return mItems.size();
  }
}
