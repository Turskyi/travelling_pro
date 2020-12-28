package io.github.turskyi.travellingpro.features.home.viewmodels

import android.app.Application
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.chad.library.adapter.base.entity.node.BaseNode
import com.firebase.ui.auth.AuthUI
import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.common.App
import io.github.turskyi.travellingpro.extensions.*
import io.github.turskyi.travellingpro.models.City
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.models.VisitedCountry
import io.github.turskyi.travellingpro.utils.Event
import kotlinx.coroutines.launch

class HomeActivityViewModel(private val interactor: CountriesInteractor, application: Application) :
    AndroidViewModel(application), LifecycleObserver {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    val visitedCountries: LiveData<List<Country>>
        get() = _visitedCountries

    private val _visitedCountriesWithCities = MutableLiveData<List<VisitedCountry>>()
    val visitedCountriesWithCities: LiveData<List<VisitedCountry>>
        get() = _visitedCountriesWithCities

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _navigateToAllCountries = MutableLiveData<Boolean>()
    val navigateToAllCountries: LiveData<Boolean>
        get() = _navigateToAllCountries

    init {
        log(" init viewmodel")
        _visibilityLoader.postValue(VISIBLE)
    }

    val getCountries: () -> Unit = {
        viewModelScope.launch {
            interactor.getNotVisitedCountriesNum({ notVisitedCountriesNum ->
                log("get count : $notVisitedCountriesNum")
                if (notVisitedCountriesNum == 0) {
                    viewModelScope.launch {
                        downloadCountries()
                    }
                } else {
                    getVisitedCountries()
                    notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                }
            }, { exception ->
//                getVisitedCountries()
                log("get count error : message => ${exception.message}")
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    //    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifecycleResume() {
        _visibilityLoader.postValue(GONE)
        log("resume in viewmodel")
    }

    fun initAuthentication(authorizationResultLauncher: ActivityResultLauncher<Intent>) {
        log("auth init")
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
            .setAvailableProviders(providers)
            /** Set logo drawable */
            .setLogo(R.drawable.pic_logo)
            .setTheme(R.style.AuthTheme)
            .setTosAndPrivacyPolicyUrls(
//                TODO: replace with Terms of service
                getApplication<App>().getString(R.string.privacy_web_page),
                getApplication<App>().getString(R.string.privacy_web_page)
            )
            .build()
    }

    fun initListOfCountries() {
        getCountries()
    }

    private suspend fun downloadCountries() {
        log("start download")
        interactor.downloadCountries({
            log("start getting countries from db")
            getCountries()
        }, { exception ->
            log("download countries error ${exception.message}")
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    /* Trigger the event by setting a new Event as a new value */
                    postValue(Event(message))
                }
            }
//            loadCountries()
        })
    }

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    private fun getVisitedCountries() {
        viewModelScope.launch {
            interactor.getVisitedModelCountries({ countries ->
                log("after get")
                val visitedCountries = countries.mapModelListToNodeList()
                for (country in visitedCountries) {
                    val cityList = mutableListOf<BaseNode>()
                    viewModelScope.launch {
                        interactor.getCities({ cities ->
                            for (city in cities) {
                                if (country.id == city.parentId) {
                                    cityList.add(city.mapModelToBaseNode())
                                }
                            }
                            citiesCount = cities.size
                        }, { exception ->
                            exception.printStackTrace()
                            _visibilityLoader.postValue(GONE)
                            _errorMessage.run {
                                exception.message?.let { message ->
                                    /* Trigger the event by setting a new Event as a new value */
                                    postValue(Event(message))
                                }
                            }
                        })
                    }
                    country.childNode = cityList
                }
                _visitedCountriesWithCities.run { postValue(visitedCountries) }
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
                _visibilityLoader.postValue(GONE)
            }, { exception ->
                log("get visited error ${exception.message}")
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    fun removeFromVisited(country: Country) {
        viewModelScope.launch {
            interactor.removeCountryModelFromVisitedList(country.mapActualToModel())
            initListOfCountries()
        }
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            interactor.removeCity(city.mapNodeToModel())
            initListOfCountries()
        }
    }
}