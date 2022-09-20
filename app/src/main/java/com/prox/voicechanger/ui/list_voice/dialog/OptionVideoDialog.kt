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
import com.prox.voicechanger.databinding.DialogOptionVideoBinding
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.utils.FirebaseUtils

class OptionVideoDialog(val fileVoice: FileVoice) : DialogFragment() {
    private lateinit var binding: DialogOptionVideoBinding

    private var onShareListener: (() -> Unit)? = null

    fun setOnShareListener(listener: (() -> Unit)) {
        onShareListener = listener
    }

    private var onRenameListener: (() -> Unit)? = null

    fun setOnRenameListener(listener: (() -> Unit)) {
        onRenameListener = listener
    }

    private var onDeleteListener: (() -> Unit)? = null

    fun setOnDeleteListener(listener: (() -> Unit)) {
        onDeleteListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogOptionVideoBinding.inflate(layoutInflater)

        binding.btnShare.setOnClickListener {
            FirebaseUtils.sendEvent(requireContext(), "Layout_List_File_Save", "Click More Share")
            onShareListener?.let {
                it()
            }
            dismiss()
        }

        binding.btnRename.setOnClickListener {
            onRenameListener?.let {
                it()
            }
            dismiss()
        }

        binding.btnDeleteItem.setOnClickListener {
            onDeleteListener?.let {
                it()
            }
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