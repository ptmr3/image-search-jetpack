package com.jneuberger.imagesearchjetpack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jneuberger.imagesearchjetpack.data.Image
import com.jneuberger.imagesearchjetpack.data.repository.ImageDownloadRepository
import com.jneuberger.imagesearchjetpack.data.repository.ImageSearchRepository

class ImageSearchViewModel(private val mSearchRepository: ImageSearchRepository,
                           private val mDownloadRepository: ImageDownloadRepository) : ViewModel() {
    var isEditModeEnabled = MutableLiveData<Boolean>()
        get() {
            field.value ?: run { field.value = false }
            return field
        }
    var imageList:LiveData<ArrayList<Image>> = mSearchRepository.imageList
    var imagesDownloading = mDownloadRepository.imagesDownloading
    var lastSearchTerm: LiveData<String>? = mSearchRepository.lastSearchTerm
    var requestError: LiveData<Throwable> = mSearchRepository.requestError

    fun cancelSearch() = mSearchRepository.cancelSearch()
    fun downloadImage(image: Image) = mDownloadRepository.downloadSingleImage(image)
    fun downloadImages(images: List<Image>) = mDownloadRepository.downloadMultipleImages(images)
    fun notifyComplete(downloadId: Long) = mDownloadRepository.notifyDownloadComplete(downloadId)
    fun searchByTerm(searchTerm: String) = mSearchRepository.getImagesBySearchTerm(searchTerm)
}