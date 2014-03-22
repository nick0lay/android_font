package com.example_font_manager.android_samples;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import com.example_font_manager.android_samples.font.SimpleFactory;
import fontam.manager.FontManager;

public class FontActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FontManager.getInstance().initialize(this, R.xml.fonts);
        setContentView(R.layout.main);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        SimpleFactory factory = new SimpleFactory();
        return factory.onCreateView(name, context, attrs);
    }
}
