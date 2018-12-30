package org.andcreator.assistant.skin

open class Skin {

    /** 主题色 **/
    var colorPrimary = 0
    /** 深色的主题色 **/
    var colorPrimaryDark = 0
    /** 浅色的主题色 **/
    var colorPrimaryLight = 0
    /** 撞色 **/
    var colorAccent = 0

    override fun hashCode(): Int {
        //以一种特别的方式将几个数字组合起来，作为当前数据的hashCode
        return (colorPrimary + colorPrimaryDark +  colorPrimaryLight
                + colorAccent)
    }

    override fun equals(other: Any?): Boolean {
        if(other == null){
            return false
        }
        return this.hashCode() == other.hashCode()
    }

    fun copy(skin: Skin){
        this.colorAccent = skin.colorAccent
        this.colorPrimaryDark = skin.colorPrimaryDark
        this.colorPrimaryLight = skin.colorPrimaryLight
    }

    override fun toString(): String {
        return "colorPrimary:$colorPrimary,colorPrimaryDark:$colorPrimaryDark,colorPrimaryLight:$colorPrimaryLight,colorAccent:$colorAccent,"
    }

}