package io.github.turskyi.data.datasources.remote

import android.accounts.NetworkErrorException
import io.github.turskyi.data.datasources.remote.entities.CountryListResponse
import io.github.turskyi.data.datasources.remote.entities.CountryResponse
import io.github.turskyi.data.util.exceptions.NotFoundException
import io.github.turskyi.data.util.throwException
import org.koin.core.component.KoinComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetSource(private val countriesApi: CountriesApi) : KoinComponent {

    fun getCountryNetList(
        onComplete: (List<CountryResponse>) -> Unit,
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
                    if(response.body() != null){
                        onComplete(response.body()!!)
                    } else {
                        onError(NotFoundException())
                    }
                } else {
                    onError(response.code().throwException(response.message()))
                }
            }
        })
    }
}