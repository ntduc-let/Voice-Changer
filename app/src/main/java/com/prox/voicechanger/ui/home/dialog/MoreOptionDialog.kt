package com.prox.voicechanger.ui.home.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ntduc.contextutils.displayWidth
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.DialogMoreOptionBinding
import com.prox.voicechanger.utils.FirebaseUtils

class MoreOptionDialog : DialogFragment() {
    private lateinit var binding: DialogMoreOptionBinding

    private var onImportListener: (() -> Unit)? = null

    fun setOnImportListener(listener: (() -> Unit)) {
        onImportListener = listener
    }

    private var onTextToVoiceListener: (() -> Unit)? = null

    fun setOnTextToVoiceListener(listener: (() -> Unit)) {
        onTextToVoiceListener = listener
    }

    private var onFileListener: (() -> Unit)? = null

    fun setOnFileListener(listener: (() -> Unit)) {
        onFileListener = listener
    }

    private var onVideoListener: (() -> Unit)? = null

    fun setOnVideoListener(listener: (() -> Unit)) {
        onVideoListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogMoreOptionBinding.inflate(layoutInflater)

        binding.btnImport.setOnClickListener {
            FirebaseUtils.sendEvent(
                requireContext(),
                "Layout_Home_More",
                "Click Import pre-recorded sound"
            )

            onImportListener?.let { it() }
            dismiss()
        }

        binding.btnTextToVoice.setOnClickListener {
            FirebaseUtils.sendEvent(
                requireContext(),
                "Layout_Home_More",
                "Click Create voice from text"
            )

            onTextToVoiceListener?.let { it() }
            dismiss()
        }

        binding.btnFile.setOnClickListener {
            FirebaseUtils.sendEvent(requireContext(), "Layout_Home_More", "Click Recorded file")

            onFileListener?.let { it() }
            dismiss()
        }

        binding.btnVideo.setOnClickListener {
            FirebaseUtils.sendEvent(requireContext(), "Layout_Home_More", "Click Video file")

            onVideoListener?.let { it() }
            dismiss()
        }

        builder.setView(binding.root)
        val d: Dialog = builder.create()
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return d
    }

    override fun onStart() {
        super.onStart()
        val mDialog = dialog
        if (mDialog != null) {
            mDialog.setCanceledOnTouchOutside(true)
            if (mDialog.window != null) {
                mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window!!.setLayout(
                    (requireContext().displayWidth*0.75).toInt(),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                val layoutParams = mDialog.window!!.attributes
                layoutParams.gravity = Gravity.TOP or Gravity.END
                layoutParams.y = 32
                layoutParams.x = 16
                layoutParams.windowAnimations = R.style.MoreOptionDialogAnimation
                mDialog.window!!.attributes = layoutParams
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isAdded) {
            return
        }
        try {
            super.show(manager, tag)
        } catch (_: Exception) {
        }
    }

    override fun dismiss() {
        if (isAdded) {
            try {
                super.dismissAllowingStateLoss()
            } catch (_: Exception) {
            }
        }
    }
}