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
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding

class TextToVoiceDialog : DialogFragment() {
    private lateinit var binding: DialogTextToVoiceBinding

    private var onDoneListener: ((String) -> Unit)? = null

    fun setOnDoneListener(listener: ((String) -> Unit)) {
        onDoneListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogTextToVoiceBinding.inflate(layoutInflater)

        binding.btnDone.setOnClickListener {
            val text = binding.edtTextToVoice.text.toString().trim()
            if (text.isEmpty()) {
                requireContext().shortToast(R.string.text_empty)
                return@setOnClickListener
            }

            onDoneListener?.let { it(text) }
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
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
                    (requireContext().displayWidth * 0.9).toInt(),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                val layoutParams = mDialog.window!!.attributes
                layoutParams.gravity = Gravity.CENTER
                layoutParams.windowAnimations = R.style.CustomDialogAnimation
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