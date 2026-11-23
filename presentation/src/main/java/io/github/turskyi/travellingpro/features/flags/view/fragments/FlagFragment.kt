package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.view.View.GONE
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.Observer
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsFragmentViewModel
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.observeOnce
import io.github.turskyi.travellingpro.utils.extensions.showReportDialog
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class FlagFragment : BaseFlagFragment() {

    private val viewModel: FlagsFragmentViewModel by inject()

    private lateinit var photoPickerResultLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initResultLauncher()
    }

    override fun onResume() {
        super.onResume()
        initListeners()
        initObservers()
    }

    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private fun initResultLauncher() {
        photoPickerResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val selectedImageUri: Uri? = photoChooserIntent?.data
                val position: Int? = this.arguments?.getInt(EXTRA_POSITION)
                if (selectedImageUri != null && position != null) {
                    createInputStreamAndChangeImage(selectedImageUri, position)
                } else {
                    requireContext().showReportDialog()
                }
            } else {
                toast(R.string.msg_did_not_choose)
            }
        }
    }

    private fun createInputStreamAndChangeImage(selectedImageUri: Uri, position: Int) {
        var imageName: String? = null
        try {
            val projectionColumns: Array<String> = arrayOf(OpenableColumns.DISPLAY_NAME)
            requireContext().contentResolver.query(
                selectedImageUri,
                projectionColumns,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        imageName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FlagFragment", "Failed to query image name from URI", e)
        }
        val name: String = imageName ?: "selfie_${System.currentTimeMillis()}.jpg"
        val inputStream: InputStream? = try {
            requireContext().contentResolver.openInputStream(selectedImageUri)
        } catch (e: Exception) {
            Log.w("FlagFragment", "Failed to open input stream for URI", e)
            null
        }
        if (inputStream != null) {
            updateFlagImage(name, inputStream, position)
        }
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
    }

    private fun updateFlagImage(name: String, inputStream: InputStream, position: Int) {
        val file = File(requireContext().cacheDir, name)
        val fileOutputStream: FileOutputStream = file.outputStream()
        fileOutputStream.use { inputStream.copyTo(it) }
        val visitedCountriesObserverForLocalPhotos: Observer<List<VisitedCountry>> =
            Observer { visitedCountries: List<VisitedCountry> ->
                if (position in visitedCountries.indices) {
                    viewModel.updateSelfie(
                        shortName = visitedCountries[position].shortName,
                        filePath = file.absolutePath,
                        selfieName = visitedCountries[position].selfieName,
                    )
                }
            }
        viewModel.visitedCountries.observeOnce(
            viewLifecycleOwner,
            visitedCountriesObserverForLocalPhotos
        )
    }

    private fun addSelfieLongClickListener(): OnLongClickListener = OnLongClickListener {
        initPhotoPicker()
        return@OnLongClickListener true
    }

    private fun initPhotoPicker() {
        val action: String = Intent.ACTION_OPEN_DOCUMENT
        val intent = Intent(action)
        intent.type = getString(R.string.image_and_jpeg_type)
        val intentChooser: Intent =
            Intent.createChooser(intent, getString(R.string.flag_chooser_title_complete_using))
        photoPickerResultLauncher.launch(intentChooser)
    }

    private fun initListeners() {
        binding.wvFlag.isLongClickable = true
        binding.ivEnlargedFlag.setOnLongClickListener(addSelfieLongClickListener())
        binding.wvFlag.setOnLongClickListener(addSelfieLongClickListener())
        binding.tvFlagFallback.setOnLongClickListener(addSelfieLongClickListener())
    }

    private fun initObservers() {
        lifecycle.addObserver(viewModel)
        viewModel.errorMessage.observe(this) { event: Event<String> ->
            val message: String? = event.getMessageIfNotHandled()
            if (message != null) {
                toastLong(message)
            }
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility: Int ->
            if (flagsActivityViewListener != null) {
                flagsActivityViewListener!!.setLoaderVisibility(currentVisibility)
            }
        }
        val visitedCountriesObserver: Observer<List<VisitedCountry>> =
            Observer { countries: List<VisitedCountry> ->
                val position: Int = this.requireArguments().getInt(EXTRA_POSITION)
                if (mChangeFlagListener != null) {
                    mChangeFlagListener!!.onChangeToolbarTitle(countries[position].title)
                }
                if (countries[position].selfie.isEmpty() && countries[position].selfieName.isEmpty()) {
                    showTheFlag(countries, position)
                } else {
                    showSelfie(countries, position)
                    binding.ivEnlargedFlag.setOnClickListener(
                        showFlagClickListener(countries, position)
                    )
                }
            }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }
}