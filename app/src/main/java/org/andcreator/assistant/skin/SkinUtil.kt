package org.andcreator.assistant.skin

import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.CardView

class SkinUtil(private var skin:Skin) {

    fun setSkin(skin: Skin){
        this.skin = skin
    }

    fun withFAB(floatingActionButton: FloatingActionButton): SkinUtil{
        floatingActionButton.backgroundTintList = ColorStateList.valueOf(skin.colorAccent)
        return this
    }

    fun whitCard(cardView: CardView): SkinUtil{
        cardView.setCardBackgroundColor(skin.colorAccent)
        return this
    }

}