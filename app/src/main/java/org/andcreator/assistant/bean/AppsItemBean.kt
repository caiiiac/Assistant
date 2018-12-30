package org.andcreator.assistant.bean

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppsItemBean(val name: String,
                        val icon: Drawable,
                        val intent: Intent,
                        val pkgName: String,
                        val activityName: String)