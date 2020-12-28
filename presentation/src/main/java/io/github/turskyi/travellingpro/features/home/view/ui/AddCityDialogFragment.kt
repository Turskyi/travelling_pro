package io.github.turskyi.travellingpro.features.home.view.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import io.github.turskyi.travellingpro.common.Constants.ACCESS_LOCATION
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.extensions.toast
import io.github.turskyi.travellingpro.extensions.toastLong
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.models.City
import io.github.turskyi.travellingpro.utils.isOnline
import io.github.turskyi.travellingpro.widgets.LinedEditText
import java.util.*

class AddCityDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(id: Int): AddCityDialogFragment {
            val fragment = AddCityDialogFragment()
            val bundle = Bundle().apply {
                putInt(ARG_ID, id)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private val viewModel by inject<AddCityDialogViewModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var etCity: LinedEditText? = null
    private var etMonth: EditText? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        initLocationServices()

        val builder = context?.let { context ->
            AlertDialog.Builder(
                context,
                R.style.RoundShapedDarkAlertDialogStyle
            )
        }

        val viewGroup = (activity as AppCompatActivity)
            .findViewById<ViewGroup>(android.R.id.content)
        val dialogView = LayoutInflater.from(context)
            .inflate(
                R.layout.dialogue_city, viewGroup,
                false
            )

        builder?.setView(dialogView)
        val alertDialog = builder?.create()

        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDate = dialogView.findViewById<Button>(R.id.btnDate)
        val buttonGps = dialogView.findViewById<Button>(R.id.btnGps)
        etCity = dialogView.findViewById(R.id.letCity)
        etMonth = dialogView.findViewById(R.id.etMonth)

        /**
         * There is a unique case when particular android version cannot perform location logic
         * and crashing, so here button just used as a cancel button.
         */
        if (Build.VERSION.RELEASE == "5.1") {
            buttonGps.text = getString(R.string.home_dialog_btn_cancel)
            /* Removes CompoundDrawable */
            buttonGps.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        etCity?.visibility = VISIBLE
        etCity?.setText("")
        etMonth?.setText("")
        buttonSave.visibility = VISIBLE
        buttonGps.visibility = VISIBLE

        initListeners(buttonSave, alertDialog, buttonGps, buttonDate)

        return alertDialog!!
    }

    private fun initListeners(
        buttonSave: Button,
        alertDialog: AlertDialog?,
        buttonGps: Button,
        buttonDate: Button
    ) {
        buttonSave.setOnClickListener {
            if (etCity?.text.toString() != "") {
                if (etMonth?.text.toString() != "") {
                    arguments?.getInt(ARG_ID)?.let { parentId ->
                        City(
                            name = etCity?.text.toString(),
                            parentId = parentId,
                            month = etMonth?.text.toString()
                        ).let { city ->
                            viewModel.insert(
                                city
                            )
                        }
                    }
                } else {
                    arguments?.getInt(ARG_ID)?.let { parentId ->
                        City(etCity?.text.toString(), parentId).let { city ->
                            viewModel.insert(
                                city
                            )
                        }
                    }
                }
            } else {
                alertDialog?.cancel()
                toast(R.string.home_city_did_not_save)
            }
            alertDialog?.dismiss()
        }

        buttonGps.setOnClickListener {
            /**
             * There is a unique case when particular android version cannot perform location logic
             * and crashing, so here button just used as a cancel button.
             */
            etCity?.let {
                if (Build.VERSION.RELEASE == "5.1") {
                    alertDialog?.cancel()
                } else {
                    checkIfGpsEnabled(it)
                }
            }
        }

        buttonDate.setOnClickListener {
            val monthYear = DateFormat.format(
                getString(R.string.home_dialog_date_format),
                Date()
            )
            etMonth?.setText(monthYear)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity: Activity? = activity
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == ACCESS_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                etCity?.let { addCityTo(it) }
            } else {
                toastLong(R.string.msg_gps_permission_denied)
            }
        }
    }

    private fun initLocationServices() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun checkIfGpsEnabled(editText: LinedEditText) {
        val gpsEnabled = checkIfGpsEnabled()
        if (!gpsEnabled) {
            toast(R.string.dialogue_turn_on_gps)
        } else if (!isOnline()) {
            toast(R.string.dialog_no_internet)
        } else {
            addCityTo(editText)
        }
    }

    private fun checkIfGpsEnabled(): Boolean {
        var gpsEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return gpsEnabled
    }

    private fun addCityTo(editText: LinedEditText) {
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
                ACCESS_LOCATION
            )
        } else {
            val findLastLocationTask = fusedLocationClient.lastLocation
            findLastLocationTask.addOnSuccessListener { location ->
                if (location != null) {
                    addLastLocation(location, editText)
                } else {
                    toast(R.string.dialog_hold_on)
                    addChangedLocation(editText)
                }
            }
        }
    }

    private fun addChangedLocation(editText: LinedEditText) {
        try {
            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    val addressesChanged: MutableList<Address>? =
                        location.latitude.let { latitude ->
                            geoCoder.getFromLocation(
                                latitude,
                                location.longitude, 1
                            )
                        }
                    val cityChanged: String? = addressesChanged?.get(0)?.locality
                    editText.setText(cityChanged)
                }

                override fun onStatusChanged(
                    provider: String,
                    status: Int,
                    extras: Bundle
                ) {
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            /*      Request location updates */
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (ex: SecurityException) {
            ex.printStackTrace()
        }
    }

    private fun addLastLocation(
        location: Location,
        editText: LinedEditText
    ) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: MutableList<Address>? = geoCoder.getFromLocation(
            location.latitude,
            location.longitude, 1
        )

        val cityName: String? = addresses?.get(0)?.locality
        editText.setText(cityName)
    }
}