package com.bvm.events.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class InsetsUtil {
    private InsetsUtil() {}

    public static void applyPadding(View view, boolean top, boolean bottom) {
        final int l = view.getPaddingLeft();
        final int t = view.getPaddingTop();
        final int r = view.getPaddingRight();
        final int b = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(l + bars.left,
                    t + (top ? bars.top : 0),
                    r + bars.right,
                    b + (bottom ? bars.bottom : 0));
            return insets;
        });
    }
}

