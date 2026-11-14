package io.github.turskyi.travellingpro

import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToBaseNodeList
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedListToVisitedNodeList
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedModelListToVisitedList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitedCountryFlowTest {
    @Test
    fun visitedCountries_mapToUiCountriesAndExpandableNodes() {
        val models = listOf(
            VisitedCountryModel(
                id = 124,
                shortName = "CAN",
                title = "Canada",
                flag = "https://flagcdn.com/ca.svg",
                selfie = "",
                selfieName = "",
            ),
            VisitedCountryModel(
                id = 840,
                shortName = "USA",
                title = "United States",
                flag = "https://flagcdn.com/us.svg",
                selfie = "/photos/usa.jpg",
                selfieName = "usa.jpg",
            ),
        )

        val countries = models.mapVisitedModelListToVisitedList()
        val nodes = countries.mapVisitedListToVisitedNodeList()

        assertEquals(2, countries.size)
        assertEquals(124, countries[0].id)
        assertEquals("Canada", countries[0].title)
        assertEquals("usa.jpg", countries[1].selfieName)
        assertEquals(2, nodes.size)
        assertEquals(124, nodes[0].id)
        assertEquals("Canada", nodes[0].title)
        assertTrue(nodes[0].childNode.isEmpty())
        assertEquals(840, nodes[1].id)
        assertEquals("usa.jpg", nodes[1].selfieName)
        assertTrue(nodes[1].childNode.isEmpty())
    }

    @Test
    fun cities_mapToNodesWithTheirCountryAssociationAndVisitMonth() {
        val cities = listOf(
            CityModel(id = "city-1", name = "Toronto", parentId = 124, month = "July"),
            CityModel(id = "city-2", name = "Vancouver", parentId = 124, month = "August"),
        )

        val nodes = cities.mapModelListToBaseNodeList()

        assertEquals(2, nodes.size)
        assertEquals("city-1", nodes[0].id)
        assertEquals("Toronto", nodes[0].name)
        assertEquals(124, nodes[0].parentId)
        assertEquals("July", nodes[0].month)
        assertEquals(124, nodes[1].parentId)
        assertEquals("August", nodes[1].month)
    }
}
