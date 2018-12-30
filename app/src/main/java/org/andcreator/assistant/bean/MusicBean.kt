package org.andcreator.assistant.bean

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.andcreator.assistant.util.MusicUtil


/**
 * @date: 2018/7/26 23:50
 * @author: lollipop
 *
 * 唱片的bean
 */
class MusicBean {

    //本应用内的的id
    var id = 0L
    //服务数据库中记录的id
    var cursorId = 0L
    //歌曲名
    var name = ""
    //艺术家
    var artist = ""
    //专辑
    var album = ""
    //专辑id
    var albumId = 0L
    //持续时间
    var duration = 0L
    //路径
    var path = ""
    //文件名
    var fileName = ""
    //文件大小
    var fileSize = 0L
    //封面图片地址
    var coverUri: Uri? = null
    //    //封面图片
//    var coverBitmap: Bitmap? = null
    //是否喜欢
    var isLike = false

    fun copy(bean: MusicBean){
        this.id = bean.id
        this.cursorId = bean.cursorId
        this.name = bean.name
        this.artist = bean.artist
        this.album = bean.album
        this.albumId = bean.albumId
        this.duration = bean.duration
        this.path = bean.path
        this.fileName = bean.fileName
        this.fileSize = bean.fileSize
        this.coverUri = bean.coverUri
//        this.coverBitmap = bean.coverBitmap
        this.isLike = bean.isLike
    }

    private fun getByteArray(): ByteArray?{
        return MusicUtil.getAlbumArt(this.path)
    }

    fun getCover(): Bitmap?{
//        if(coverBitmap != null){
//            return coverBitmap
//        }
        val byteArray = getByteArray()?:return null
//        coverBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
//        return coverBitmap
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    override fun toString(): String {
        return path
    }

}