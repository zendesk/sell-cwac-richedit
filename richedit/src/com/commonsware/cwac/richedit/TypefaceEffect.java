/***
  Copyright (c) 2008-2011 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.commonsware.cwac.richedit;

import com.futuresimple.base.richedit.text.EffectsHandler;

import android.text.Spannable;
import android.text.style.TypefaceSpan;

import java.util.Arrays;
import java.util.List;

public class TypefaceEffect extends Effect<String, TypefaceSpan> {

  private final EffectsHandler mEffectsHandler;
  private String mFamily;

  public TypefaceEffect() {
    mEffectsHandler = new EffectsHandler(this);
  }

  @Override
  boolean existsInSelection(RichEditText editor) {
    return(valueInSelection(editor) != null);
  }

  @Override
  String valueInSelection(RichEditText editor) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();

    final List<TypefaceSpan> effects = getAllEffectsFrom(str, selection);

    if (!effects.isEmpty()) {
      return(effects.get(0).getFamily());
    }

    return(null);
  }

  @Override
  void applyToSelection(RichEditText editor, String family) {
    mFamily = family;
    mEffectsHandler.applyToSelection(editor.getText(), new Selection(editor));
  }

  @Override
  public final TypefaceSpan newEffect() {
    return new TypefaceSpan(mFamily);
  }

  @Override
  public final List<TypefaceSpan> getAllEffectsFrom(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, TypefaceSpan.class));
  }

}
