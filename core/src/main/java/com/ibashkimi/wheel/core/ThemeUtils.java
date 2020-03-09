package com.ibashkimi.wheel.core;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;


public class ThemeUtils {

    public static float round(float value, int places) {
        if (places < 0)
            return value;
        long factor = (long) Math.pow(10, places);
        return ((float) Math.round(value * factor)) / factor;
    }

    public static double round(double value, int places) {
        if (places < 0)
            return value;
        long factor = (long) Math.pow(10, places);
        return ((double) Math.round(value * factor)) / factor;
    }

    @ColorInt
    public static int getColorFromAttribute(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int[] getColorsFromStyle(Context context, @StyleRes int style, int[] attrs, @ColorInt int defaultColor) {
        int[] colors = new int[attrs.length];
        TypedArray a = context.obtainStyledAttributes(style, attrs);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = a.getColor(i, defaultColor);
        }
        a.recycle();
        return colors;
    }

    public static int[] getColorsFromContextTheme(Context context, int[] attrs, @ColorInt int defaultColor) {
        int[] colors = new int[attrs.length];
        TypedArray a = context.obtainStyledAttributes(attrs);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = a.getColor(i, defaultColor);
        }
        a.recycle();
        return colors;
    }

    public static int getThemeAttr(final Context context, int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    public static int getThemeAttr(final Resources.Theme theme, int attr) {
        final TypedValue value = new TypedValue();
        theme.resolveAttribute(attr, value, true);
        return value.data;
    }

    @Deprecated
    public static float getDimensionPixelSize(float dots, float actualDpi) {
        return dots * actualDpi / 160;
    }

    public static float dpToPx(DisplayMetrics metrics, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static float dpToPx(Context context, float dp) {
        return dpToPx(context.getResources().getDisplayMetrics(), dp);
    }

    public static float dpToPx(Context context, int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public static boolean hasNavBar(Context context) {
        //http://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }
}

