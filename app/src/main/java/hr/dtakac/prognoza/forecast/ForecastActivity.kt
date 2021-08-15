package hr.dtakac.prognoza.forecast

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import hr.dtakac.prognoza.R
import hr.dtakac.prognoza.base.ViewBindingActivity
import hr.dtakac.prognoza.common.*
import hr.dtakac.prognoza.databinding.ActivityForecastBinding
import hr.dtakac.prognoza.forecast.adapter.ForecastPagerAdapter
import hr.dtakac.prognoza.places.PlaceSearchDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val SEARCH_FRAGMENT_TAG = "search"

class ForecastActivity :
    ViewBindingActivity<ActivityForecastBinding>(ActivityForecastBinding::inflate) {
    private val viewModel by viewModel<ForecastActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
        initializeViewPager()
        initializeToolbar()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPlaceName()
        viewModel.cleanUpDatabase()
    }

    private fun observeViewModel() {
        viewModel.placeName.observe(this) {
            binding.toolbar.title = it
        }
    }

    private fun initializeViewPager() {
        binding.viewPager.adapter = ForecastPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getStringArray(R.array.forecast_tab_names)[position]
        }.attach()
    }

    private fun initializeToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_forecast)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    openSearch()
                    true
                }
                else -> false
            }
        }
        supportFragmentManager.setFragmentResultListener(
            PLACE_SEARCH_REQUEST_KEY,
            this,
            { _, bundle ->
                if (bundle.getBoolean(BUNDLE_KEY_PLACE_PICKED)) {
                    viewModel.getPlaceName()
                    notifyFragmentsOfNewPlace()
                    closeSearch()
                }
            }
        )
    }

    private fun openSearch() {
        PlaceSearchDialogFragment().show(supportFragmentManager, SEARCH_FRAGMENT_TAG)
    }

    private fun closeSearch() {
        (supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) as? DialogFragment)?.dismiss()
    }

    private fun notifyFragmentsOfNewPlace() {
        val result = Bundle().apply { putBoolean(BUNDLE_KEY_PLACE_PICKED, true) }
        supportFragmentManager.apply {
            setFragmentResult(TODAY_REQUEST_KEY, result)
            setFragmentResult(TOMORROW_REQUEST_KEY, result)
            setFragmentResult(DAYS_REQUEST_KEY, result)
        }
    }
}