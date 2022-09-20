package com.prox.voicechanger.utils

import android.util.Log
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.prox.voicechanger.VoiceChangerApp
import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import com.prox.voicechanger.R
import com.prox.voicechanger.model.Effect
import java.io.File
import java.io.IOException
import java.util.ArrayList

object FFMPEGUtils {

    fun executeFFMPEG(cmd: String?, executeCallback: FFmpegExecuteCallback) {
        FFmpegKit.executeAsync(cmd) { session: FFmpegSession ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d(VoiceChangerApp.TAG, "executeFFMPEG: Success")
                executeCallback.onSuccess()
            } else {
                Log.d(VoiceChangerApp.TAG, "executeFFMPEG: Failed")
                executeCallback.onFailed()
            }
        }
    }

    fun getCMDConvertRecording(fromPath: String, toPath: String): String {
        return "-y -i \"$fromPath\" -ar 16000 \"$toPath\""
    }

    fun getCMDAddEffect(fromPath: String, toPath: String, effect: Effect): String {
        return "-y -i \"" + fromPath + "\" " + effect.changeVoice + " -ar 16000 \"" + toPath + "\""
    }

    fun getCMDAddImage(fromPathMusic: String, fromPathImage: String, toPath: String): String {
        return "-loop 1 -framerate 1 -y -i \"$fromPathImage\" -i \"$fromPathMusic\" -tune stillimage -preset ultrafast -crf 18 -c:a copy -vf format=yuv420p -r 5 -shortest \"$toPath\""
    }

    fun getCMDConvertImage(fromPath: String, toPath: String): String {
        if (FileUtils.getType(fromPath) == "jpg") {
            try {
                val metadata = ImageMetadataReader.readMetadata(File(fromPath))
                for (directory in metadata.directories) {
                    for (tag in directory.tags) {
                        if (tag.toString().contains("Orientation")) {
                            if (tag.toString().contains("normal")) {
                                return "-y -i \"$fromPath\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
                            } else if (tag.toString().contains("90")) {
                                return "-y -i \"$fromPath\" -preset ultrafast -vf \"transpose=clock,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
                            } else if (tag.toString().contains("180")) {
                                return "-y -i \"$fromPath\" -preset ultrafast -vf \"transpose=clock,transpose=clock,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
                            } else if (tag.toString().contains("270")) {
                                return "-y -i \"$fromPath\" -preset ultrafast -vf \"transpose=clock,transpose=clock,transpose=clock,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
                            }
                        }
                    }
                }
            } catch (e: ImageProcessingException) {
                Log.d(VoiceChangerApp.TAG, "FFMPEGUtils getCMDConvertImage " + e.message)
            } catch (e: IOException) {
                Log.d(VoiceChangerApp.TAG, "FFMPEGUtils getCMDConvertImage " + e.message)
            }
            return "-y -i \"$fromPath\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
        }
        return "-y -i \"$fromPath\" -preset ultrafast -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" \"$toPath\""
    }

    val effects: ArrayList<Effect>
        get() {
            val effects = ArrayList<Effect>()
            effects.add(Effect(1, R.drawable.ic_original, "Original", "-c:a copy"))
            effects.add(
                Effect(
                    2,
                    R.drawable.ic_helium,
                    "Helium",
                    "-af asetrate=16000*2,atempo=1/2"
                )
            )
            effects.add(
                Effect(
                    3,
                    R.drawable.ic_robot,
                    "Robot",
                    "-af afftfilt=\"real='hypot(re,im)*sin(0)':imag='hypot(re,im)*cos(0)':win_size=512:overlap=0.75\""
                )
            )
            effects.add(Effect(4, R.drawable.ic_radio, "Radio", "-af atempo=1"))
            effects.add(Effect(5, R.drawable.ic_backward, "Backward", "-af areverse"))
            effects.add(
                Effect(
                    6,
                    R.drawable.ic_indoor,
                    "Indoor",
                    "-af \"aecho=0.8:0.9:40|50|70:0.4|0.3|0.2\""
                )
            )
            effects.add(
                Effect(
                    7,
                    R.drawable.ic_cave,
                    "Cave",
                    "-af \"aecho=0.8:0.9:500|1000:0.2|0.1\""
                )
            )
            effects.add(
                Effect(
                    8,
                    R.drawable.ic_whisper,
                    "Whisper",
                    "-af afftfilt=\"real='hypot(re,im)*cos((random(0)*2-1)*2*3.14)':imag='hypot(re,im)*sin((random(1)*2-1)*2*3.14)':win_size=128:overlap=0.8\""
                )
            )
            effects.add(Effect(9, R.drawable.ic_chipmunk, "Chipmunk", "-af asetrate=2*22100"))
            effects.add(
                Effect(
                    10,
                    R.drawable.ic_hexafluoride,
                    "Hexafluoride",
                    "-af asetrate=16000/1.3,atempo=1.3"
                )
            )
            effects.add(Effect(11, R.drawable.ic_slowmotion, "Slow Motion", "-af asetrate=16000/2"))
            effects.add(
                Effect(
                    12,
                    R.drawable.ic_loudspeaker,
                    "Loudspeaker",
                    "-af stereotools=mlev=64"
                )
            )
            return effects
        }

    fun getCMDCustomEffect(
        fromPath: String,
        toPath: String,
        tempoPitch: Double,
        tempoRate: Double,
        panning: Double,
        hz: Double,
        bandwidth: Double,
        gain: Double,
        inGain: Double,
        outGain: Double,
        delay: Double,
        decay: Double
    ): String {
        return "-y -i \"$fromPath\" -af asetrate=16000*$tempoPitch,atempo=1/$tempoPitch,atempo=$tempoRate,volume=$panning,equalizer=f=$hz:t=h:width=$bandwidth:g=$gain,aecho=$inGain:$outGain:$delay:$decay \"$toPath\""
    }
}