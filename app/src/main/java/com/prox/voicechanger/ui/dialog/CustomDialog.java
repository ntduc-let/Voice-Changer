package com.prox.voicechanger.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;

public class CustomDialog extends Dialog {
    public CustomDialog(@NonNull Context context, View layout) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout);

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.windowAnimations = R.style.CustomDialogAnimation;
        getWindow().setAttributes(layoutParams);
    }
}
