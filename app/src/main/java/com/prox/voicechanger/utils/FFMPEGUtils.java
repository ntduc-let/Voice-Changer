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

    private static String pathFFMPEG;
    private static String videoFFMPEG;

    public static String getPathFFMPEG() {
        return pathFFMPEG;
    }

    public static String getVideoFFMPEG() {
        return videoFFMPEG;
    }
    public static void playEffect(String path, Effect effect) {
        String type = FileUtils.getType(path);
        String root = FileUtils.getRoot(path);
        String oldName = FileUtils.getName(path);
        String newName = oldName+"-"+effect.getTitle();
        String pathFFMPEG = root+newName+"."+type;
        String cmd = "-y -i "+path+" -af "+effect.getChangeVoice()+" "+pathFFMPEG;
        if(exceuteFFMPEG(cmd)){
            FFMPEGUtils.pathFFMPEG = pathFFMPEG;
        }else {
            FFMPEGUtils.pathFFMPEG = null;
        }
    }

    private static boolean exceuteFFMPEG(String cmd) {
        FFmpegSession session = FFmpegKit.execute(cmd);

        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i(TAG, "exceuteFFMPEG: Success");
            return true;
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i(TAG, "exceuteFFMPEG: Cancel");
            return false;
        }else {
            Log.i(TAG, "exceuteFFMPEG: Failed");
            return false;
        }
    }

    public static ArrayList<Effect> getEffects() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new Effect(1, R.drawable.ic_original, Original, ""));
        effects.add(new Effect(2, R.drawable.ic_original, "Helium", "asetrate=8000*2,atempo=1/2"));
        effects.add(new Effect(3, R.drawable.ic_robot, "Robot", "afftfilt=\"real='hypot(re,im)*sin(0)':imag='hypot(re,im)*cos(0)':win_size=512:overlap=0.75\""));
        effects.add(new Effect(4, R.drawable.ic_radio, "Radio", "atempo=1"));
        effects.add(new Effect(5, R.drawable.ic_backward, "Backward", "areverse"));
        effects.add(new Effect(6, R.drawable.ic_cave, "Cave", "aecho=0.8:0.9:1000:0.3"));
        effects.add(new Effect(7, R.drawable.ic_original, "Whisper", "afftfilt=\"real='hypot(re,im)*cos((random(0)*2-1)*2*3.14)':imag='hypot(re,im)*sin((random(1)*2-1)*2*3.14)':win_size=128:overlap=0.8\""));
        effects.add(new Effect(8, R.drawable.ic_original, "Chipmunk", "asetrate=22100,atempo=1/2"));
        effects.add(new Effect(9, R.drawable.ic_original, "Hexafluoride", "asetrate=8000*10/11,atempo=11/10"));
        effects.add(new Effect(10, R.drawable.ic_slowmotion, "SlowMotion", "asetrate=8000/2"));
        effects.add(new Effect(11, R.drawable.ic_loudspeaker, "Loudspeaker", "stereotools=mlev=64"));

        return effects;
    }

    public static void addMusicToImage(String pathMusic, String pathImage) {
        String videoFFMPEG = FileUtils.getImageFilePath();
        String cmd = "-loop 1 -r 1 -y -i " + pathImage + " -i " + pathMusic + " -c:v libx264 -crf 27 -tune stillimage -c:a aac -pix_fmt yuv420p -preset ultrafast -shortest -vf pad=\"width=ceil(iw/2)*2:height=ceil(ih/2)*2\" " + videoFFMPEG;
        if(exceuteFFMPEG(cmd)){
            FFMPEGUtils.videoFFMPEG = videoFFMPEG;
        }else {
            FFMPEGUtils.videoFFMPEG = null;
        }
    }
}
