package org.andcreator.assistant;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import org.andcreator.assistant.bean.MusicBean;
import org.andcreator.assistant.util.CoverModelLoaderFactory;

import java.nio.ByteBuffer;

/**
 * @author hawvu
 */
@GlideModule
public class CoverGlideModules extends AppGlideModule{

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

         registry.replace(MusicBean.class, ByteBuffer.class, new CoverModelLoaderFactory());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
