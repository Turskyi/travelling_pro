package io.github.turskyi.data.firestoreSource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.turskyi.data.constants.Constants.KEY_ID
import io.github.turskyi.data.constants.Constants.KEY_IS_VISITED
import io.github.turskyi.data.constants.Constants.KEY_SELFIE
import io.github.turskyi.data.constants.Constants.REF_CITIES
import io.github.turskyi.data.constants.Constants.REF_COUNTRIES
import io.github.turskyi.data.constants.Constants.REF_USERS
import io.github.turskyi.data.extensions.log
import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import org.koin.core.KoinComponent

class FirestoreSource : KoinComponent {
    /* init Authentication */
    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    private val usersRef: CollectionReference = db.collection(REF_USERS)

    fun insertAllCountries(
        countries: List<CountryModel>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        countries.forEachIndexed { index, countryModel ->
            val country =
                CountryModel(
                    id = index,
                    name = countryModel.name,
                    flag = countryModel.flag,
                    isVisited = false,
                )
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES).document(countryModel.name).set(country)
                .addOnSuccessListener {
                    if (index == countries.size - 1) {
                        onSuccess()
                    }
                }.addOnFailureListener {
                    onError?.invoke(it)
                }
        }
    }

    fun markAsVisited(
        name: String, onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(name)
        countryRef.update(KEY_IS_VISITED, true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError?.invoke(it) }
    }

    fun removeFromVisited(id: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_IS_VISITED, false)
            .addOnSuccessListener { log("country removed from visited") }
            .addOnFailureListener { e -> log("Error removing from visited ${e.message}") }
    }

    fun updateSelfie(id: String, selfie: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_SELFIE, selfie)
            .addOnSuccessListener { log("selfie successfully updated!") }
            .addOnFailureListener { e -> log("Error updating selfie : ${e.message}") }
    }

    fun insertCity(city: CityModel) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(city.id.toString()).set(city)
            .addOnSuccessListener {
                log("created city ${city.name}")
            }.addOnFailureListener {
                log("exception ${it.message}")
            }
    }

    fun removeCity(id: String) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(id)
            .delete()
            .addOnSuccessListener { log("city successfully deleted!") }
            .addOnFailureListener { e -> log("Error deleting city : ${e.message}") }
    }

    fun getVisitedCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES)
        countriesRef.whereEqualTo(KEY_IS_VISITED, true).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                if (queryDocumentSnapshots.size() == 0) {
                    onSuccess(countries)
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: CountryModel =
                            documentSnapshot.toObject(CountryModel::class.java)
                        countries.add(country)
                        if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                            onSuccess(countries.sortedBy { listItem -> listItem.name })
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
            }
    }

    fun getCities(onSuccess: (List<CityModel>) -> Unit, onError: ((Exception) -> Unit?)?) {
        val citiesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_CITIES)
        citiesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val cities: MutableList<CityModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val city: CityModel = documentSnapshot.toObject(CityModel::class.java)
                    cities.add(city)
                }
                onSuccess(cities.sortedBy { city -> city.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
            }
    }

    fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES)
        countriesRef.whereEqualTo(KEY_IS_VISITED, false).orderBy(KEY_ID).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { notVisitedCountries ->
                        onSuccess(notVisitedCountries.size())
                    }
                } else {
                    task.exception?.let { exception ->
                        onError?.invoke(exception)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES)
        countriesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                if (queryDocumentSnapshots.size() == 0) {
                    onSuccess(0, 0)
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: CountryModel =
                            documentSnapshot.toObject(CountryModel::class.java)
                        countries.add(country)
                        /** check if it is the last document in list, filter and send success */
                        if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                            val notVisitedCount: Int =
                                countries.filter { countryModel -> countryModel.isVisited == false }.size
                            val visitedCount: Int =
                                countries.filter { countryModel -> countryModel.isVisited == true }.size
                            onSuccess(notVisitedCount, visitedCount)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun getCountriesByRange(
        to: Int, from: Int, onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: Query =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES).orderBy(KEY_ID)
        countriesRef.startAt(from).endBefore(to).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryModel =
                        documentSnapshot.toObject(CountryModel::class.java)
                    countries.add(country)
                }
                onSuccess(countries)
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
            }
    }

    fun getCountriesByName(
        nameQuery: String?, onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: Query =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES).orderBy(KEY_ID)
        countriesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryModel =
                        documentSnapshot.toObject(CountryModel::class.java)
                    if (nameQuery != null && country.name.startsWith(nameQuery)) {
                        countries.add(country)
                    }
                    if (documentSnapshot == queryDocumentSnapshots.last()) {
                        onSuccess(countries)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }
}