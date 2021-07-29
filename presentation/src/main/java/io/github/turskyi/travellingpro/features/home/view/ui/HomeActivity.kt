package io.github.turskyi.travellingpro.features.home.view.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityHomeBinding
import io.github.turskyi.travellingpro.utils.decoration.SectionAverageGapItemDecoration
import io.github.turskyi.travellingpro.utils.extensions.*
import io.github.turskyi.travellingpro.features.allcountries.view.ui.AllCountriesActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_ITEM_COUNT
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.home.view.adapter.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.TravellersActivity
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import org.koin.android.ext.android.inject
import java.util.*

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener {

    private val viewModel: HomeActivityViewModel by inject()
    private val homeAdapter: HomeAdapter by inject()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var allCountriesResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var authorizationResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var internetResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        registerActivitiesForResult()
        checkPermissionAndInitAuthentication(this@HomeActivity)
        initView()
        initListeners()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        // makes info icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // makes social network icon visible
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_travellers, menu)
        return true
    }

    /**
     * Calling when "add city dialogue" dismissed.
     */
    override fun onDismiss(dialogInterface: DialogInterface) {
        viewModel.showListOfVisitedCountries()
    }

    override fun onBackPressed() {
        if (viewModel.backPressedTiming + resources.getInteger(R.integer.desired_time_interval) > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
        }
        viewModel.backPressedTiming = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog(R.string.txt_info_home)
                true
            }
            R.id.action_travellers -> {
                openActivity(TravellersActivity::class.java)
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
            resources.getInteger(R.integer.location_and_storage_request_code) -> {
                if ((grantResult.isNotEmpty()
                            && grantResult.first() == PackageManager.PERMISSION_GRANTED)
                ) {
                    // we got here only the first time, when permission is received
                    viewModel.isPermissionGranted = true
                    initAuthentication()
                } else {
                    requestPermission(this)
                }
            }
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        // set drawable icon
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
                    resources.getDimensionPixelOffset(R.dimen.offset_16),
                )
            )
        }
        initGravityForTitle()
    }

    private fun initListeners() {
        homeAdapter.apply {
            onFlagClickListener = { country ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                        viewModel.visitedCountries.value?.size?.let { itemCount ->
                            putInt(EXTRA_ITEM_COUNT, itemCount)
                        }
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode ->
                val country: Country = countryNode.mapVisitedCountryNodeToCountry()

                binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)) {
                    action(R.string.yes) {
                        viewModel.removeFromVisited(country)
                        toastLong(getString(R.string.deleted, country.name))
                    }
                }
            }

            onCountryNameClickListener = { countryNode ->
                // Creating the new Fragment with the Country id passed in.
                val fragment: AddCityDialogFragment =
                    AddCityDialogFragment.newInstance(countryNode.id)
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
        viewModel.visitedCountriesWithCitiesNode.observe(this, { visitedCountries ->
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
                val allCountriesIntent = Intent(this, AllCountriesActivity::class.java)
                allCountriesResultLauncher.launch(allCountriesIntent)
                viewModel.onNavigatedToAllCountries()
            }
        })
    }

    private fun checkPermissionAndInitAuthentication(activity: AppCompatActivity) {
        val locationPermission: Int = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val externalStoragePermission: Int = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (locationPermission != PackageManager.PERMISSION_GRANTED
            && externalStoragePermission != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(activity)
        } else {
            /* we are getting here every time except the first time,
             * since permission is already received */
            viewModel.isPermissionGranted = true
            initAuthentication()
        }
    }

    private fun requestPermission(activity: AppCompatActivity) = ActivityCompat.requestPermissions(
        activity,
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).toTypedArray(),
        resources.getInteger(R.integer.location_and_storage_request_code)
    )

    private fun registerActivitiesForResult() {
        registerAuthorization()
        registerInternetConnectionLauncher()
        registerAllCountriesActivityResultLauncher()
    }

    private fun registerInternetConnectionLauncher() {
        internetResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { internetResult: ActivityResult ->
            // User pressed back button on his phone internet settings page
            if (internetResult.resultCode == RESULT_CANCELED && isOnline()) {
                internetResultLauncher.unregister()
                initPersonalization()
                return@registerForActivityResult
            } else if (internetResult.resultCode == RESULT_CANCELED && !isOnline()) {
                toastLong(R.string.msg_no_internet)
                val internetSettingsIntent = Intent(ACTION_WIRELESS_SETTINGS)
                internetResultLauncher.launch(internetSettingsIntent)
                return@registerForActivityResult
            } else {
                // this case is never happened before
                AuthUI.getInstance().signOut(this)
                return@registerForActivityResult
            }
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
                initPersonalization()
                return@registerForActivityResult
            } else {
                // Sign in failed
                val internetSettingsIntent = Intent(ACTION_WIRELESS_SETTINGS)
                if (result.resultCode == Activity.RESULT_CANCELED && !isOnline()) {
                    toastLong(R.string.msg_no_internet)
                    internetResultLauncher.launch(internetSettingsIntent)
                    return@registerForActivityResult
                } else {
                    val response: IdpResponse? = IdpResponse.fromResultIntent(result.data)
                    when {
                        response == null -> {
                            // User pressed back button
                            toastLong(R.string.msg_sign_in_cancelled)
                            finishAndRemoveTask()
                            return@registerForActivityResult
                        }
                        response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                            toastLong(R.string.msg_bad_internet)
                            internetResultLauncher.launch(internetSettingsIntent)
                            return@registerForActivityResult
                        }
                        response.error?.errorCode == ErrorCodes.INVALID_EMAIL_LINK_ERROR -> {
                            toastLong(R.string.msg_bad_internet)
                            internetResultLauncher.launch(internetSettingsIntent)
                            return@registerForActivityResult
                        }
                        else -> {
                            toastLong(response.error?.localizedMessage)
                            finishAndRemoveTask()
                            return@registerForActivityResult
                        }
                    }
                }
            }
        }
    }

    private fun initPersonalization() {
        toast(R.string.msg_home_signed_in)
        authorizationResultLauncher.unregister()
        // Successfully signed in
        binding.toolbarLayout.title = getString(R.string.home_onboarding_title_loading)
        viewModel.showListOfVisitedCountries()
    }

    private fun registerAllCountriesActivityResultLauncher() {
        allCountriesResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                // New Country is added to list of visited countries
                binding.floatBtnLarge.hide()
                viewModel.showListOfVisitedCountries()
            } else {
                // did not added country to visited list
                when (result.resultCode) {
                    RESULT_CANCELED -> {
                        // User pressed back button
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

    private fun showFloatBtn(visitedCountries: List<VisitedCountry>) {
        if (visitedCountries.isEmpty()) {
            binding.floatBtnLarge.show()
            binding.floatBtnSmall.visibility = View.GONE
        } else {
            binding.floatBtnLarge.hide()
            binding.floatBtnSmall.show()
        }
    }

    private fun updateAdapterWith(visitedCountryNodes: List<VisitedCountryNode>) {
        // makes all list items collapsed
        for (countryNode in visitedCountryNodes) {
            countryNode.isExpanded = false
        }
        homeAdapter.setList(visitedCountryNodes)
    }

    private fun initTitleWithNumberOf(visitedCountryNodes: List<VisitedCountryNode>) {
        if (viewModel.citiesCount == 0) {
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountryNodes.size,
                visitedCountryNodes.size
            )
        } else {
            if (viewModel.citiesCount > visitedCountryNodes.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.citiesCount,
                        viewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountryNodes.size,
                        visitedCountryNodes.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        visitedCountryNodes.size,
                        visitedCountryNodes.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountryNodes.size,
                        visitedCountryNodes.size
                    )
                }"
            }
        }
    }

    private fun showTitleWithCitiesAndCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { countries ->
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
    }

    /** must be open to use it in custom "circle pie chart" widget */
    fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })
    }

    private fun initAuthentication() {
        if (isOnline()) {
            authorizationResultLauncher.launch(getAuthorizationIntent())
        } else {
            toastLong(R.string.msg_no_internet)
            val internetSettingsIntent = Intent(ACTION_WIRELESS_SETTINGS)
            internetResultLauncher.launch(internetSettingsIntent)
        }
    }

    private fun getAuthorizationIntent(): Intent {
        // Choosing authentication providers
        val providers: ArrayList<AuthUI.IdpConfig> = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            /* SmartLock = true -> allows the phone to automatically save the
                    user`s  credentials and try to log them in. */
            .setIsSmartLockEnabled(true)
            .setAvailableProviders(providers)
            // Setting logo drawable for authentication page
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