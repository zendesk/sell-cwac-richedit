package com.futuresimple.base.richedit.text.style;

public class OrderedListSpan extends BaseListSpan<NumberSpan> {

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
