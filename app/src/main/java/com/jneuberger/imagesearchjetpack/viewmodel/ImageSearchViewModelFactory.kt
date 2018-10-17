package com.jneuberger.imagesearchjetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jneuberger.imagesearchjetpack.data.repository.ImageDownloadRepository
import com.jneuberger.imagesearchjetpack.data.repository.ImageSearchRepository

class ImageSearchViewModelFactory(private val mSearchRepository: ImageSearchRepository,
                                  private val mDownloadRepository: ImageDownloadRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ImageSearchViewModel(mSearchRepository, mDownloadRepository) as T
}