package com.jneuberger.imagesearchjetpack.view.fragment

import android.Manifest
import android.app.DownloadManager
import android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jneuberger.imagesearchjetpack.R
import com.jneuberger.imagesearchjetpack.data.Image
import com.jneuberger.imagesearchjetpack.exception.NoInternetException
import com.jneuberger.imagesearchjetpack.exception.NoResultsException
import com.jneuberger.imagesearchjetpack.exception.UnexpectedErrorException
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_DESCRIPTION
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_IMAGE
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_TITLE
import com.jneuberger.imagesearchjetpack.util.Constants.RETRY_BUTTON_ENABLED
import com.jneuberger.imagesearchjetpack.util.Constants.WIFI_SETTINGS_ENABLED
import com.jneuberger.imagesearchjetpack.util.Injection
import com.jneuberger.imagesearchjetpack.view.adapter.ImageListAdapter
import com.jneuberger.imagesearchjetpack.viewmodel.ImageSearchViewModel
import kotlinx.android.synthetic.main.fragment_image_grid.*


class ImageGridFragment : Fragment() {
    private lateinit var mImageListAdapter: ImageListAdapter
    private val mImagesToDownloadList = ArrayList<Image>()
    private var mViewModel: ImageSearchViewModel? = null

    private val mDownloadCompletionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (mViewModel?.imagesDownloading!!.keys.contains(id)) {
                mViewModel?.notifyComplete(id)
            }
            if (mViewModel?.imagesDownloading!!.keys.size < 1) {
                Toast.makeText(context, getString(R.string.download_completed), Toast.LENGTH_SHORT).show()
                context.unregisterReceiver(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mImageListAdapter = ImageListAdapter(context!!)
        val factory = Injection().provideSearchViewModelFactory(requireContext().applicationContext)
        mViewModel = ViewModelProviders.of(requireActivity(), factory).get(ImageSearchViewModel::class.java)
        return inflater.inflate(R.layout.fragment_image_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLiveDataObservers()
        fragmentProgressBar.visibility = View.VISIBLE
        fragmentProgressBar.isIndeterminate = true
        imageGridRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            adapter = mImageListAdapter
        }
        downloadButton.setOnClickListener {
            mImagesToDownloadList.map { image -> image.isDownloading = true }.apply { mImageListAdapter.notifyDataSetChanged() }
            mViewModel?.downloadImages(mImagesToDownloadList)
            requireContext().registerReceiver(mDownloadCompletionReceiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
            mViewModel?.isEditModeEnabled?.value = false
        }
        mImageListAdapter.setOnClickListener(View.OnClickListener {
            if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                checkPermission()
            } else {
                val position = imageGridRecyclerView.getChildAdapterPosition(it)
                val image = mViewModel?.imageList?.value!![position]
                if (mViewModel?.isEditModeEnabled?.value!!) {
                    if (mImagesToDownloadList.contains(image) && mImagesToDownloadList.size > 1) {
                        mImagesToDownloadList.remove(image)
                        it.alpha = 1f
                    } else {
                        mImagesToDownloadList.add(image)
                        it.alpha = .5f
                    }
                } else {
                    AlertDialog.Builder(context!!)
                            .setTitle(resources.getString(R.string.download_this_image_dialog_title))
                            .setPositiveButton(resources.getString(R.string.download_dialog_positive_button)) { _, _ ->
                                mViewModel?.downloadImage(image)
                                context!!.registerReceiver(mDownloadCompletionReceiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
                                image.isDownloading = true
                                mImageListAdapter.notifyItemChanged(position)
                            }
                            .setNegativeButton(resources.getString(R.string.download_dialog_negative_button)) { _, _ -> }
                            .show()
                }
            }
        })
        mImageListAdapter.setOnLongClickListener(View.OnLongClickListener {
            if (context!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                checkPermission()
            } else {
                mImagesToDownloadList.add(mViewModel?.imageList?.value!![imageGridRecyclerView.getChildAdapterPosition(it)])
                it.alpha = .5f
                mViewModel?.isEditModeEnabled?.value = true
            }
            true
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel?.cancelSearch()
    }

    private fun checkPermission() {
        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_description))
                .setPositiveButton(getString(R.string.permission_dialog_positive_button)) { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
                .show()
    }

    private fun setErrorFragment(errorImage: Int, errorTitle: Int, errorDescription: Int, retryEnabled: Boolean, wifiEnabled: Boolean) {
        val bundle = Bundle().apply {
            putInt(ERROR_IMAGE, errorImage)
            putInt(ERROR_TITLE, errorTitle)
            putInt(ERROR_DESCRIPTION, errorDescription)
            putBoolean(RETRY_BUTTON_ENABLED, retryEnabled)
            putBoolean(WIFI_SETTINGS_ENABLED, wifiEnabled)
        }
        Navigation.findNavController(view!!).apply {
            popBackStack(R.id.imageGridFragment, true)
            navigate(R.id.errorFragment, bundle)
        }
    }

    private fun setLiveDataObservers() {
        mViewModel?.isEditModeEnabled?.observe(this, Observer {
            if (it) {
                downloadButton.visibility = View.VISIBLE
            } else {
                downloadButton.visibility = View.GONE
                mImagesToDownloadList.clear()
                mImageListAdapter.updateImageList(mViewModel?.imageList?.value!!)
            }
        })
        mViewModel?.imageList?.observe(this, Observer {
            if (it.isNotEmpty()) {
                fragmentProgressBar.visibility = View.GONE
                fragmentProgressBar.isIndeterminate = false
                if (!mViewModel?.isEditModeEnabled?.value!!) {
                    mImageListAdapter.updateImageList(it)
                }
            }
        })
        mViewModel?.requestError?.observe(this, Observer {
            when (it) {
                is NoInternetException -> setErrorFragment(R.drawable.no_internet_image, R.string.no_internet_title,
                        R.string.no_internet_description, true, true)
                is NoResultsException -> setErrorFragment(R.drawable.error_image, R.string.no_results_title,
                        R.string.no_results_description, false, false)
                is UnexpectedErrorException -> setErrorFragment(R.drawable.error_image, R.string.unknown_error_title,
                        R.string.unknown_error_description, true, false)
            }
        })
    }

    companion object {
        val instance: ImageGridFragment by lazy { ImageGridFragment() }
    }
}
