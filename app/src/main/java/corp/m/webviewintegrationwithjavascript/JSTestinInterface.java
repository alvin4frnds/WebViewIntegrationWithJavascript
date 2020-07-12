package corp.m.webviewintegrationwithjavascript;

import android.webkit.JavascriptInterface;

import java.lang.annotation.Annotation;

/**
 * Created by praveen on 03/07/18.
 */

public class JSTestinInterface implements JavascriptInterface {
    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @android.webkit.JavascriptInterface
    public String getNameFromAndroidET() {
        return "some text";
    }
}
