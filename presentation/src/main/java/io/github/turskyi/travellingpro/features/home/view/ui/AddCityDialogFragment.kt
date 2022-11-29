package io.github.turskyi.travellingpro.features.home.view.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.utils.extensions.isOnline
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import io.github.turskyi.travellingpro.widgets.LinedEditText
import org.koin.android.ext.android.inject
import java.io.IOException
import java.util.*

class AddCityDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(id: Int): AddCityDialogFragment {
            val fragment = AddCityDialogFragment()
            val bundle = Bundle().apply { putInt(ARG_ID, id) }
            fragment.arguments = bundle
            return fragment
        }
    }

    private val viewModel: AddCityDialogViewModel by inject()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var etCity: LinedEditText
    private lateinit var etMonth: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        initLocationServices()

        val builder: AlertDialog.Builder = AlertDialog.Builder(
            requireContext(),
            R.style.RoundShapedDarkAlertDialogStyle
        )

        val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
        val dialogView: View = layoutInflater.inflate(
            R.layout.dialogue_city, viewGroup,
            false
        )

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()

        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)
        val buttonDate: Button = dialogView.findViewById(R.id.btnDate)
        val buttonGps: Button = dialogView.findViewById(R.id.btnGps)
        etCity = dialogView.findViewById(R.id.letCity)
        etMonth = dialogView.findViewById(R.id.etMonth)

        /**
         * There is a unique case when particular android version cannot perform location logic
         * and crashing, so here button just used as a cancel button.
         */
        if (Build.VERSION.RELEASE == getString(R.string.android_5_1)) {
            buttonGps.text = getString(R.string.home_dialog_btn_cancel)
            // Removes CompoundDrawable
            buttonGps.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        etCity.visibility = VISIBLE
        etCity.setText("")
        etMonth.setText("")
        buttonSave.visibility = VISIBLE
        buttonGps.visibility = VISIBLE

        initListeners(buttonSave, alertDialog, buttonGps, buttonDate)

        return alertDialog
    }

    private fun initListeners(
        buttonSave: Button,
        alertDialog: AlertDialog,
        buttonGps: Button,
        buttonDate: Button
    ) {
        buttonSave.setOnClickListener {
            val parentId: Int? = arguments?.getInt(ARG_ID)
            if (etCity.text != null && etCity.text!!.isNotBlank() && parentId != null) {
                if (etMonth.text.isNotBlank()) {
                    viewModel.insert(
                        city = City(
                            name = etCity.text.toString(),
                            parentId = parentId,
                            month = etMonth.text.toString()
                        ),
                        onSuccess = { alertDialog.dismiss() },
                        onError = { exception: Exception /* = java.lang.Exception */ ->
                            toastLong(
                                exception.localizedMessage
                                    ?: exception.stackTraceToString()
                            )
                        },
                    )
                } else {
                    viewModel.insert(
                        city = City(name = etCity.text.toString(), parentId = parentId),
                        onSuccess = { alertDialog.dismiss() },
                        onError = { exception: Exception /* = java.lang.Exception */ ->
                            toastLong(
                                exception.localizedMessage ?: exception.stackTraceToString()
                            )
                        }
                    )
                }
            } else {
                alertDialog.cancel()
                toast(R.string.home_city_did_not_save)
            }
        }

        buttonGps.setOnClickListener {
            /*
             * There is a unique case when particular android version cannot perform location logic
             * and crashing, so here button just used as a cancel button.
             */
            if (Build.VERSION.RELEASE == getString(R.string.android_5_1)) {
                alertDialog.cancel()
            } else if (!isGpsEnabled()) {
                toastLong(R.string.dialogue_turn_on_gps)
                /* even if we have GPS enabled, it will not work if there is no internet,
                   so we have to check that too.*/
            } else if (!isOnline()) {
                toastLong(R.string.dialog_no_internet)
            } else {
                insertCityIntoAnEmptyField(etCity)
            }
        }

        buttonDate.setOnClickListener {
            val monthYear: CharSequence = DateFormat.format(
                getString(R.string.home_dialog_date_format),
                Date()
            )
            etMonth.setText(monthYear)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity: Activity = requireActivity()
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == resources.getInteger(R.integer.location_access_request_code)) {
            if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                insertCityIntoAnEmptyField(etCity)
            } else {
                toast(R.string.msg_gps_permission_denied)
            }
        }
    }

    private fun initLocationServices() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun isGpsEnabled(): Boolean {
        var gpsEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (exception: Exception) {
            toastLong(exception.localizedMessage ?: exception.stackTraceToString())
        }
        return gpsEnabled
    }

    private fun checkPermission(onPermissionGranted: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                resources.getInteger(R.integer.location_access_request_code)
            )
        } else {
            onPermissionGranted()
        }
    }

    private fun insertCityIntoAnEmptyField(editText: LinedEditText) {
        checkPermission(
            onPermissionGranted = {
                @SuppressLint("MissingPermission")
                val findLastLocationTask: Task<Location> = fusedLocationClient.lastLocation
                findLastLocationTask.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        setCityName(location, editText)
                    } else {
                        toastLong(R.string.dialog_hold_on)
                        addChangedLocation(editText)
                    }
                }.addOnFailureListener { exception: java.lang.Exception ->
                    toastLong(exception.localizedMessage ?: exception.stackTraceToString())
                    addChangedLocation(editText)
                }
            },
        )
    }

    @SuppressLint("MissingPermission")
    private fun addChangedLocation(editText: LinedEditText) {
        try {
            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) = setCityName(location, editText)
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            // Request location updates
            checkPermission(
                onPermissionGranted = {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                },
            )
        } catch (exception: SecurityException) {
            toastLong(exception.localizedMessage ?: exception.stackTraceToString())
        }
    }

    private fun setCityName(
        location: Location,
        editText: LinedEditText
    ) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            @Suppress("DEPRECATION")
            val addresses: MutableList<Address>? = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
            )

            val cityName: String? = addresses?.first()?.locality
            editText.setText(cityName)
        } catch (exception: IOException) {
            toastLong(exception.localizedMessage ?: exception.stackTraceToString())
        }
    }
}