package com.jneuberger.imagesearchjetpack.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.jneuberger.imagesearchjetpack.data.Image
import java.io.File
import java.util.*

class ImageDownloadRepository(private val mContext: Context) {
    private var mDownloadId: Long? = null
    var imagesDownloading = HashMap<Long, Image>()

    fun downloadSingleImage(image: Image) = downloadImage(image)

    fun downloadMultipleImages(imageList: List<Image>) = imageList.map { downloadImage(it) }

    fun notifyDownloadComplete(downloadId: Long) {
        imagesDownloading[downloadId]!!.apply {
            isDownloaded = true
            isDownloading = false
        }
        imagesDownloading.remove(downloadId)
    }

    private fun downloadImage(image: Image) {
        val imageDirectory = File(Environment.getExternalStorageDirectory(), DIRECTORY_IMAGE_SEARCH)
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs()
        }
        val request = DownloadManager.Request(Uri.parse(image.downloadLink)).apply {
            setDestinationInExternalPublicDir(DIRECTORY_IMAGE_SEARCH, "$IMAGE_BY_PREFIX${image.user.replace(" ", "")}$JPG_EXTENSION")
        }
        mDownloadId = (mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        imagesDownloading[mDownloadId!!] = image
    }

    companion object {
        private const val DIRECTORY_IMAGE_SEARCH = "/ImageSearch"
        private const val IMAGE_BY_PREFIX = "imageBy"
        private const val JPG_EXTENSION = ".jpg"
    }
}