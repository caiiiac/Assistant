package org.andcreator.assistant.listener


/**
 * @date: 2018/09/08 22:48
 * @author: lollipop
 * 自定义异常
 */
class MusicException: RuntimeException {

    constructor(message: String, cause: Throwable): super(message, cause)

    constructor(message: String): super(message)

}