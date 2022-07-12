package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.Observer
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsFragmentViewModel
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.observeOnce
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import org.koin.android.ext.android.inject

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
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val position: Int? = this.arguments?.getInt(EXTRA_POSITION)
                binding.ivEnlargedFlag.visibility = VISIBLE
                binding.wvFlag.visibility = GONE
                val selectedImageUri: Uri? = photoChooserIntent?.data
                if (selectedImageUri.toString().contains(getString(R.string.media_providers))) {
                    val imageId: Int? =
                        selectedImageUri?.lastPathSegment?.takeLastWhile { character -> character.isDigit() }
                            ?.toInt()
                    val visitedCountriesObserverForLocalPhotos: Observer<List<VisitedCountry>> =
                        Observer<List<VisitedCountry>> { visitedCountries ->
                            val contentImg: VisitedCountry? = imageId?.let { contentImgId ->
                                position?.let {
                                    getContentUriFromUri(
                                        id = visitedCountries[position].id,
                                        imageId = contentImgId,
                                        shortName = visitedCountries[position].shortName,
                                        name = visitedCountries[position].title,
                                        flag = visitedCountries[position].flag
                                    )
                                }
                            }
                            contentImg?.selfie?.let { uri ->
                                position?.let {
                                    viewModel.updateSelfie(
                                        shortName = visitedCountries[position].shortName,
                                        selfie = uri,
                                        selfieName = visitedCountries[position].selfieName,
                                    )
                                }
                            }
                        }
                    viewModel.visitedCountries.observeOnce(
                        viewLifecycleOwner,
                        visitedCountriesObserverForLocalPhotos
                    )
                } else {
                    val visitedCountriesObserverForCloudPhotos: Observer<List<VisitedCountry>> =
                        Observer<List<VisitedCountry>> { visitedCountries ->
                            position?.let {
                                viewModel.updateSelfie(
                                    shortName = visitedCountries[position].shortName,
                                    selfie = selectedImageUri.toString(),
                                    selfieName = visitedCountries[position].selfieName
                                )
                            }
                        }
                    viewModel.visitedCountries.observeOnce(
                        viewLifecycleOwner, visitedCountriesObserverForCloudPhotos
                    )
                }
            } else {
                toast(R.string.msg_did_not_choose)
            }
        }
    }

    private fun getContentUriFromUri(
        id: Int,
        imageId: Int,
        shortName: String,
        name: String,
        flag: String
    ): VisitedCountry {
        val columns: Array<String> = arrayOf(MediaStore.Images.Media._ID)

        val orderBy: String =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) MediaStore.Images.Media.DATE_TAKEN
            else MediaStore.Images.Media._ID

        /* This cursor will hold the result of the query
        and put all data in Cursor by sorting in descending order */
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns, null, null, "$orderBy DESC"
        )
        cursor?.moveToFirst()
        val uriImage: Uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "" + imageId
        )
        val visitedCountry = VisitedCountry(
            id = id,
            shortName = shortName,
            title = name,
            flag = flag,
            selfie = uriImage.toString(),
            selfieName = "${System.currentTimeMillis()}",
        )
        cursor?.close()
        return visitedCountry
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
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            flagsActivityViewListener!!.setLoaderVisibility(currentVisibility)
        }
        val visitedCountriesObserver: Observer<List<VisitedCountry>> =
            Observer<List<VisitedCountry>> { countries ->
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