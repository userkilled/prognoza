package hr.dtakac.prognoza.places

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import hr.dtakac.prognoza.base.ViewBindingActivity
import hr.dtakac.prognoza.databinding.ActivityPlacesBinding
import hr.dtakac.prognoza.places.adapter.PlacesRecyclerViewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlacesActivity : ViewBindingActivity<ActivityPlacesBinding>(ActivityPlacesBinding::inflate) {
    private val adapter = PlacesRecyclerViewAdapter()
    private val viewModel by viewModel<PlacesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
        initializeRecyclerView()
        initializeSearchField()
        viewModel.showSavedPlaces()
    }

    private fun observeViewModel() {
        viewModel.places.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun initializeRecyclerView() {
        binding.rvResults.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvResults.adapter = adapter
    }

    private fun initializeSearchField() {
        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(textView.text.toString())
                true
            } else {
                false
            }
        }
        binding.etSearch.addTextChangedListener {
            if (it.isNullOrBlank()) {
                viewModel.showSavedPlaces()
            }
        }
    }
}