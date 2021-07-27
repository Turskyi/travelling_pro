package io.github.turskyi.data.network.service

import io.github.turskyi.data.entities.network.CountryListResponse
import retrofit2.Call
import retrofit2.http.GET

interface CountriesApi {
    companion object {
        const val ENDPOINT_NAME = "rest/v2/all"
    }
    @GET(ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}