package com.prox.voicechanger.ui.change_voice.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ntduc.contextutils.displayWidth
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.DialogNameBinding
import com.prox.voicechanger.utils.FileUtils
import java.io.File

class NameDialog : DialogFragment() {
    companion object {
        const val RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE"
        private const val NAME = "NAME"
    }

    private var name: String? = null

    private lateinit var binding: DialogNameBinding

    private var onSaveListener: ((String) -> Unit)? = null

    fun setOnSaveListener(listener: ((String) -> Unit)) {
        onSaveListener = listener
    }

    private var onCancelListener: (() -> Unit)? = null

    fun setOnCancelListener(listener: (() -> Unit)) {
        onCancelListener = listener
    }

    fun newInstance(name: String?): NameDialog {
        val args = Bundle()
        args.putString(NAME, name)
        val fragment = NameDialog()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            name = requireArguments().getString(NAME)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogNameBinding.inflate(layoutInflater)

        binding.edtName.setText(name ?: "")

        binding.btnSave.setOnClickListener {
            if (binding.edtName.text.toString().trim().isEmpty()) {
                requireContext().shortToast(R.string.name_empty)
                return@setOnClickListener
            }

            val path =
                FileUtils.getDownloadFolderPath(VoiceChangerApp.FOLDER_APP) + "/" + binding.edtName.text.toString()
                    .trim() + ".mp3"
            val file = File(path)
            if (file.exists()) {
                requireContext().shortToast(R.string.name_exits)
                return@setOnClickListener
            }

            binding.txtNoti.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_enable30)
            binding.btnSave.setTextColor(requireContext().resources.getColor(R.color.white30))
            binding.btnCancel.isEnabled = false

            onSaveListener?.let {
                it(path)
            }

            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            onCancelListener?.let {
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