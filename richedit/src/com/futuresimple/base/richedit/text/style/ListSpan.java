package com.futuresimple.base.richedit.text.style;

import java.util.List;

public interface ListSpan<TItem> {

  public static final int SPAN_NUMBER = 100;
  public static final int SPAN_BULLET = 101;

  public Type getListType();
  public ListSpan newSpan();

  public enum Type {
    ORDERED, UNORDERED
  }

  public void addItem(final TItem item);
  public List<TItem> getItems();
}
