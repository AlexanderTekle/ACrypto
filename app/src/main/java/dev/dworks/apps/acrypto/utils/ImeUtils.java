/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.acrypto.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility methods for working with the keyboard
 */
public class ImeUtils {

    private ImeUtils() { }

    public static void showIme(@NonNull View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(null != imm) {
            imm.showSoftInput(view, 0);
        }
    }

    public static void hideIme(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if(null != imm) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideIme(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if(null != imm && Utils.isActivityAlive(activity) && null != activity.getCurrentFocus()) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}