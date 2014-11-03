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

import android.text.Spannable;
import android.text.style.RelativeSizeSpan;

import java.util.Arrays;
import java.util.List;

public class RelativeSizeEffect extends Effect<Float, RelativeSizeSpan> {

  private Float mProportion;

  @Override
  boolean existsInSelection(RichEditText editor) {
    return(!getAllEffectsFrom(editor.getText(), new Selection(editor)).isEmpty());
  }

  @Override
  Float valueInSelection(RichEditText editor) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();
    float max=0.0f;

    final List<RelativeSizeSpan> effects = getAllEffectsFrom(str, selection);

    if (!effects.isEmpty()) {
      for (RelativeSizeSpan span : effects) {
        max=(max < span.getSizeChange() ? span.getSizeChange() : max);
      }

      return(max);
    }

    return(null);
  }

  @Override
  void applyToSelection(RichEditText editor, Float proportion) {
    mProportion = proportion;
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();

    final List<RelativeSizeSpan> effects = getAllEffectsFrom(str, selection);
    for (RelativeSizeSpan span : effects) {
      str.removeSpan(span);
    }

    if (proportion != null) {
      str.setSpan(newEffect(), selection.start,
                  // TODO: not needed for now, but we have to consider the span type !!!
                  selection.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  @Override
  public final RelativeSizeSpan newEffect() {
    return new RelativeSizeSpan(mProportion);
  }

  @Override
  public final List<RelativeSizeSpan> getAllEffectsFrom(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, RelativeSizeSpan.class));
  }

}
