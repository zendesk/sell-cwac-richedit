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

import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import android.text.style.AlignmentSpan.Standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineAlignmentEffect extends Effect<Layout.Alignment, AlignmentSpan> {

  private Alignment mAlignment;

  @Override
  boolean existsInSelection(RichEditText editor) {
    return(valueInSelection(editor)!=null);
  }

  @Override
  Layout.Alignment valueInSelection(RichEditText editor) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();

    final List<AlignmentSpan> effects = getAllEffectsFrom(str, selection);

    if (!effects.isEmpty()) {
      return(effects.get(0).getAlignment());
    }
    
    return(null);
  }

  @Override
  void applyToSelection(RichEditText editor, Layout.Alignment alignment) {
    mAlignment = alignment;
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();

    final List<AlignmentSpan> effects = getAllEffectsFrom(str, selection);
    for (AlignmentSpan span : effects) {
      str.removeSpan(span);
    }

    if (alignment!=null) {
      str.setSpan(new AlignmentSpan.Standard(alignment), selection.start, selection.end,
                  // TODO: not needed for now, but we have to consider the span type !!!
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  @Override
  public final AlignmentSpan newEffect() {
    return new Standard(mAlignment);
  }

  @Override
  public final List<AlignmentSpan> getAllEffectsFrom(final Spannable text, final Selection selection) {
    final List<AlignmentSpan> effects = new ArrayList<>();
    effects.addAll(Arrays.asList(text.getSpans(selection.start, selection.end, Standard.class)));
    return effects;
  }

}
