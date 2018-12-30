package org.andcreator.assistant.util

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import org.andcreator.assistant.bean.MusicBean
import java.nio.ByteBuffer

/**
 * @date: 2018/10/05 16:16
 * @author: lollipop
 * 自定义加载模块的工厂
 */
class CoverModelLoaderFactory : ModelLoaderFactory<MusicBean, ByteBuffer> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<MusicBean, ByteBuffer> {
        return CoverLoader()
    }

    override fun teardown() {
        // Do nothing.
    }
}