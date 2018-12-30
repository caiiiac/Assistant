package org.andcreator.assistant.listener

interface NotificationCallback {

    fun callStart()

    fun callPause()

    fun callNext()

    fun callPrevious()
}