package io.github.turskyi.travellingpro.features.home.view.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.common.Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
import io.github.turskyi.travellingpro.common.Constants.TIME_INTERVAL
import io.github.turskyi.travellingpro.databinding.ActivityHomeBinding
import io.github.turskyi.travellingpro.decoration.SectionAverageGapItemDecoration
import io.github.turskyi.travellingpro.extensions.*
import io.github.turskyi.travellingpro.features.allcountries.view.ui.AllCountriesActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_ITEM_COUNT
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.home.view.adapter.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import io.github.turskyi.travellingpro.models.City
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.models.VisitedCountry
import io.github.turskyi.travellingpro.utils.PermissionHandler
import io.github.turskyi.travellingpro.utils.PermissionHandler.isPermissionGranted
import io.github.turskyi.travellingpro.utils.PermissionHandler.requestPermission
import org.koin.android.ext.android.inject
import java.util.*

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener {

    private lateinit var binding: ActivityHomeBinding
    private var authorizationResultLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var allCountriesResultLauncher: ActivityResultLauncher<Intent>

    private var backPressedTiming: Long = 0
    private var mLastClickTime: Long = 0

    private val viewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        registerAuthorization()
        registerAllCountriesActivityResultLauncher()
        PermissionHandler.checkPermissionAndInitAuthentication(this@HomeActivity)
        initView()
        initListeners()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        /** makes info icon visible */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Calling when "add city dialogue" dismissed.
     */
    override fun onDismiss(dialogInterface: DialogInterface?) = viewModel.showListOfCountries()

    override fun onBackPressed() {
        if (backPressedTiming + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
        }
        backPressedTiming = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog(R.string.txt_info_home)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        when (requestCode) {
            ACCESS_LOCATION_AND_EXTERNAL_STORAGE -> if ((grantResult.isNotEmpty()
                        && grantResult[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                /** we got here the first time, when permission is received */
                isPermissionGranted = true
                initAuthentication(authorizationResultLauncher!!)
            } else {
                requestPermission(this)
            }
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        /* set drawable icon */
        supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_info_ripple)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = homeAdapter
            layoutManager = linearLayoutManager
            addItemDecoration(
                SectionAverageGapItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_20),
                    resources.getDimensionPixelOffset(R.dimen.offset_16)
                )
            )
        }
        initGravityForTitle()
    }

    private fun initListeners() {
        homeAdapter.apply {
            onFlagClickListener = { country ->
                /* mis-clicking prevention, using threshold of 1000 ms */
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                        viewModel.visitedCountries.value?.size?.let { itemCount ->
                            putInt(EXTRA_ITEM_COUNT, itemCount)
                        }
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode ->
                val country = countryNode.mapVisitedCountryNodeToCountry()

                binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)) {
                    action(R.string.yes) {
                        viewModel.removeFromVisited(country)
                        toastLong(getString(R.string.deleted, country.name))
                    }
                }
            }

            onCountryNameClickListener = { countryNode ->
                /* Creating the new Fragment with the Country id passed in. */
                val fragment = AddCityDialogFragment.newInstance(countryNode.id)
                fragment.show(supportFragmentManager, null)
            }
            onCityLongClickListener = { city ->
                binding.root.showSnackWithAction(getString(R.string.delete_it, city.name)) {
                    action(R.string.yes) {
                        removeCityOnLongClick(city)
                        toastLong(getString(R.string.deleted, city.name))
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.visitedCountriesWithCities.observe(this, { visitedCountries ->
            initTitleWithNumberOf(visitedCountries)
            updateAdapterWith(visitedCountries)
        })
        viewModel.visitedCountries.observe(this, { visitedCountries ->
            binding.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
                binding.circlePieChart.animatePieChart()
            }
            showFloatBtn(visitedCountries)
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })

        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        })

        /*  here could be a more efficient way to handle a click to open activity,
         * but it is made on purpose of demonstration databinding */
        viewModel.navigateToAllCountries.observe(this, { shouldNavigate ->
            if (shouldNavigate == true) {
                allCountriesResultLauncher.launch(Intent(this, AllCountriesActivity::class.java))
                viewModel.onNavigatedToAllCountries()
            }
        })
    }

    /** must be open to use it in permission handler */
    fun initAuthentication() {
        if(authorizationResultLauncher != null){
            initAuthentication(authorizationResultLauncher!!)
        } else {
            registerAuthorization()
            initAuthentication(authorizationResultLauncher!!)
        }
    }

    /** must be open to use it in custom "circle pie chart" widget */
    fun setTitle() = if (viewModel.citiesCount > 0) {
        showTitleWithCitiesAndCountries()
    } else {
        showTitleWithOnlyCountries()
    }

    private fun registerAuthorization() {
        authorizationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                toast(R.string.msg_home_signed_in)
                authorizationResultLauncher?.unregister()
                /* Successfully signed in */
                binding.toolbarLayout.title = getString(R.string.home_onboarding_title_loading)
                viewModel.showListOfCountries()
                return@registerForActivityResult
            } else {
                /* Sign in failed */
                val response = IdpResponse.fromResultIntent(result.data)
                when {
                    response == null || result.resultCode == Activity.RESULT_CANCELED -> {
                        /* User pressed back button */
                        toastLong(R.string.msg_sign_in_cancelled)
                        finishAndRemoveTask()
                        return@registerForActivityResult
                    }
                    response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                        toastLong(R.string.msg_no_internet)
                        val intent = Intent(ACTION_WIRELESS_SETTINGS)
                        startActivity(intent)
                        finishAndRemoveTask()
                        return@registerForActivityResult
                    }
                    else -> {
                        toastLong(response.error?.message)
                        finishAndRemoveTask()
                        return@registerForActivityResult
                    }
                }
            }
        }
    }

    private fun registerAllCountriesActivityResultLauncher() {
        allCountriesResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                /* New Country is added to list of visited countries */
                binding.floatBtnLarge.hide()
                viewModel.showListOfCountries()
            } else {
                /* did not added country to visited list */
                when (result.resultCode) {
                    RESULT_CANCELED -> {
                        /* User pressed back button */
                        toast(R.string.msg_home_country_did_not_added)
                        return@registerForActivityResult
                    }
                }
            }
        }
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) binding.toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
    }

    private fun removeCityOnLongClick(city: City) = viewModel.removeCity(city)

    private fun showFloatBtn(visitedCountries: List<Country>?) =
        if (visitedCountries.isNullOrEmpty()) {
            binding.floatBtnLarge.show()
            binding.floatBtnSmall.visibility = View.GONE
        } else {
            binding.floatBtnLarge.hide()
            binding.floatBtnSmall.show()
        }

    private fun updateAdapterWith(visitedCountries: List<VisitedCountry>) {
        for (countryNode in visitedCountries) {
            countryNode.isExpanded = false
        }
        homeAdapter.setList(visitedCountries)
    }

    private fun initTitleWithNumberOf(visitedCountries: List<VisitedCountry>) =
        if (viewModel.citiesCount == 0) {
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountries.size,
                visitedCountries.size
            )
        } else {
            if (viewModel.citiesCount > visitedCountries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.citiesCount,
                        viewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                        visitedCountries.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        visitedCountries.size,
                        visitedCountries.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                        visitedCountries.size
                    )
                }"
            }
        }

    private fun showTitleWithCitiesAndCountries() =
        viewModel.visitedCountriesWithCities.observe(this, { countries ->
            if (viewModel.citiesCount > countries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.citiesCount,
                        viewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, countries.size,
                        countries.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        countries.size,
                        countries.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, countries.size,
                        countries.size
                    )
                }"
            }
        })

    /** must be open to use it in custom "circle pie chart" widget */
    fun showTitleWithOnlyCountries() =
        viewModel.visitedCountriesWithCities.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })

    private fun initAuthentication(authorizationResultLauncher: ActivityResultLauncher<Intent>) {
        authorizationResultLauncher.launch(getAuthorizationIntent())
    }

    private fun getAuthorizationIntent(): Intent {
        /** Choosing authentication providers */
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            /* SmartLock = true -> allows the phone to automatically save the
                    user`s  credentials and try to log them in. */
            .setIsSmartLockEnabled(true)
            .setAvailableProviders(providers)
            /** Set logo drawable for authentication page */
            .setLogo(R.drawable.pic_logo)
            .setTheme(R.style.AuthTheme)
            .setTosAndPrivacyPolicyUrls(
//                TODO: replace with Terms of service
                getString(R.string.privacy_web_page),
                getString(R.string.privacy_web_page)
            )
            .build()
    }
}