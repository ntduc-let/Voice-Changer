package com.prox.voicechanger.utils;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.prox.voicechanger.R;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.model.Effect;

import java.io.File;
import java.io.IOException;
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
        return "-y -i \""+fromPath+"\" -ar 16000 \""+toPath+"\"";
    }

    public static String getCMDAddEffect(String fromPath, String toPath, Effect effect) {
        return "-y -i \""+fromPath+"\" "+effect.getChangeVoice()+" -ar 16000 \""+toPath+"\"";
    }

    public static String getCMDAddImage(String fromPathMusic, String fromPathImage, String toPath) {
        return "-loop 1 -framerate 1 -y -i \"" + fromPathImage + "\" -i \"" + fromPathMusic + "\" -tune stillimage -preset ultrafast -crf 18 -c:a copy -vf format=yuv420p -r 5 -shortest \"" + toPath + "\"";
    }

    public static String getCMDConvertImage(String fromPath, String toPath) {
        if (FileUtils.getType(fromPath).equals("jpg")){
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(new File(fromPath));
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.toString().contains("Orientation")){
                            if (tag.toString().contains("normal")){
                                return "-y -i \"" + fromPath + "\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"" + toPath + "\"";
                            }else if (tag.toString().contains("90")){
                                return "-y -i \"" + fromPath + "\" -preset ultrafast -vf \"transpose=clock,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"" + toPath + "\"";
                            }else if (tag.toString().contains("180")){
                                return "-y -i \"" + fromPath + "\" -preset ultrafast -vf \"transpose=clock,transpose=clock,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"" + toPath + "\"";
                            }
                        }
                    }
                }
            } catch (ImageProcessingException | IOException e) {
                Log.d(TAG, "FFMPEGUtils getCMDConvertImage "+e.getMessage());
            }
            return "-y -i \"" + fromPath + "\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"" + toPath + "\"";
        }
        return "-y -i \"" + fromPath + "\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"" + toPath + "\"";
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
        effects.add(new Effect(9, R.drawable.ic_hexafluoride, "Hexafluoride", "-af asetrate=16000/1.3,atempo=1.3"));
        effects.add(new Effect(10, R.drawable.ic_slowmotion, "Slow Motion", "-af asetrate=16000/2"));
        effects.add(new Effect(11, R.drawable.ic_loudspeaker, "Loudspeaker", "-af stereotools=mlev=64"));

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
