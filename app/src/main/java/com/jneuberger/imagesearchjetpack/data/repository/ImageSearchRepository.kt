package com.jneuberger.imagesearchjetpack.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.jneuberger.imagesearchjetpack.data.network.AsyncResult
import com.jneuberger.imagesearchjetpack.data.Image
import com.jneuberger.imagesearchjetpack.data.network.SearchImagesRequest
import com.jneuberger.imagesearchjetpack.exception.NoInternetException
import com.jneuberger.imagesearchjetpack.exception.NoResultsException
import java.util.*
import kotlin.collections.ArrayList

class ImageSearchRepository(private val mContext: Context) : Observable(), AsyncResult {
    private var mSearchRequest: AsyncTask<String?, ArrayList<Image>?, ArrayList<Image>?>? = null
    var lastSearchTerm: MutableLiveData<String>? = null
    var imageList = MutableLiveData<ArrayList<Image>>()
        get() {
            field.value ?: run { field.value = ArrayList() }
            return field
        }
    var requestError = MutableLiveData<Throwable>()

    fun cancelSearch() {
        mSearchRequest?.apply { cancel(false) }
    }

    fun getImagesBySearchTerm(searchTerm: String) {
        imageList.value?.clear()
        mSearchRequest?.apply { cancel(false) }
        val networkInfo = (mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected) {
            requestError.value = NoInternetException()
        } else {
            mSearchRequest = SearchImagesRequest(this@ImageSearchRepository).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchTerm)
        }
    }

    override fun onError(throwable: Throwable) {
        requestError.value = throwable
    }

    override fun onProcessComplete(result: ArrayList<Image>?) {
        if (result == null || result.isEmpty()) {
            requestError.value = NoResultsException()
        } else {
            imageList.value = result
        }
    }

    override fun onProgressUpdate(result: ArrayList<Image>) {
        if (result.size < 20) {
            imageList.value = result
        } else if (result.size % 10 == 0) {
            imageList.value = result
        }
    }
}