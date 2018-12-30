package org.andcreator.assistant.util

import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import org.andcreator.assistant.bean.MusicBean
import java.nio.ByteBuffer
import java.util.regex.Pattern

/**
 * @date: 2018/10/03 16:47
 * @author: lollipop
 * 封面的Glide加载器
 */
class CoverLoader : ModelLoader<MusicBean, ByteBuffer> {

    private val pattern = Pattern.compile("^/(.+/).+")

    override fun buildLoadData(model: MusicBean, width: Int, height: Int, options: Options): ModelLoader.LoadData<ByteBuffer> {
        return ModelLoader.LoadData<ByteBuffer>(ObjectKey(model), CoverDataFetcher(model.path))
    }

    override fun handles(model: MusicBean): Boolean {
        val matcher = pattern.matcher(model.path)
        return matcher.matches()
    }

    class CoverDataFetcher(private val model: String): DataFetcher<ByteBuffer> {

        private val mediaMetadataRetriever = MediaMetadataRetriever()

        /**
         * 确定返回值的类型
         */
        override fun getDataClass(): Class<ByteBuffer> {
            return ByteBuffer::class.java
        }

        /**
         * cleanup() 方法很有意思。
         * 如果你正在加载一个 InputStream 或打开任何 I/O 类的资源，
         * 你肯定要在 cleanup() 方法中关闭并清理这些 InputStream 或资源。
         */
        override fun cleanup() {
            mediaMetadataRetriever.release()
        }

        /**
         * getDataSource 也基本不重要，但它有一些影响。
         * Glide 对本地图片和远程图片的默认缓存策略是不同的。
         * Glide 假定获取本地图片是简单廉价的，因此我们默认在它们被下采样和变换之后才缓存它们。
         * 相反，Glide 假定获取远程图片是困难而且昂贵的，因此我们将默认缓存获取到的原始数据。
         * 此处确定数据源来自于本地，因此返回DataSource.LOCAL
         */
        override fun getDataSource(): DataSource {
            return DataSource.LOCAL
        }

        /**
         * 对于可以取消的网络连接库或长时间加载，
         * 实现 cancel() 方法是一个好主意。
         * 这将帮助加速其他队列里的加载，并节约一些 CPU ，内存或其他资源。
         * 此处没有可以取消的api，因此留空
         */
        override fun cancel() {
            // Intentionally empty.
        }

        /**
         * 加载数据的方法，
         * @priority 如果你正在使用网络库或其他队列系统，它可以用于含有优先级的请求。
         * @callback 你需要使用你解码出来的数据来调用它，如果因为任何原因解码失败，你也可以使用错误消息来调用。
         */
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
            try{
                mediaMetadataRetriever.setDataSource(model)
                val data = mediaMetadataRetriever.embeddedPicture
                if(data == null){
//                    callback.onLoadFailed(NullPointerException("$model is null"))
                    callback.onDataReady(null)
                }
                val byteBuffer = ByteBuffer.wrap(data)
                callback.onDataReady(byteBuffer)
            }catch (e: Exception){
                callback.onLoadFailed(e)
            }
        }

    }

}