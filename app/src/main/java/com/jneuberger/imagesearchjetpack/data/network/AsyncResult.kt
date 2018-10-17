package com.jneuberger.imagesearchjetpack.data.network

import com.jneuberger.imagesearchjetpack.data.Image

interface AsyncResult {
    fun onError(throwable: Throwable)
    fun onProgressUpdate(result: ArrayList<Image>)
    fun onProcessComplete(result: ArrayList<Image>?)
}