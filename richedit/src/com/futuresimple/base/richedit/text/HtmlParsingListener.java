package com.futuresimple.base.richedit.text;

public interface HtmlParsingListener {
  public void onImageFound(final String source);
  public void onParsingFinished();
}
