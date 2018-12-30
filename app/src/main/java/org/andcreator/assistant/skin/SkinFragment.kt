package org.andcreator.assistant.skin

import android.content.Context
import org.andcreator.assistant.fragment.LazyLoadFragment

abstract class SkinFragment : LazyLoadFragment(), SkinActivity.SkinUpdateCallback {

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context != null && context is SkinActivity){
            context.addSkinUpdateCallback(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        val thisContext = context
        if(thisContext != null && thisContext is SkinActivity){
            thisContext.removeSkinUpdateCallback(this)
        }
    }

}