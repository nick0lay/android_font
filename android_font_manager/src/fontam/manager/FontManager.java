package fontam.manager;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.util.Log;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FontManager {
    private final String TAG = getClass().getSimpleName();
    private static FontManager instance;
    private static Context context;
    private static Typeface mDefaultFont = Typeface.DEFAULT;

    // Tags
    private static final String TAG_FAMILY = "family";
    private static final String TAG_NAMESET = "nameset";
    private static final String TAG_NAME = "name";
    private static final String TAG_FILESET = "fileset";
    private static final String TAG_FILE = "file";
    //Attributes
    private static final String ATTR_STYLE = "style";

    //Font styles
    private static final String STYLE_NORMAL = "normal";
    private static final String STYLE_BOLD = "bold";
    private static final String STYLE_ITALIC = "italic";
    private static final String STYLE_BOLD_ITALIC = "bold_italic";

    private boolean isName = false;
    private boolean isFile = false;

    private List<Font> mFonts;

    private class FontStyle {
        int style;
        Typeface font;
    }

    private class Font {
        // different font-family names that this Font will respond to.
        List<String> families;
        // different styles for this font.
        List<FontStyle> styles;
    }

    private FontManager(){

    }

    public static FontManager getInstance(){
        if(instance == null){
            instance = new FontManager();
        }
        return instance;
    }

    // Parse the resId and initialize the parser.
    public void initialize(Context context, int resId) {
        if(mFonts != null){
            Log.d(TAG,"FontManager have already initialized");
            return;
        }
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getXml(resId);
            mFonts = new ArrayList<Font>();

            String tag;
            String fontStryleAttr = null;
            int eventType = parser.getEventType();

            Font font = null;

            do {
                tag = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equals(TAG_FAMILY)) {
                            // one of the font-families.
                            font = new Font();
                        } else if (tag.equals(TAG_NAMESET)) {
                            // a list of font-family names supported.
                            font.families = new ArrayList<String>();
                        } else if (tag.equals(TAG_NAME)) {
                            isName = true;
                        } else if (tag.equals(TAG_FILESET)) {
                            // a list of files specifying the different styles.
                            font.styles = new ArrayList<FontStyle>();
                        } else if (tag.equals(TAG_FILE)) {
                            isFile = true;
                            fontStryleAttr = parser.getAttributeValue(null, ATTR_STYLE);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (tag.equals(TAG_FAMILY)) {
                            // add it to the list.
                            if (font != null) {
                                mFonts.add(font);
                                font = null;
                            }
                        } else if (tag.equals(TAG_NAME)) {
                            isName = false;
                        } else if (tag.equals(TAG_FILE)) {
                            isFile = false;
                            fontStryleAttr = null;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText();
                        if (isName) {
                            // value is a name, add it to list of family-names.
                            if (font.families != null)
                                font.families.add(text);
                        } else if (isFile) {
                            // value is a file, add it to the proper kind.
                            FontStyle fontStyle = new FontStyle();
                            fontStyle.font = Typeface.createFromAsset(context.getAssets(), text);
                            String attr = parser.getAttributeValue(null, ATTR_STYLE);
                            if (fontStryleAttr.equals(STYLE_BOLD))
                                fontStyle.style = Typeface.BOLD;
                            else if (fontStryleAttr.equals(STYLE_ITALIC))
                                fontStyle.style = Typeface.ITALIC;
                            else if (fontStryleAttr.equals(STYLE_BOLD_ITALIC))
                                fontStyle.style = Typeface.BOLD_ITALIC;
                            else
                                fontStyle.style = Typeface.NORMAL;
                            font.styles.add(fontStyle);
                        }
                }

                eventType = parser.next();

            } while (eventType != XmlPullParser.END_DOCUMENT);

        } catch (XmlPullParserException e) {
            throw new InflateException("Error inflating font XML", e);
        } catch (IOException e) {
            throw new InflateException("Error inflating font XML", e);
        } finally {
            if (parser != null)
                parser.close();
        }
    }

    public Typeface get(String family, int style) {
        for (Font font: mFonts) {
            for (String familyName : font.families) {
                if (familyName.equals(family)) {
                    // if no style in specified, return normal style.
                    if (style == -1)
                        style = Typeface.NORMAL;
                    for (FontStyle fontStyle : font.styles) {
                        if (fontStyle.style == style)
                            return fontStyle.font;
                    }
                }
            }
        }
        return mDefaultFont;
    }

    public static void setDefaultFont(Typeface typeface){
        mDefaultFont = typeface;
    }

    /**
     * Set font to view. If no font supplied default font will be applied. Use {@link #setDefaultFont(android.graphics.Typeface)}
     * to set default view, otherwise {@link android.graphics.Typeface#NORMAL} will be applied.
     * @param view - view to apply font to. If view instance of {@link android.view.ViewGroup} font will be applied to each
     *             view in {@link android.view.ViewGroup}
     * @param font - font to be applies to view
     */
    public void applyFont(View view, Typeface font){
        if(font == null){
            font = mDefaultFont;
        }
        if(view instanceof ViewGroup){
            applyFont((ViewGroup) view, font);
        }
        if(!(view instanceof TextView)){
            Log.w(TAG, "Font can't be applied to view " + view.getClass().getSimpleName());
            return;
        }
        ((TextView)view).setTypeface(font);
    }

    /**
     *  Set default font to view. See {@link #applyFont(android.view.View, android.graphics.Typeface)} for more.
     * @param view
     */
    public void applyFont(View view){
        applyFont(view, mDefaultFont);
    }

    /**
     * Set defined font family to view. Font style will be extracted from view if style will be defined, otherwise
     * {@link android.graphics.Typeface#DEFAULT} style will be applied.
     * @param view - view to apply font to. If view instance of {@link android.view.ViewGroup} font will be applied to each
     *             view in {@link android.view.ViewGroup}
     * @param fontFamily - font family name.
     */
    public void applyFont(View view, String fontFamily){
        if(view instanceof ViewGroup){
            applyFont((ViewGroup) view, fontFamily);
        }
        if(!(view instanceof TextView)){
            Log.w(TAG, "Font can't be applied to view " + view.getClass().getSimpleName());
            return;
        }

        Typeface tf = ((TextView)view).getTypeface();
        int style = -1;
        if(tf == null){
            style = Typeface.NORMAL;
        } else{
            style = tf.getStyle();
        }
        ((TextView)view).setTypeface(get(fontFamily, style));
    }

    private void applyFont(ViewGroup views, String fontFamily){
        int childs = views.getChildCount();
        for (int i = 0; i < childs; i++) {
            View child = views.getChildAt(i);
            applyFont(child, fontFamily);
        }
    }

    /**
     * Set font family and style to view.
     * @param view - view to apply font to. If view instance of {@link android.view.ViewGroup} font will be applied to each
     *             view in {@link android.view.ViewGroup}
     * @param fontFamily - font family name.
     * @param style - font style, supported following Android styles:
     *              <li>{@link android.graphics.Typeface#NORMAL}</li>
     *              <li>{@link android.graphics.Typeface#BOLD}</li>
     *              <li>{@link android.graphics.Typeface#BOLD_ITALIC}</li>
     *              <li>{@link android.graphics.Typeface#ITALIC}</li>
     */
    public void applyFont(View view, String fontFamily, int style){
        Typeface font =  get(fontFamily, style);
        applyFont(view, font);
    }

    private void applyFont(ViewGroup views, Typeface font){
        int childs = views.getChildCount();
        for (int i = 0; i < childs; i++) {
            View child = views.getChildAt(i);
            applyFont(child, font);
        }
    }
}
