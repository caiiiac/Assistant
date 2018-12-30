package org.andcreator.assistant.fragment

import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.listener.MusicStatus


/**
 * @date: 2018/09/16 23:34
 * @author: lollipop
 *
 */
interface MusicListFragment {

    fun rejectTouch(isReject: Boolean)

    fun getMusicList(): ArrayList<MusicBean>

    fun onReply(musicStatus: MusicStatus,cursorId: Long?)

}