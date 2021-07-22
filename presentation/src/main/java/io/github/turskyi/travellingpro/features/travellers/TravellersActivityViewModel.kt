package io.github.turskyi.travellingpro.features.travellers

import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.features.travellers.view.adapter.FilteredTravellersPositionalDataSource
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersPositionalDataSource
import io.github.turskyi.travellingpro.models.Traveller
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.MainThreadExecutor
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TravellersActivityViewModel(private val interactor: TravellersInteractor) : ViewModel() {

    private val _topTravellersPercentLiveData = MutableLiveData<Int>()
    val topTravellersPercentLiveData: MutableLiveData<Int>
        get() = _topTravellersPercentLiveData

    private var _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    var pagedList: PagedList<Traveller>

    var searchQuery = ""
        set(value) {
            field = value
            pagedList = getUserList(value)
        }

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    init {
        _visibilityLoader.postValue(VISIBLE)
        setTopTravellersNum()
        pagedList = getUserList(searchQuery)
    }

    private fun getUserList(searchQuery: String): PagedList<Traveller> = if (searchQuery == "") {
        // PagedList
        val config: PagedList.Config = PagedList.Config.Builder()
            /* If "true", then it should be created another viewType in Adapter "onCreateViewHolder"
               while uploading */
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()
        // DataSource
        val dataSource = TravellersPositionalDataSource(interactor)
        _visibilityLoader = dataSource.visibilityLoader
        PagedList.Builder(dataSource, config)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .setNotifyExecutor(MainThreadExecutor())
            .build()
    } else {
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(1)
            .setPageSize(1)
            .build()
        val filteredDataSource =
            FilteredTravellersPositionalDataSource(userName = searchQuery, interactor = interactor)
        PagedList.Builder(filteredDataSource, config)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .setNotifyExecutor(MainThreadExecutor())
            .build()
    }

    private fun setTopTravellersNum() = viewModelScope.launch {
//TODO: implement setting number of better travellers
    }
}