package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View.*
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
        val orderBy: String =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                MediaStore.Images.Media.DATE_TAKEN
            } else {
                MediaStore.Images.Media._ID
            }
        val projectionColumns: Array<String> =
            arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        // Constructs a selection clause with a replaceable parameter
        val selectionClause = "var = ?"
        // Defines a mutable list to contain the selection arguments
        val selectionArgs: MutableList<String> = mutableListOf()
        requireContext().contentResolver.query(
            selectedImageUri,  // The content URI of the image table
            projectionColumns, // The columns to return for each row
            selectionClause, // Selection criteria
            selectionArgs.toTypedArray(), // Selection criteria
            "$orderBy DESC", // The sort order for the returned rows
        )?.use { cursor: Cursor ->
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(selectedImageUri)
            if (inputStream != null && cursor.moveToFirst()) {
                updateFlagImage(cursor, inputStream, position)
            }
        }
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
    }

    private fun updateFlagImage(cursor: Cursor, inputStream: InputStream, position: Int) {
        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val name: String = cursor.getString(nameIndex)
        // create same file with same name
        val file = File(requireContext().cacheDir, name)
        val fileOutputStream: FileOutputStream = file.outputStream()
        fileOutputStream.use { inputStream.copyTo(it) }
        val visitedCountriesObserverForLocalPhotos: Observer<List<VisitedCountry>> =
            Observer<List<VisitedCountry>> { visitedCountries: List<VisitedCountry> ->
                viewModel.updateSelfie(
                    shortName = visitedCountries[position].shortName,
                    filePath = file.absolutePath,
                    selfieName = visitedCountries[position].selfieName,
                )
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
            flagsActivityViewListener!!.setLoaderVisibility(currentVisibility)
        }
        val visitedCountriesObserver: Observer<List<VisitedCountry>> =
            Observer<List<VisitedCountry>> { countries: List<VisitedCountry> ->
                val position: Int = this.requireArguments().getInt(EXTRA_POSITION)
                mChangeFlagListener!!.onChangeToolbarTitle(countries[position].title)
                if (countries[position].selfie.isEmpty()) {
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