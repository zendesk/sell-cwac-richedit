package com.futuresimple.base.richedit.text.util;

import java.util.ArrayList;
import java.util.List;

public class StringsUtil {

  public static List<Integer> getIndexesOf(final String str, final String keyword) {
    final List<Integer> indexes = new ArrayList<>();
    int index = str.indexOf(keyword);
    while (index >= 0) {
      indexes.add(index);
      index = str.indexOf(keyword, index + keyword.length());
    }

    return indexes;
  }

}
