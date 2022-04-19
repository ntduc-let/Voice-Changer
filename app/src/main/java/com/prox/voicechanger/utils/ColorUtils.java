package com.prox.voicechanger.utils;

import android.graphics.LinearGradient;
import android.graphics.Shader;

public class ColorUtils {
    public static Shader textShader(int startColor, int endColor){
        return new LinearGradient(
                0,
                0,
                0,
                100,
                startColor,
                endColor,
                Shader.TileMode.CLAMP);
    }
}
