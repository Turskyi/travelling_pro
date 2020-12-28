package io.github.turskyi.data.api.datasource

import android.accounts.NetworkErrorException
import io.github.turskyi.data.api.service.CountriesApi
import org.koin.core.KoinComponent
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