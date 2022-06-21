package com.prox.voicechanger.ui.dialog

import android.app.Dialog
import android.content.Context
import com.prox.voicechanger.R
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.graphics.Color
import android.view.View
import android.view.Window

open class CustomDialog(context: Context, layout: View?) : Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(layout!!)
        window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.CENTER
        layoutParams.windowAnimations = R.style.CustomDialogAnimation
        window!!.attributes = layoutParams
    }
}