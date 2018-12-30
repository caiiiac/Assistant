package org.andcreator.assistant.dialog

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.color_change_dialog.*
import org.andcreator.assistant.R

class ColorChangeDialog: DialogFragment() {

    private var onColorChangeCallback:OnColorChangeCallback? = null

    companion object {
        private const val TAG = "ColorChangeDialog"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.color_change_dialog,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPicker.onChoose = {
            preview.setBackgroundColor(it)
            onColorChangeCallback?.OnChanged(it)
            preview.text = "#" + Integer.toHexString(it)
        }

        preview.setOnClickListener{
            dismiss()
        }
    }

    fun show(fragmentManager: FragmentManager){
        super.show(fragmentManager,TAG)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context == null){
            return
        }
        if(context is OnColorChangeCallback){
            onColorChangeCallback = context
        }
//        level = LSettings.getNightModeLevel(context)
    }

    interface OnColorChangeCallback{
        fun OnChanged(color:Int)
    }

}