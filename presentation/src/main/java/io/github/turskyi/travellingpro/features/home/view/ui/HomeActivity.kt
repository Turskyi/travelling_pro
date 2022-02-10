package io.github.turskyi.travellingpro.features.home.view.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
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
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.features.allcountries.view.ui.AllCountriesActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_ITEM_COUNT
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.home.view.HomeActivityView
import io.github.turskyi.travellingpro.features.home.view.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.TravellersActivity
import io.github.turskyi.travellingpro.utils.decoration.SectionAverageGapItemDecoration
import io.github.turskyi.travellingpro.utils.extensions.*
import org.koin.android.ext.android.inject

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener, HomeActivityView {

    private val viewModel: HomeActivityViewModel by inject()
    private val listAdapter: HomeAdapter by inject()

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
        initObservers()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        // makes info icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // makes [R.menu.menu_travellers] icon visible
        invalidateOptionsMenu()
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
        // init animated background
        binding.root.setBackgroundResource(R.drawable.gradient_list)
        val layoutBackground: Drawable = binding.root.background
        if (layoutBackground is AnimationDrawable) {
            val animationDrawable: AnimationDrawable = binding.root.background as AnimationDrawable
            animationDrawable.setEnterFadeDuration(2000)
            animationDrawable.setExitFadeDuration(4000)
            animationDrawable.start()
        }
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.includeAppBar.toolbar)
        binding.includeAppBar.toolbarLayout.title = getString(
            R.string.home_onboarding_title_authorization,
        )
        if (supportActionBar != null) {
            // set drawable icon "info"
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.btn_info_ripple)
        }

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = listAdapter
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
        listAdapter.apply {
            onFlagClickListener = { country ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                        if (viewModel.visitedCountries.value != null) {
                            putInt(EXTRA_ITEM_COUNT, viewModel.visitedCountries.value!!.size)
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
                val fragment: AddCityDialogFragment = AddCityDialogFragment.newInstance(
                    countryNode.id,
                )
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
        viewModel.visitedCountriesWithCitiesNode.observe(this) { visitedCountries ->
            initTitle()
            updateAdapterAndTitle(visitedCountries)
        }
        viewModel.visitedCountries.observe(this) { visitedCountries ->
            binding.includeAppBar.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
                binding.includeAppBar.circlePieChart.animatePieChart()
            }
            showFloatBtn(visitedCountries)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            binding.pb.visibility = currentVisibility
        }

        viewModel.errorMessage.observe(this) { event ->
            val errorMessage: String? = event.getMessageIfNotHandled()
            if (errorMessage != null) {
                toastLong(errorMessage)
                AuthUI.getInstance().signOut(this)
            }
        }

        /*  here could be a more efficient way to handle a click to open activity,
         * but it is made on purpose of demonstration databinding */
        viewModel.navigateToAllCountries.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                val allCountriesIntent = Intent(
                    this,
                    AllCountriesActivity::class.java,
                )
                allCountriesResultLauncher.launch(allCountriesIntent)
                viewModel.onNavigatedToAllCountries()
            }
        }
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
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ),
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
    override fun setTitle() = if (viewModel.cityCount > 0) {
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
                            AuthUI.getInstance().signOut(this)
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
                            AuthUI.getInstance().signOut(this)
                            return@registerForActivityResult
                        }
                        else -> {
                            toastLong(
                                response.error?.localizedMessage ?: response.error.toString()
                            )
                            AuthUI.getInstance().signOut(this)
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
        binding.includeAppBar.toolbarLayout.title =
            getString(R.string.home_onboarding_title_loading)
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
        if (getScreenWidth() < 1082) binding.includeAppBar.toolbarLayout.expandedTitleGravity =
            Gravity.BOTTOM
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

    private fun updateAdapterAndTitle(visitedCountryNodes: List<VisitedCountryNode>) {
        viewModel.cityCount = 0
        // makes all list items collapsed
        for (countryNode: VisitedCountryNode in visitedCountryNodes) {
            countryNode.isExpanded = false
            viewModel.cityCount = viewModel.cityCount + countryNode.childNode.size
        }
        listAdapter.setList(visitedCountryNodes)
        initTitle()
    }

    private fun initTitle() {
        if (viewModel.cityCount == 0) {
            showTitleWithOnlyCountries()
        } else {
            showTitleWithCitiesAndCountries()
        }
    }

    private fun showTitleWithCitiesAndCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this) { visitedCountryNodes ->
            if (viewModel.cityCount > visitedCountryNodes.size) {
                binding.includeAppBar.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.cityCount,
                        viewModel.cityCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountryNodes.size,
                        visitedCountryNodes.size
                    )
                }"
            } else {
                binding.includeAppBar.toolbarLayout.title = "${
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

    /** [showTitleWithOnlyCountries] function must be open
     *  to use it in custom "circle pie chart" widget */
    override fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(
            this,
        ) { visitedCountryNodes: List<VisitedCountryNode> ->
            binding.includeAppBar.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountryNodes.size,
                visitedCountryNodes.size
            )
        }
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