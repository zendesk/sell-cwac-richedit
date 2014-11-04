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
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class SimpleBooleanEffect<T> extends Effect<Boolean, T> {
  private final EffectsHandler mEffectsHandler;
  private Class<T> clazz;

  SimpleBooleanEffect(Class<T> clazz) {
    this.clazz=clazz;
    mEffectsHandler = new EffectsHandler(this);
  }

  @Override
  boolean existsInSelection(RichEditText editor) {
    return mEffectsHandler.presentInsideSelection(editor.getText(), new Selection(editor));
  }

  @Override
  Boolean valueInSelection(RichEditText editor) {
    return(existsInSelection(editor));
  }

  @Override
  void applyToSelection(RichEditText editor, Boolean add) {
    mEffectsHandler.applyToSelection(editor.getText(), new Selection(editor));
  }

  @Override
  public final T newEffect() {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      Log.e("RichEditText",
          "Exception instantiating " + clazz.toString(), e);
      return null;
    }
  }

  @Override
  public final List<T> getAllEffectsFrom(final Spannable text, final Selection selection) {
    return Arrays.asList(text.getSpans(selection.start, selection.end, clazz));
  }

}
