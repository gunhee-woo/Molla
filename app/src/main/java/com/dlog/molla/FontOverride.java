package com.dlog.molla;

import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.Typeface;

/**
 * 앱의 전체 글씨 폰트를 변경하는 기능을 하는 클래스
 */

public class FontOverride {

    /**
     * 파라미터로 받은 폰트를 앱의 기본 폰트로 설정
     * @param context context
     * @param staticTypefaceFieldName
     * @param fontAssetName 폰트
     */

    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    /**
     * 앱의 폰트를 파라미터로 받은 폰트로 바꿈
     * @param staticTypefaceFieldName
     * @param newTypeface 새로운 폰트
     */

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}