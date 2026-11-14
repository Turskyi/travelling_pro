package io.github.turskyi.data

import io.github.turskyi.data.datasources.local.entities.CityEntity
import io.github.turskyi.data.datasources.local.entities.CountryEntity
import io.github.turskyi.data.datasources.local.entities.VisitedCountryEntity
import io.github.turskyi.data.datasources.remote.entities.CountryResponse
import io.github.turskyi.data.datasources.remote.entities.NameResponse
import io.github.turskyi.data.util.extensions.mapCountryToVisitedCountry
import io.github.turskyi.data.util.extensions.mapEntityToModel
import io.github.turskyi.data.util.extensions.mapModelToEntity
import io.github.turskyi.data.util.extensions.mapNetToDomain
import io.github.turskyi.data.util.extensions.mapVisitedCountryEntityToVisitedCountry
import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import org.junit.Assert.assertEquals
import org.junit.Test

class TravellingDataMappingTest {
    @Test
    fun restCountry_becomesCountryModelUsedByCountryList() {
        val model = CountryResponse(
            cca2 = "CA",
            cca3 = "CAN",
            name = NameResponse(common = "Canada"),
        ).mapNetToDomain()

        assertEquals(CountryModel("CAN", "Canada", "https://flagcdn.com/ca.svg"), model)
    }

    @Test
    fun markingCountryVisited_preservesCountryIdentityAndFlag() {
        val country = CountryModel(
            id = 124,
            shortName = "CAN",
            name = "Canada",
            flag = "https://flagcdn.com/ca.svg",
            isVisited = false,
            selfie = "",
            selfieName = "",
        )

        val visited = country.mapModelToEntity().mapCountryToVisitedCountry()

        assertEquals(
            VisitedCountryEntity(124, "CAN", "Canada", "https://flagcdn.com/ca.svg", "", ""),
            visited,
        )
    }

    @Test
    fun visitedCountry_mapsBackToUiModelWithSelfieData() {
        val entity = VisitedCountryEntity(
            id = 124,
            shortName = "CAN",
            name = "Canada",
            flag = "https://flagcdn.com/ca.svg",
            selfie = "/photos/canada.jpg",
            selfieName = "canada.jpg",
        )

        val model = entity.mapVisitedCountryEntityToVisitedCountry()

        assertEquals(124, model.id)
        assertEquals("CAN", model.shortName)
        assertEquals("Canada", model.title)
        assertEquals("https://flagcdn.com/ca.svg", model.flag)
        assertEquals("/photos/canada.jpg", model.selfie)
        assertEquals("canada.jpg", model.selfieName)
    }

    @Test
    fun cityEntity_mapsToDomainCityWithCountryAndMonth() {
        val entity = CityEntity(
            id = "city-1",
            name = "Toronto",
            parentId = 124,
            month = "July",
        )

        assertEquals(CityModel("city-1", "Toronto", 124, "July"), entity.mapEntityToModel())
    }

    @Test
    fun countryListEntity_preservesVisitedStateForAllCountriesScreen() {
        val entity = CountryEntity(
            id = 124,
            shortName = "CAN",
            name = "Canada",
            flag = "https://flagcdn.com/ca.svg",
            isVisited = true,
        )

        val model = entity.mapEntityToModel()

        assertEquals(124, model.id)
        assertEquals("CAN", model.shortName)
        assertEquals("Canada", model.name)
        assertEquals(true, model.isVisited)
    }
}
