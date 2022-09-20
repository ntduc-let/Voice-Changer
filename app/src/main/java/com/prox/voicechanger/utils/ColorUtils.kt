package com.prox.voicechanger.utils

import android.graphics.LinearGradient
import android.graphics.Shader

object ColorUtils {
    fun textShader(startColor: Int, endColor: Int, height: Float): Shader {
        return LinearGradient(
            0f,
            0f,
            0f, (height * 1.3).toFloat(),
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
    }
}