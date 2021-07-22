package io.github.turskyi.data.network.datasource

import android.accounts.NetworkErrorException
import io.github.turskyi.data.network.service.CountriesApi
import org.koin.core.component.KoinComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import io.github.turskyi.data.entities.network.CountryListResponse
import io.github.turskyi.data.entities.network.CountryNet
import io.github.turskyi.data.util.throwException

class CountriesNetSource(private val countriesApi: CountriesApi) : KoinComponent {

    fun getCountryNetList(
        onComplete: (List<CountryNet>?) -> Unit,
        onError: (Exception) -> Unit) {
        countriesApi.getCategoriesFromApi().enqueue(object : Callback<CountryListResponse> {
            override fun onFailure(call: Call<CountryListResponse>, t: Throwable) {
                onError(NetworkErrorException(t))
            }

            override fun onResponse(
                call: Call<CountryListResponse>,
                response: Response<CountryListResponse>
            ) {
                if (response.isSuccessful) {
                    onComplete(response.body())
                } else {
                    onError(response.code().throwException(response.message()))
                }
            }
        })
    }
}