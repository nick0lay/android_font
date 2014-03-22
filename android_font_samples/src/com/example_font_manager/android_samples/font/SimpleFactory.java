package com.example_font_manager.android_samples.font;

import android.content.Context;
import android.view.View;
import fontam.manager.FontManager;
import fontam.manager.factory.FontFactory;

public class SimpleFactory extends FontFactory{

    @Override
    public void onFontApply(Context context, View view) {
        FontManager.getInstance().applyFont(view, "HelveticaNeueLTStd");
    }
}
