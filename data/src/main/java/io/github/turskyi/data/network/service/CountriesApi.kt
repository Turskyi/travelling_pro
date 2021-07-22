package io.github.turskyi.data.network.service

import retrofit2.Call
import retrofit2.http.GET
import io.github.turskyi.data.constants.ApiEndpoint
import io.github.turskyi.data.entities.network.CountryListResponse

interface CountriesApi {
    @GET(ApiEndpoint.ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}