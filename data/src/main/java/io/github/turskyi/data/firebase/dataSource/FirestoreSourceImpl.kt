package io.github.turskyi.data.firebase.dataSource

import android.net.Uri
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storageMetadata
import io.github.turskyi.data.constants.Constants.IMG_TYPE
import io.github.turskyi.data.constants.Constants.KEY_ID
import io.github.turskyi.data.constants.Constants.KEY_IS_VISITED
import io.github.turskyi.data.constants.Constants.KEY_PARENT_ID
import io.github.turskyi.data.constants.Constants.KEY_SELFIE
import io.github.turskyi.data.constants.Constants.KEY_SELFIE_NAME
import io.github.turskyi.data.constants.Constants.REF_CITIES
import io.github.turskyi.data.constants.Constants.REF_COUNTRIES
import io.github.turskyi.data.constants.Constants.REF_SELFIES
import io.github.turskyi.data.constants.Constants.REF_USERS
import io.github.turskyi.data.constants.Constants.REF_VISITED_COUNTRIES
import io.github.turskyi.data.entities.firestore.CityEntity
import io.github.turskyi.data.entities.firestore.CountryEntity
import io.github.turskyi.data.entities.firestore.VisitedCountryEntity
import io.github.turskyi.data.extensions.log
import io.github.turskyi.data.extensions.mapCountryToVisitedCountry
import io.github.turskyi.data.firebase.service.FirestoreSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class FirestoreSourceImpl : KoinComponent, FirestoreSource {
    /* init Authentication */
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private val selfiesStorageRef: StorageReference = firebaseStorage.getReference(REF_SELFIES)
    private val usersRef: CollectionReference = db.collection(REF_USERS)

    override fun insertAllCountries(
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

    override fun markAsVisited(
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

    override fun removeFromVisited(
        name: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        /** deleting from list of visited countries */
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_VISITED_COUNTRIES).document(name)
            .delete()
            .addOnSuccessListener {
                /** set mark "isVisited = false" in list of all countries */
                val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_COUNTRIES).document(name)
                countryRef.update(KEY_IS_VISITED, false)
                    .addOnSuccessListener {
                        val visitedCities: CollectionReference =
                            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                                .collection(REF_CITIES)
                        /** getting visited cities of deleted visited country */
                        visitedCities.whereEqualTo(KEY_PARENT_ID, parentId)
                            .get().addOnSuccessListener { queryDocumentSnapshots ->
                                if (queryDocumentSnapshots.size() == 0) {
                                    onSuccess()
                                } else {
                                    /* Getting a new write batch and commit all write operations */
                                    val batch: WriteBatch = db.batch()
                                    /** delete every visited city of deleted visited country */
                                    for (documentSnapshot in queryDocumentSnapshots) {
                                        batch.delete(documentSnapshot.reference)
                                    }
                                    batch.commit().addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { exception ->
                                            onError?.invoke(exception)
                                        }
                                }
                            }.addOnFailureListener { exception -> onError?.invoke(exception) }
                    }.addOnFailureListener { exception -> onError?.invoke(exception) }
            }.addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    override fun updateSelfie(
        name: String,
        selfie: String,
        previousSelfieName: String?,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val selfieImage: Uri = Uri.parse(selfie)
        val metadata = storageMetadata {
            contentType = IMG_TYPE
        }

        val selfieName = "${System.currentTimeMillis()}"
        val selfieRef: StorageReference = selfiesStorageRef.child(selfieName)

        /** In the putFile method, there is a TaskSnapshot which contains the details of uploaded file */
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

                /** before saving a new image deleting previous image */
                deleteImage(previousSelfieName, {
                    /** if deleting is successful , saving new url and new image name to model */
                    countryRef.update(
                        mapOf(
                            KEY_SELFIE to uploadedSelfieUrl,
                            KEY_SELFIE_NAME to selfieName
                        )
                    ).addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onError?.invoke(exception) }
                }, { exception -> onError?.invoke(exception) })
            } else {
                // Handle failures
                task.exception?.let { exception ->
                    onError?.invoke(exception)
                }
            }
        }
    }

    private fun deleteImage(
        selfieName: String?,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            log("$selfieName")
            if (selfieName != null) {
                selfiesStorageRef.child(selfieName).delete()
                    .addOnSuccessListener {
                        log("deleted")
                        onSuccess()
                    }
                    .addOnFailureListener { exception -> onError?.invoke(exception) }
            } else {
                onSuccess()
            }
        } catch (exception: Exception) {
            onError?.invoke(exception)
        }
    }

    override fun insertCity(
        city: CityEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(city.name).set(city)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    override fun removeCity(
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

    override fun getVisitedCountries(
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

    override fun getCities(
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

    override fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val currentUserId = mFirebaseAuth.currentUser?.uid
        if (currentUserId != null) {
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
        } else {
            mFirebaseAuth.signOut()
        }
    }

    override fun getCountNotVisitedAndVisitedCountries(
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

    override fun getCountriesByRange(
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

    override fun getCountriesByName(
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