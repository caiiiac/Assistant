package org.andcreator.assistant.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import org.andcreator.assistant.bean.MusicBean


/**
 * 音乐相关的工具方法
 */
object MusicUtil {

    private val mediaMetadataRetriever = MediaMetadataRetriever()

    private val queryKeys = arrayOf(
        BaseColumns._ID,
        MediaStore.Audio.AudioColumns.IS_MUSIC,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.SIZE,
        MediaStore.Audio.AudioColumns.DURATION
    )

    private val artworkUri = Uri
        .parse("content://media/external/audio/albumart")

    fun scanMusic(context: Context, musicList: ArrayList<MusicBean>, musicId: Long): MusicBean? {

        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            queryKeys,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER) ?: return null

        var getBean: MusicBean? = null

        while (cursor.moveToNext()){

            val isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC))
            if(isMusic == 0){
                continue
            }

            val bean = cursor.bale()

            if (bean.duration > 30000)
                musicList.add(bean)

            if (bean.cursorId == musicId){
                getBean = bean
            }
        }

        cursor.close()

        return getBean
    }

    private fun Cursor.bale(): MusicBean{
        val bean = MusicBean()

        bean.cursorId = find(BaseColumns._ID,0L)
        bean.name = find(MediaStore.Audio.AudioColumns.TITLE,"")
        bean.artist = find(MediaStore.Audio.AudioColumns.ARTIST,"")
        bean.album = find(MediaStore.Audio.AudioColumns.ALBUM,"")
        bean.albumId = find(MediaStore.Audio.AudioColumns.ARTIST_ID,0L)
        bean.duration = find(MediaStore.Audio.AudioColumns.DURATION,0L)
        bean.path = find(MediaStore.Audio.AudioColumns.DATA,"")
        bean.fileName = find(MediaStore.Audio.AudioColumns.DISPLAY_NAME,"")
        bean.fileSize = find(MediaStore.Audio.AudioColumns.SIZE,0L)
        bean.coverUri = ContentUris.withAppendedId(artworkUri,bean.albumId)
        return bean
    }

    private inline fun <reified T> Cursor.find(key: String, def: T): T{

        val colIndex = getColumnIndex(key)
        if(colIndex < 0){
            return def
        }

        return when (def) {
            is String -> {
                getString(colIndex) as T
            }
            is Long -> {
                getLong(colIndex) as T
            }
            is Int -> {
                getInt(colIndex) as T
            }
            else -> def
        }
    }

    /**
     * 获取音乐文件的封面
     */
    fun getAlbumArt(mediaUri: String): ByteArray?{
        return try{
            mediaMetadataRetriever.setDataSource(mediaUri)
            mediaMetadataRetriever.embeddedPicture
        }catch (e: Exception){
            null
        }
    }

}