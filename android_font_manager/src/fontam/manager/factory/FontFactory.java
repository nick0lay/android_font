package fontam.manager.factory;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class FontFactory implements LayoutInflater.Factory{
    public final String TAG = getClass().getSimpleName();

    static final Class<?>[] mConstructorSignature = new Class[] {Context.class, AttributeSet.class};
    final Object[] mConstructorArgs = new Object[2];
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit."
    };

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if("ViewStub".equals(name) || "View".equals(name)){
            return null;
        }
        View view = null;
        Constructor<? extends View> constructor = null;
        Class clazz = null;

        if (view == null) {
            if (-1 == name.indexOf('.')) {
                for (String prefix : sClassPrefixList) {
                    clazz = getClazz(prefix, name);
                    if(clazz != null){
                        break;
                    }
                }
            } else {
                clazz = getClazz("", name);
            }
        }

        if (clazz == null) {
            Log.d(TAG, "View can't be created " + name);
            return null;
        }

        try {
            constructor = clazz.getConstructor(mConstructorSignature);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Object[] args = mConstructorArgs;
        args[1] = attrs;

        if(constructor == null){
            return null;
        }

        try {
                view = constructor.newInstance(context, attrs);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if(view != null){
            onFontApply(context, view);
        }
        return view;
    }

    public abstract void onFontApply(Context context, View view);

    private Class getClazz(String prefix, String name){
        Class clazz = null;
        try {
            clazz = Class.forName(prefix + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            return clazz;
        }
    }
}
