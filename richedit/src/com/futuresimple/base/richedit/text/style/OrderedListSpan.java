package com.futuresimple.base.richedit.text.style;

import java.util.ArrayList;
import java.util.List;

public class OrderedListSpan implements ListSpan<NumberSpan> {

  private final List<NumberSpan> mItems = new ArrayList<>();

  static int counter = 0;

  public static void newList() {
    counter = 0;
  }

  public static int getNextListItemIndex() {
    return ++counter;
  }

  @Override
  public Type getListType() {
    return Type.ORDERED;
  }

  @Override
  public final ListSpan newSpan() {
    return new OrderedListSpan();
  }

  @Override
  public final void addItem(final NumberSpan numberSpan) {
    mItems.add(numberSpan);
  }

  @Override
  public final List<NumberSpan> getItems() {
    return mItems;
  }
}
