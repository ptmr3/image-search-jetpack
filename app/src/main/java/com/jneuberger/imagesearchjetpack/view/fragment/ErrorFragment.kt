package com.jneuberger.imagesearchjetpack.view.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.jneuberger.imagesearchjetpack.R
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_DESCRIPTION
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_IMAGE
import com.jneuberger.imagesearchjetpack.util.Constants.ERROR_TITLE
import com.jneuberger.imagesearchjetpack.util.Constants.RETRY_BUTTON_ENABLED
import com.jneuberger.imagesearchjetpack.util.Constants.WIFI_SETTINGS_ENABLED
import com.jneuberger.imagesearchjetpack.util.Injection
import com.jneuberger.imagesearchjetpack.viewmodel.ImageSearchViewModel
import kotlinx.android.synthetic.main.fragment_error.*

class ErrorFragment : Fragment() {
    private var mRetrySearchEnabled: Boolean = false
    private var mWifiSettingsEnabled: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = Injection().provideSearchViewModelFactory(requireContext())
        val viewModel = ViewModelProviders.of(requireActivity(), factory).get(ImageSearchViewModel::class.java)
        arguments?.apply {
            errorImage.setImageDrawable(resources.getDrawable(getInt(ERROR_IMAGE)))
            errorTitle.text = context!!.resources.getString(getInt(ERROR_TITLE))
            errorDescription.text = context!!.resources.getString(getInt(ERROR_DESCRIPTION))
            mWifiSettingsEnabled = getBoolean(WIFI_SETTINGS_ENABLED)
            mRetrySearchEnabled = getBoolean(RETRY_BUTTON_ENABLED)
        }
        retryButton.text = if (mRetrySearchEnabled) {
            resources.getString(R.string.retry_button)
        } else {
            resources.getString(R.string.new_search_button)
        }
        retryButton.setOnClickListener {
            if (mRetrySearchEnabled) {
                Navigation.findNavController(view).navigate(R.id.imageGridFragment)
                viewModel.lastSearchTerm?.value?.let { searchTerm -> viewModel.searchByTerm(searchTerm) }
            } else {
                Navigation.findNavController(view).popBackStack(R.id.searchFragment, true)
            }
        }
        wifiSettings.visibility = if (mWifiSettingsEnabled) { View.VISIBLE } else { View.GONE }
        wifiSettings.setOnClickListener { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
    }
}
