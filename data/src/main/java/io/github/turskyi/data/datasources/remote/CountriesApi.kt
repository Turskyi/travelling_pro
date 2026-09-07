package io.github.turskyi.data.datasources.remote

import io.github.turskyi.data.datasources.remote.entities.CountryListResponse
import retrofit2.Call
import retrofit2.http.GET

interface CountriesApi {
    companion object {
        const val ENDPOINT_NAME = "mledoze/countries/master/dist/countries.json"
    }
    @GET(ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}