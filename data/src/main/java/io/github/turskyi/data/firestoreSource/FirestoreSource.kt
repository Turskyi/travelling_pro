package io.github.turskyi.data.firestoreSource

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storageMetadata
import io.github.turskyi.data.constants.Constants.IMG_TYPE
import io.github.turskyi.data.constants.Constants.KEY_ID
import io.github.turskyi.data.constants.Constants.KEY_IS_VISITED
import io.github.turskyi.data.constants.Constants.KEY_SELFIE
import io.github.turskyi.data.constants.Constants.REF_CITIES
import io.github.turskyi.data.constants.Constants.REF_COUNTRIES
import io.github.turskyi.data.constants.Constants.REF_SELFIES
import io.github.turskyi.data.constants.Constants.REF_USERS
import io.github.turskyi.data.constants.Constants.REF_VISITED_COUNTRIES
import io.github.turskyi.data.entities.firestore.CityEntity
import io.github.turskyi.data.entities.firestore.CountryEntity
import io.github.turskyi.data.entities.firestore.VisitedCountryEntity
import io.github.turskyi.data.extensions.mapCountryToVisitedCountry
import org.koin.core.KoinComponent

class FirestoreSource : KoinComponent {
    /* init Authentication */
    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference = firebaseStorage.getReference(REF_SELFIES)

    private val usersRef: CollectionReference = db.collection(REF_USERS)

    fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = countries.forEachIndexed { index, countryEntity ->
        val country = CountryEntity(
            id = index,
            name = countryEntity.name,
            flag = countryEntity.flag,
            isVisited = false,
        )
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(countryEntity.name).set(country)
            .addOnSuccessListener {
                if (index == countries.size - 1) {
                    onSuccess()
                }
            }.addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        /** set mark "isVisited = true" in list of all countries */
        val countryRef: DocumentReference = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(countryEntity.name)
        countryRef.update(KEY_IS_VISITED, true)
            .addOnSuccessListener {
                /** making copy of the country and adding to a new list of visited countries */
                usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_VISITED_COUNTRIES).document(countryEntity.name)
                    .set(countryEntity.mapCountryToVisitedCountry())
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception -> onError?.invoke(exception) }
            }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun removeFromVisited(name: String, onSuccess: () -> Unit, onError: ((Exception) -> Unit?)?) {
        /** deleting from list of visited countries */
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_VISITED_COUNTRIES).document(name)
            .delete()
            .addOnSuccessListener {
                /** set mark "isVisited = false" in list of all countries */
                val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_COUNTRIES).document(name)
                countryRef.update(KEY_IS_VISITED, false)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception -> onError?.invoke(exception) }
            }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun updateSelfie(
        name: String,
        selfie: String,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val selfieImage: Uri = Uri.parse(selfie)
        val metadata = storageMetadata {
            contentType = IMG_TYPE
        }

        /** In the putFile method, there is a TaskSnapshot which contains the details of uploaded file */
        val selfieRef: StorageReference = storageReference.child("${selfieImage.lastPathSegment}")
        val uploadTask: UploadTask = selfieRef.putFile(selfieImage, metadata)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { exception ->
                    onError?.invoke(exception)
                }
            }
            return@continueWithTask selfieRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                /* Upload URL is needed to save it to the database */
                val downloadUri: Uri? = task.result
                /* We are using uri as String because our data type in Firestore will be String */
                val uploadedSelfieUrl: String = downloadUri.toString()

                /** Saving the URL to the database */
                val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_VISITED_COUNTRIES).document(name)
                countryRef.update(KEY_SELFIE, uploadedSelfieUrl)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onError?.invoke(exception)}
            } else {
                // Handle failures
                task.exception?.let { exception ->
                    onError?.invoke(exception)
                }
            }
        }
    }

    fun insertCity(
        city: CityEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(city.name).set(city)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun removeCity(
        name: String,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(name)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun getVisitedCountries(
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_VISITED_COUNTRIES)
        countriesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.size() == 0) {
                    onSuccess(emptyList())
                } else {
                    val countries: MutableList<VisitedCountryEntity> = mutableListOf()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: VisitedCountryEntity =
                            documentSnapshot.toObject(VisitedCountryEntity::class.java)
                        countries.add(country)
                        if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                            onSuccess(countries.sortedBy { listItem -> listItem.name })
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun getCities(
        onSuccess: (List<CityEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val citiesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_CITIES)
        citiesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.size() == 0) {
                    onSuccess(emptyList())
                } else {
                    val cities: MutableList<CityEntity> = mutableListOf()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val cityEntity: CityEntity =
                            documentSnapshot.toObject(CityEntity::class.java)
                        cities.add(cityEntity)
                        if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                            onSuccess(cities.sortedBy { city -> city.name })
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
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
                val countries: MutableList<CountryEntity> = mutableListOf()
                if (queryDocumentSnapshots.size() == 0) {
                    onSuccess(0, 0)
                } else {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: CountryEntity =
                            documentSnapshot.toObject(CountryEntity::class.java)
                        countries.add(country)
                        /** check if it is the last document in list, filter and send success */
                        if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                            val notVisitedCount: Int =
                                countries.filter { countryEntity -> countryEntity.isVisited == false }.size
                            val visitedCount: Int =
                                countries.filter { countryEntity -> countryEntity.isVisited == true }.size
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
        to: Int, from: Int, onSuccess: (List<CountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: Query =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES).orderBy(KEY_ID)
        countriesRef.startAt(from).endBefore(to).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryEntity> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryEntity =
                        documentSnapshot.toObject(CountryEntity::class.java)
                    countries.add(country)
                }
                onSuccess(countries)
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun getCountriesByName(
        nameQuery: String?, onSuccess: (List<CountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: Query =
            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                .collection(REF_COUNTRIES).orderBy(KEY_ID)
        countriesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryEntity> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryEntity =
                        documentSnapshot.toObject(CountryEntity::class.java)
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