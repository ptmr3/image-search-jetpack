package com.jneuberger.imagesearchjetpack.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.jneuberger.imagesearchjetpack.R
import com.jneuberger.imagesearchjetpack.util.Injection
import com.jneuberger.imagesearchjetpack.viewmodel.ImageSearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = Injection().provideSearchViewModelFactory(requireContext())
        val viewModel = ViewModelProviders.of(requireActivity(), factory).get(ImageSearchViewModel::class.java)
        searchButton.setOnClickListener {
            stringToSearchInput.onEditorAction(EditorInfo.IME_ACTION_DONE)
            val searchTerm = stringToSearchInput.text.toString()
            if (searchTerm.isEmpty()) {
                stringToSearchInput.apply {
                    error = context.getString(R.string.please_enter_text)
                    text?.clear()
                }
            } else {
                stringToSearchInput.text?.clear()
                Navigation.findNavController(view).navigate(R.id.imageGridFragment)
                viewModel.searchByTerm(searchTerm)
            }
        }
    }

    companion object {
        val instance: SearchFragment by lazy { SearchFragment() }
    }
}