package com.prox.voicechanger.ui.list_voice.dialog

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
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.DialogRenameBinding
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.utils.FileUtils

class RenameDialog(private var fileVoice: FileVoice) : DialogFragment() {
    private lateinit var binding: DialogRenameBinding

    private var onSaveListener: ((String) -> Unit)? = null

    fun setOnSaveListener(listener: ((String) -> Unit)) {
        onSaveListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogRenameBinding.inflate(layoutInflater)

        binding.edtName.setText(FileUtils.getName(fileVoice.path))
        binding.icEffect.setImageResource(fileVoice.src)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val newPath = FileUtils.renameFile(
                requireContext(),
                fileVoice.path,
                binding.edtName.text.toString().trim()
            )
            if (newPath != null) {
                onSaveListener?.let {
                    it(newPath)
                }
                dismiss()
            }
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
            mDialog.setCanceledOnTouchOutside(false)
            if (mDialog.window != null) {
                mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window!!.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
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