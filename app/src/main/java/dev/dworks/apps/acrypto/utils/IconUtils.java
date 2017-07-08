/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;

import dev.dworks.apps.acrypto.R;

public class IconUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable applyTintList(Context context, int drawableId, int tintColorId) {
        final Drawable icon = getDrawable(context, drawableId);
        icon.mutate();
        DrawableCompat.setTintList(DrawableCompat.wrap(icon), ContextCompat.getColorStateList(context, tintColorId));
        return icon;
    }

    public static Drawable applyTint(Context context, int drawableId, int tintColorId) {
        final Drawable icon = getDrawable(context, drawableId);
        icon.mutate();
        DrawableCompat.setTint(DrawableCompat.wrap(icon), tintColorId);
        return icon;
    }

    public static Drawable applyTintAttr(Context context, int drawableId, int tintAttrId) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(tintAttrId, outValue, true);
        return applyTintList(context, drawableId, outValue.resourceId);
    }

    private static Drawable getDrawable(Context context, int drawableId){
        try {
            return ContextCompat.getDrawable(context, drawableId);
        } catch (Resources.NotFoundException e){
            return ContextCompat.getDrawable(context, R.drawable.ic_coins);
        }
    }
}