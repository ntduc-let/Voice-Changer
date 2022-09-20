package com.prox.voicechanger.utils;

import android.graphics.LinearGradient;
import android.graphics.Shader;

public class ColorUtils {
    public static Shader textShader(int startColor, int endColor, float height){
        return new LinearGradient(
                0,
                0,
                0,
                (float) (height*1.3),
                startColor,
                endColor,
                Shader.TileMode.CLAMP);
    }
}
