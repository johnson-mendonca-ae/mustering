package com.alert.mustering.helper;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.widget.Toast;

import com.alert.mustering.R;

import io.github.muddz.styleabletoast.StyleableToast;

public class ComponentUtil {
    static final String TAG = ComponentUtil.class.getSimpleName();
    
    public static SpannableString getSpanLink(final Context ctx, final int key){
        SpannableString dataContent = new SpannableString(ctx.getString(key));
        dataContent.setSpan(new UnderlineSpan(), 0, dataContent.length(), 0);
        return dataContent;
    }
    public static void displayTopMessage(Context ctx, String message){
        new StyleableToast.Builder(ctx).text(message).textColor(Color.WHITE)
                .backgroundColor(Color.parseColor("#FA8128")).gravity(Gravity.TOP)
                .show();
    }
    public static void displayLongMessage(Context ctx, String message){
        StyleableToast.makeText(ctx, message, Toast.LENGTH_LONG, R.style.ToastCenter).show();
    }

    public static void displayMessage(Context ctx, String message){
        StyleableToast.makeText(ctx, message, Toast.LENGTH_SHORT, R.style.ToastCenter).show();
    }
}
