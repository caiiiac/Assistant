package org.andcreator.assistant

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.util.CoverModelLoaderFactory
import java.nio.ByteBuffer

//@GlideModule
class CoverGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(MusicBean::class.java, ByteBuffer::class.java, CoverModelLoaderFactory())
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

}