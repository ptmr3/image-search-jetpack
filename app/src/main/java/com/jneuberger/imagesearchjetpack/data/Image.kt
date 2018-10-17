package com.jneuberger.imagesearchjetpack.data

import android.graphics.Bitmap

data class Image(val smallImage: Bitmap,
                 val description: String?,
                 val user: String,
                 val downloadLink: String,
                 var isDownloading: Boolean? = false,
                 var isDownloaded: Boolean? = false)