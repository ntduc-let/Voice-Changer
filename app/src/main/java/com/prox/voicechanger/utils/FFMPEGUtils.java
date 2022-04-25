package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.prox.voicechanger.R;
import com.prox.voicechanger.model.Effect;

import java.util.ArrayList;

public class FFMPEGUtils {

    public final static String Original = "Original";

    public static boolean executeFFMPEG(String cmd) {
        FFmpegSession session = FFmpegKit.execute(cmd);

        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i(TAG, "executeFFMPEG: Success");
            return true;
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i(TAG, "executeFFMPEG: Cancel");
            return false;
        }else {
            Log.i(TAG, "executeFFMPEG: Failed");
            return false;
        }
    }

    public static String getCMDConvertRecording(String fromPath, String toPath) {
        return "-i \""+fromPath+"\" -c:v libx264 -ar 16000 \""+toPath+"\"";
    }

    public static String getCMDAddEffect(String fromPath, String toPath, Effect effect) {
        return "-y -i \""+fromPath+"\" -af "+effect.getChangeVoice()+" \""+toPath+"\"";
    }

    public static String getCMDAddImage(String fromPathMusic, String fromPathImage, String toPath) {
        return "-loop 1 -y -i \"" + fromPathImage + "\" -i \"" + fromPathMusic + "\" -c:v libx264 -crf 27 -tune stillimage -c:a aac -pix_fmt yuv420p -preset ultrafast -shortest -vf pad=\"width=ceil(iw/2)*2:height=ceil(ih/2)*2\" \"" + toPath + "\"";
    }

    public static ArrayList<Effect> getEffects() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new Effect(1, R.drawable.ic_original, Original, ""));
        effects.add(new Effect(2, R.drawable.ic_original, "Helium", "asetrate=16000*2,atempo=1/2"));
        effects.add(new Effect(3, R.drawable.ic_robot, "Robot", "afftfilt=\"real='hypot(re,im)*sin(0)':imag='hypot(re,im)*cos(0)':win_size=512:overlap=0.75\""));
        effects.add(new Effect(4, R.drawable.ic_radio, "Radio", "atempo=1"));
        effects.add(new Effect(5, R.drawable.ic_backward, "Backward", "areverse"));
        effects.add(new Effect(6, R.drawable.ic_cave, "Cave", "aecho=0.8:0.9:1000:0.3"));
        effects.add(new Effect(7, R.drawable.ic_original, "Whisper", "afftfilt=\"real='hypot(re,im)*cos((random(0)*2-1)*2*3.14)':imag='hypot(re,im)*sin((random(1)*2-1)*2*3.14)':win_size=128:overlap=0.8\""));
        effects.add(new Effect(8, R.drawable.ic_original, "Chipmunk", "asetrate=2*22100"));
        effects.add(new Effect(9, R.drawable.ic_original, "Hexafluoride", "asetrate=16000*10/11,atempo=11/10"));
        effects.add(new Effect(10, R.drawable.ic_slowmotion, "SlowMotion", "asetrate=16000/2"));
        effects.add(new Effect(11, R.drawable.ic_loudspeaker, "Loudspeaker", "stereotools=mlev=64"));

        return effects;
    }
}
