package com.futuresimple.base.richedit.text.style;

public class OrderedListSpan implements ListSpan {

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
}
