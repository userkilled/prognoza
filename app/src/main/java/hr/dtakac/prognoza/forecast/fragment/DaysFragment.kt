package hr.dtakac.prognoza.forecast.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import hr.dtakac.prognoza.base.ViewBindingFragment
import hr.dtakac.prognoza.databinding.FragmentDaysBinding
import hr.dtakac.prognoza.forecast.adapter.DaysRecyclerViewAdapter
import hr.dtakac.prognoza.forecast.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DaysFragment : ViewBindingFragment<FragmentDaysBinding>(FragmentDaysBinding::inflate) {
    private val adapter = DaysRecyclerViewAdapter()
    private val viewModel by sharedViewModel<ForecastViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        initializeRecyclerView()
        viewModel.getOtherDaysForecast()
    }

    private fun observeViewModel() {
        viewModel.otherDaysForecast.observe(viewLifecycleOwner) {
            adapter.submitList(it.days)
        }
        viewModel.isOtherDaysForecastLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.apply {
                if (isLoading) show() else hide()
            }
        }
    }

    private fun initializeRecyclerView() {
        binding.rvDays.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvDays.adapter = adapter
    }
}