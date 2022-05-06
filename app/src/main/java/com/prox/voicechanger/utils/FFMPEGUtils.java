package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.prox.voicechanger.R;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.model.Effect;

import java.util.ArrayList;

public class FFMPEGUtils {

    public static void executeFFMPEG(String cmd, FFmpegExecuteCallback executeCallback) {
        FFmpegKit.executeAsync(cmd, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d(TAG, "executeFFMPEG: Success");
                executeCallback.onSuccess();
            }else {
                Log.d(TAG, "executeFFMPEG: Failed");
                executeCallback.onFailed();
            }
        });
    }

    public static String getCMDConvertRecording(String fromPath, String toPath) {
        return "-y -i \""+fromPath+"\" -c:v libx264 -ar 16000 \""+toPath+"\"";
    }

    public static String getCMDAddEffect(String fromPath, String toPath, Effect effect) {
        return "-y -i \""+fromPath+"\" "+effect.getChangeVoice()+" \""+toPath+"\"";
    }

    public static String getCMDAddImage(String fromPathMusic, String fromPathImage, String toPath) {
        return "-loop 1 -y -i \"" + fromPathImage + "\" -i \"" + fromPathMusic + "\" -c:v libx264 -crf 27 -tune stillimage -c:a aac -pix_fmt yuv420p -preset ultrafast -shortest -vf pad=\"width=ceil(iw/2)*2:height=ceil(ih/2)*2\" \"" + toPath + "\"";
    }

    public static ArrayList<Effect> getEffects() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new Effect(1, R.drawable.ic_original, "Original", "-c:a copy"));
        effects.add(new Effect(2, R.drawable.ic_helium, "Helium", "-af asetrate=16000*2,atempo=1/2"));
        effects.add(new Effect(3, R.drawable.ic_robot, "Robot", "-af afftfilt=\"real='hypot(re,im)*sin(0)':imag='hypot(re,im)*cos(0)':win_size=512:overlap=0.75\""));
        effects.add(new Effect(4, R.drawable.ic_radio, "Radio", "-af atempo=1"));
        effects.add(new Effect(5, R.drawable.ic_backward, "Backward", "-af areverse"));
        effects.add(new Effect(6, R.drawable.ic_cave, "Cave", "-af aecho=0.8:0.9:1000:0.3"));
        effects.add(new Effect(7, R.drawable.ic_whisper, "Whisper", "-af afftfilt=\"real='hypot(re,im)*cos((random(0)*2-1)*2*3.14)':imag='hypot(re,im)*sin((random(1)*2-1)*2*3.14)':win_size=128:overlap=0.8\""));
        effects.add(new Effect(8, R.drawable.ic_chipmunk, "Chipmunk", "-af asetrate=2*22100"));
        effects.add(new Effect(9, R.drawable.ic_hexafluoride, "Hexaride", "-af asetrate=16000*10/11,atempo=11/10"));
        effects.add(new Effect(10, R.drawable.ic_slowmotion, "S-Motion", "-af asetrate=16000/2"));
        effects.add(new Effect(11, R.drawable.ic_loudspeaker, "L-Speak", "-af stereotools=mlev=64"));

        return effects;
    }

    public static String getCMDCustomEffect(
            String fromPath,
            String toPath,
            double tempoPitch,
            double tempoRate,
            double panning,
            double hz,
            double bandwidth,
            double gain,
            double inGain,
            double outGain,
            double delay,
            double decay) {
        return "-y -i \""+fromPath+"\" -af asetrate=16000*"+tempoPitch+",atempo=1/"+tempoPitch+",atempo="+tempoRate+",volume="+panning+",equalizer=f="+hz+":t=h:width="+bandwidth+":g="+gain+",aecho="+inGain+":"+outGain+":"+delay+":"+decay+" \""+toPath+"\"";
    }
}
