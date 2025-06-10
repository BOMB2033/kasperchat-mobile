
package com.example.kasperchat_test

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class KasperChatGlideModule  : AppGlideModule() {

    // Этот метод оставляем пустым, если не хотим менять стандартные компоненты Glide.
    // Он нужен для регистрации кастомных ModelLoader'ов и т.д.
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
    }

    // Этот метод нужен для применения кастомных настроек.
    // Если вам не нужны кастомные настройки, можно его вообще убрать
    // или оставить пустым.
    /*
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Пример: Установить размер дискового кэша в 100 МБ
        val diskCacheSizeBytes = 1024 * 1024 * 100L // 100 MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))
        
        // Пример: Установить формат изображений по умолчанию
        builder.setDefaultRequestOptions(
            RequestOptions().format(DecodeFormat.PREFER_RGB_565)
        )
    }
    */

    // Этот метод отключает парсинг манифеста для поиска старых GlideModule (для производительности).
    // Рекомендуется его переопределить.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}