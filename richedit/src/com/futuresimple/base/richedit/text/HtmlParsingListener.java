package com.futuresimple.base.richedit.text;

public interface HtmlParsingListener {
  public void onImageFound(final String source, final int start, final int end);
  public void onParsingFinished();
}
