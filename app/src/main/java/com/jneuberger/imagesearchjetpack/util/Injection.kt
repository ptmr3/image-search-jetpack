package com.jneuberger.imagesearchjetpack.util

import android.content.Context
import com.jneuberger.imagesearchjetpack.data.repository.ImageDownloadRepository
import com.jneuberger.imagesearchjetpack.data.repository.ImageSearchRepository
import com.jneuberger.imagesearchjetpack.viewmodel.ImageSearchViewModelFactory

class Injection {
    private var mSearchViewModelFactoryInstance: ImageSearchViewModelFactory? = null

    fun provideSearchViewModelFactory(context: Context) : ImageSearchViewModelFactory {
        mSearchViewModelFactoryInstance = mSearchViewModelFactoryInstance?.let { it } ?: run {
            ImageSearchViewModelFactory(ImageSearchRepository(context), ImageDownloadRepository(context)) }
        return mSearchViewModelFactoryInstance!!
    }
}