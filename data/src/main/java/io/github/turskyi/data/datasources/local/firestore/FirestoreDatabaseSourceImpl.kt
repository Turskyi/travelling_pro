package io.github.turskyi.data.datasources.local.firestore

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storageMetadata
import io.github.turskyi.data.R
import io.github.turskyi.data.datasources.local.entities.CityEntity
import io.github.turskyi.data.datasources.local.entities.CountryEntity
import io.github.turskyi.data.datasources.local.entities.TravellerEntity
import io.github.turskyi.data.datasources.local.entities.VisitedCountryEntity
import io.github.turskyi.data.util.exceptions.BadRequestException
import io.github.turskyi.data.util.exceptions.NetworkErrorException
import io.github.turskyi.data.util.exceptions.NotFoundException
import io.github.turskyi.data.util.extensions.mapCountryToVisitedCountry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import java.io.ByteArrayOutputStream

class FirestoreDatabaseSourceImpl(
    private val application: Application,
    private val applicationScope: CoroutineScope,
) : KoinComponent,
    FirestoreDatabaseSource {
    companion object {
        // constants for firestore
        private const val REF_SELFIES = "selfies"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_COUNTRIES = "countries"
        private const val COLLECTION_VISITED_COUNTRIES = "visited_countries"
        private const val COLLECTION_CITIES = "cities"
        private const val KEY_IS_VISITED = "isVisited"
        private const val KEY_IS_VISIBLE = "isVisible"
        private const val KEY_SELFIE = "selfie"
        private const val KEY_SELFIE_NAME = "selfieName"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_PARENT_ID = "parentId"
        private const val KEY_COUNTER = "counter"
        private const val KEY_FLAG = "flag"
    }

    // init Authentication
    private val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val currentUser: FirebaseUser? = mFirebaseAuth.currentUser
    private val selfiesStorageRef: StorageReference = firebaseStorage.getReference(REF_SELFIES)
    private val usersRef: CollectionReference = database.collection(COLLECTION_USERS)

    override suspend fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (currentUser != null) {
            val userRef: DocumentReference = usersRef.document(currentUser.uid)
            val traveller = if (currentUser.displayName != null && currentUser.photoUrl != null) {
                TravellerEntity(
                    id = userRef.id,
                    name = currentUser.displayName!!,
                    avatar = currentUser.photoUrl.toString(),
                    counter = 0,
                    isVisible = false,
                )
            } else if (currentUser.displayName != null && currentUser.photoUrl == null) {
                TravellerEntity(
                    id = userRef.id,
                    name = currentUser.displayName!!,
                    avatar = "",
                    counter = 0,
                    isVisible = false,
                )
            } else if (currentUser.photoUrl != null && currentUser.displayName == null) {
                TravellerEntity(id = userRef.id, avatar = currentUser.photoUrl.toString())
            } else {
                TravellerEntity(id = userRef.id)
            }
            userRef.set(traveller)
                .addOnSuccessListener { onSuccess.invoke() }
                .addOnFailureListener { e -> onError.invoke(e) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setUserVisibility(
        visible: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val userRef: DocumentReference = usersRef.document(currentUser.uid)
            userRef.update(KEY_IS_VISIBLE, visible)
                .addOnSuccessListener { onSuccess.invoke() }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setUserVisibility(
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val userRef: DocumentReference = usersRef.document(currentUser.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document?.getBoolean(KEY_IS_VISIBLE) != null) {
                        val isVisible: Boolean = document.getBoolean(KEY_IS_VISIBLE)!!
                        onSuccess(isVisible)
                    } else {
                        onError.invoke(NotFoundException())
                    }
                }.addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun getCountNotVisitedCountriesById(
        id: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val countriesRef: CollectionReference = usersRef
            .document(id)
            .collection(COLLECTION_COUNTRIES)
        countriesRef.whereEqualTo(KEY_IS_VISITED, false).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { notVisitedCountries ->
                        onSuccess(notVisitedCountries.size())
                    }
                } else {
                    task.exception?.let { exception -> onError.invoke(exception) }
                }
            }
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countriesRef: CollectionReference = usersRef
                .document(currentUser.uid)
                .collection(COLLECTION_COUNTRIES)
            countriesRef.whereEqualTo(KEY_IS_VISITED, false).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { notVisitedCountries ->
                            onSuccess(notVisitedCountries.size())
                        }
                    } else {
                        task.exception?.let { exception -> onError.invoke(exception) }
                    }
                }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun getCityCount(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        if (currentUser != null) {
            val countriesRef: CollectionReference = usersRef
                .document(currentUser.uid)
                .collection(COLLECTION_CITIES)
            countriesRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            onSuccess(task.result!!.size())
                        } else {
                            onError.invoke(NotFoundException())
                        }
                    } else {
                        if (task.exception != null) {
                            onError.invoke(task.exception!!)
                        } else {
                            onError.invoke(BadRequestException())
                        }
                    }
                }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun getCityCount(
        userId: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val citiesRef: CollectionReference = usersRef
            .document(userId)
            .collection(COLLECTION_CITIES)
        citiesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        onSuccess(task.result!!.size())
                    } else {
                        onError.invoke(NotFoundException())
                    }
                } else {
                    if (task.exception != null) {
                        onError.invoke(task.exception!!)
                    } else {
                        onError.invoke(BadRequestException())
                    }
                }
            }
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        countries.forEachIndexed { index: Int, countryEntity: CountryEntity ->
            val country = CountryEntity(
                id = index,
                shortName = countryEntity.shortName,
                name = countryEntity.name,
                flag = countryEntity.flag,
                isVisited = false,
            )
            if (currentUser != null) {
                usersRef.document(currentUser.uid)
                    .collection(COLLECTION_COUNTRIES).document(countryEntity.shortName).set(country)
                    .addOnSuccessListener {
                        if (index == countries.lastIndex) {
                            onSuccess.invoke()
                        }
                    }.addOnFailureListener { exception ->
                        onError.invoke(exception)
                    }
            } else {
                mFirebaseAuth.signOut()
                onError.invoke(NotFoundException())
            }
        }
    }

    override suspend fun insertAllFlags(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        countries.forEachIndexed { index: Int, countryEntity: CountryEntity ->
            if (currentUser != null) {
                usersRef.document(currentUser.uid)
                    .collection(COLLECTION_COUNTRIES).document(countryEntity.shortName).update(
                        mapOf(KEY_FLAG to countryEntity.flag)
                    )
                    .addOnSuccessListener {
                        if (index == countries.size - 1) {
                            onSuccess.invoke()
                        }
                    }.addOnFailureListener { exception ->
                        onError.invoke(exception)
                    }
            } else {
                mFirebaseAuth.signOut()
                onError.invoke(NotFoundException())
            }
        }
    }

    override suspend fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val userId: String = currentUser.uid
            val userDocRef: DocumentReference = usersRef.document(userId)
            // set mark "isVisited = true" in list of all countries
            val countryRef: DocumentReference = userDocRef
                .collection(COLLECTION_COUNTRIES)
                .document(countryEntity.shortName)
            countryRef.update(KEY_IS_VISITED, true)
                .addOnSuccessListener {
                    addToListOfVisited(userDocRef, countryEntity, onSuccess, onError)
                }.addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    /** making copy of the country and adding to a new list of visited countries */
    private fun addToListOfVisited(
        userDocRef: DocumentReference,
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        userDocRef
            .collection(COLLECTION_VISITED_COUNTRIES)
            .document(countryEntity.shortName)
            .set(countryEntity.mapCountryToVisitedCountry())
            .addOnSuccessListener { incrementUserVisitedCounter(userDocRef, onSuccess, onError) }
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    private fun incrementUserVisitedCounter(
        userDocRef: DocumentReference,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document?.toObject(TravellerEntity::class.java) != null) {
                    val traveller: TravellerEntity =
                        document.toObject(TravellerEntity::class.java)!!
                    // getting current number of visited countries by user
                    var countOfVisitedCountries: Long = traveller.counter
                    // incrementing this value by one
                    countOfVisitedCountries += 1
                    // updating this value in database
                    userDocRef
                        .update(KEY_COUNTER, countOfVisitedCountries)
                        .addOnSuccessListener { onSuccess.invoke() }
                        .addOnFailureListener { e -> onError.invoke(e) }
                } else {
                    mFirebaseAuth.signOut()
                    onError.invoke(NotFoundException())
                }
            }
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun removeCountryFromVisited(
        shortName: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            // deleting from list of visited countries
            usersRef.document(currentUser.uid)
                .collection(COLLECTION_VISITED_COUNTRIES)
                .document(shortName)
                .delete()
                .addOnSuccessListener { markAsNotVisited(shortName, parentId, onSuccess, onError) }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    /** set mark "isVisited = false" in list of all countries */
    private fun markAsNotVisited(
        shortName: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countryRef: DocumentReference =
                usersRef.document(currentUser.uid).collection(COLLECTION_COUNTRIES)
                    .document(shortName)
            countryRef.update(KEY_IS_VISITED, false)
                .addOnSuccessListener {
                    /*
                     * The [runBlocking] function blocks the current thread
                     * until the code it contains has finished running,
                     * it allows us to launch many coroutines in one thread
                     */
                    runBlocking {
                        applicationScope.launch(Dispatchers.IO) {
                            decrementTravellerVisitedCounter(currentUser, onSuccess, onError)
                        }
                        deleteCitiesByCountry(currentUser, parentId, onSuccess, onError)
                    }
                }.addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    private fun decrementTravellerVisitedCounter(
        currentUser: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId: String = currentUser.uid
        val userDocRef: DocumentReference = usersRef.document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document?.toObject(TravellerEntity::class.java) != null) {
                    val traveller: TravellerEntity =
                        document.toObject(TravellerEntity::class.java)!!
                    // getting current number of visited countries by user
                    var countOfVisitedCountries: Long = traveller.counter
                    // decrement this value by one
                    countOfVisitedCountries -= 1
                    // updating this value in database
                    userDocRef
                        .update(KEY_COUNTER, countOfVisitedCountries)
                        .addOnSuccessListener { onSuccess.invoke() }
                        .addOnFailureListener { e -> onError.invoke(e) }
                } else {
                    mFirebaseAuth.signOut()
                    onError.invoke(NotFoundException())
                }
            }
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    private fun deleteCitiesByCountry(
        currentUser: FirebaseUser,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val visitedCities: CollectionReference = usersRef.document(currentUser.uid).collection(
            COLLECTION_CITIES,
        )
        // getting visited cities of deleted visited country
        visitedCities.whereEqualTo(KEY_PARENT_ID, parentId)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.isEmpty) {
                    onSuccess.invoke()
                } else {
                    // Getting a new write batch and commit all write operations
                    val batch: WriteBatch = database.batch()
                    // delete every visited city of deleted visited country
                    for (documentSnapshot in queryDocumentSnapshots) {
                        batch.delete(documentSnapshot.reference)
                    }
                    batch
                        .commit()
                        .addOnSuccessListener { onSuccess.invoke() }
                        .addOnFailureListener { exception -> onError.invoke(exception) }
                }
            }.addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun updateSelfie(
        shortName: String,
        filePath: String,
        previousSelfieName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val selfieName = "${System.currentTimeMillis()}"
        val selfieRef: StorageReference = selfiesStorageRef.child(selfieName)
        val bitmap: Bitmap = BitmapFactory.decodeFile(filePath)
        val metadata: StorageMetadata = storageMetadata {
            contentType = application.resources.getString(R.string.image_and_jpg_type)
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        // check if image more than 200 kb
        if (bitmap.byteCount > 200000) {
            // reduce size of the image to 25% of the initial quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        }
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        /* In the putBytes method,
             * there is a TaskSnapshot which contains the details of uploaded file */
        val uploadTask: UploadTask = selfieRef.putBytes(byteArray, metadata)
        uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            if (!task.isSuccessful && task.exception != null) {
                onError.invoke(task.exception!!)
            }
            selfieRef.downloadUrl
        }.addOnCompleteListener { task: Task<Uri> ->
            if (task.isSuccessful) {
                // Upload URL is needed to save it to the database
                val downloadUri: Uri? = task.result
                // We are using uri as String because our data type in Firestore will be String
                val uploadedSelfieUrl: String = downloadUri.toString()
                if (currentUser != null) {
                    // Saving the URL to the database
                    val countryRef: DocumentReference = usersRef
                        .document(currentUser.uid)
                        .collection(COLLECTION_VISITED_COUNTRIES)
                        .document(shortName)

                    // before saving a new image deleting previous image
                    deleteImage(
                        selfieName = previousSelfieName,
                        onSuccess = {
                            // if deleting is successful, saving new url and new image name to model
                            countryRef.update(
                                mapOf(
                                    KEY_SELFIE to uploadedSelfieUrl,
                                    KEY_SELFIE_NAME to selfieName
                                )
                            )
                                .addOnSuccessListener { onSuccess.invoke() }
                                .addOnFailureListener { exception -> onError.invoke(exception) }
                        },
                        onError = { exception -> onError.invoke(exception) },
                    )
                } else {
                    mFirebaseAuth.signOut()
                    onError.invoke(NotFoundException())
                }
            } else {
                // Handle failures
                if (task.exception != null) {
                    onError.invoke(task.exception!!)
                } else {
                    mFirebaseAuth.signOut()
                    onError.invoke(NetworkErrorException())
                }
            }
        }
    }

    private fun deleteImage(
        selfieName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            if (selfieName != "") {
                selfiesStorageRef.child(selfieName).delete()
                    .addOnSuccessListener { onSuccess.invoke() }
                    .addOnFailureListener { exception -> onError.invoke(exception) }
            } else {
                onSuccess.invoke()
            }
        } catch (exception: Exception) {
            onError.invoke(exception)
        }
    }

    override suspend fun insertCity(
        city: CityEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val userDocRef: DocumentReference = usersRef.document(currentUser.uid)
            userDocRef.collection(COLLECTION_VISITED_COUNTRIES).whereEqualTo(KEY_ID, city.parentId)
                .get()
                .addOnSuccessListener { documents: QuerySnapshot ->
                    for (document in documents) {
                        val country: VisitedCountryEntity =
                            document.toObject(VisitedCountryEntity::class.java)
                        if (country.id == city.parentId) {
                            val cityRef: DocumentReference = userDocRef
                                .collection(COLLECTION_CITIES)
                                .document("${city.name},${country.shortName}")
                            city.id = cityRef.id
                            cityRef
                                .set(city)
                                .addOnSuccessListener { onSuccess.invoke() }
                                .addOnFailureListener { exception -> onError.invoke(exception) }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun removeCityById(
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            usersRef.document(currentUser.uid)
                .collection(COLLECTION_CITIES).document(id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setVisitedCountries(
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countriesRef: CollectionReference = usersRef
                .document(currentUser.uid)
                .collection(COLLECTION_VISITED_COUNTRIES)
            countriesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (queryDocumentSnapshots.isEmpty) {
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
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setVisitedCountriesById(
        id: String,
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val countriesRef: CollectionReference = usersRef
            .document(id)
            .collection(COLLECTION_VISITED_COUNTRIES)
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
            .addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun setAllVisitedCities(
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val citiesRef: CollectionReference =
                usersRef.document(currentUser.uid).collection(COLLECTION_CITIES)
            citiesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (queryDocumentSnapshots.isEmpty) {
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
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setCities(
        userId: String,
        countryId: Int,
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val citiesRef: Query =
            usersRef.document(userId).collection(COLLECTION_CITIES)
                .whereEqualTo(KEY_PARENT_ID, countryId)
        citiesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.isEmpty) {
                    onSuccess(emptyList())
                } else {
                    val cities: MutableList<CityEntity> = mutableListOf()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val cityEntity: CityEntity =
                            documentSnapshot.toObject(CityEntity::class.java)
                        cities.add(cityEntity)
                    }
                    onSuccess(cities.sortedBy { city -> city.name })
                }
            }
            .addOnFailureListener { exception ->
                onError.invoke(exception)
            }
    }

    override suspend fun setCitiesByParentId(
        parentId: Int,
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val citiesRef: Query =
                usersRef.document(currentUser.uid).collection(COLLECTION_CITIES)
                    .whereEqualTo(KEY_PARENT_ID, parentId)
            citiesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (queryDocumentSnapshots.isEmpty) {
                        onSuccess(emptyList())
                    } else {
                        val cities: MutableList<CityEntity> = mutableListOf()
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val cityEntity: CityEntity = documentSnapshot.toObject(
                                CityEntity::class.java,
                            )
                            cities.add(cityEntity)
                        }
                        onSuccess(cities.sortedBy { city -> city.name })
                    }
                }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countriesRef: CollectionReference =
                usersRef.document(currentUser.uid).collection(COLLECTION_COUNTRIES)
            countriesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val countries: MutableList<CountryEntity> = mutableListOf()
                    if (queryDocumentSnapshots.size() == 0) {
// if there are no countries in local database, then there is no visited countries too
                        onSuccess(0, 0)
                    } else {
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val country: CountryEntity =
                                documentSnapshot.toObject(CountryEntity::class.java)
                            countries.add(country)
                            // check if it is the last document in list, filter and send success
                            if (documentSnapshot.id == queryDocumentSnapshots.last().id) {
                                val notVisitedCount: Int = countries.filter { countryEntity ->
                                    !countryEntity.isVisited
                                }.size
                                val visitedCount: Int = countries.filter { countryEntity ->
                                    countryEntity.isVisited
                                }.size
                                onSuccess(notVisitedCount, visitedCount)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countriesRef: Query = usersRef
                .document(currentUser.uid)
                .collection(COLLECTION_COUNTRIES)
                .orderBy(KEY_ID)
            // sorting countries by number given in [KEY_ID]
            countriesRef.startAt(from).endBefore(to).get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val countries: MutableList<CountryEntity> = mutableListOf()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: CountryEntity = documentSnapshot.toObject(
                            CountryEntity::class.java,
                        )
                        countries.add(country)
                    }
                    onSuccess(countries)
                }
                .addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setCountriesByName(
        nameQuery: String,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            val countriesRef: Query = usersRef.document(currentUser.uid)
                .collection(COLLECTION_COUNTRIES).orderBy(KEY_ID)
            countriesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val countries: MutableList<CountryEntity> = mutableListOf()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val country: CountryEntity =
                            documentSnapshot.toObject(CountryEntity::class.java)
                        if (country.name.startsWith(nameQuery, ignoreCase = true)) {
                            countries.add(country)
                        }
                        if (documentSnapshot == queryDocumentSnapshots.last()) {
                            onSuccess(countries)
                        }
                    }
                }.addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setTravellersByRange(
        to: Long,
        from: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val usersRef: CollectionReference = usersRef
        usersRef
            // getting only users who allowed to be visible
            .whereEqualTo(KEY_IS_VISIBLE, true)
            // getting part of the list for pagination
            .limit(to)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                val travellers: MutableList<TravellerEntity> = mutableListOf()
                for (i in from until queryDocumentSnapshots.size()) {
                    val snapshot: DocumentSnapshot = queryDocumentSnapshots.documents[i]
                    travellers.add(
                        TravellerEntity(
                            id = snapshot.id,
                            name = snapshot[KEY_NAME] as String,
                            avatar = snapshot[KEY_AVATAR] as String,
                            counter = snapshot[KEY_COUNTER] as Long,
                            isVisible = snapshot[KEY_IS_VISIBLE] as Boolean,
                        )
                    )
                }
                travellers.sortByDescending { traveller -> traveller.counter }
                onSuccess(travellers)
            }.addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun setTravellersByName(
        nameQuery: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val usersRef: Query = usersRef
        // getting only users who allowed to be visible
        usersRef.whereEqualTo(KEY_IS_VISIBLE, true)
            // getting part of the list for pagination
            .limit(requestedLoadSize)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val travellers: MutableList<TravellerEntity> = mutableListOf()
                for (i in requestedStartPosition until queryDocumentSnapshots.size()) {
                    val snapshot: DocumentSnapshot = queryDocumentSnapshots.documents[i]
                    val traveller = TravellerEntity(
                        id = snapshot.id,
                        name = snapshot[KEY_NAME] as String,
                        avatar = snapshot[KEY_AVATAR] as String,
                        counter = snapshot[KEY_COUNTER] as Long,
                        isVisible = snapshot[KEY_IS_VISIBLE] as Boolean,
                    )
                    if (traveller.name.startsWith(nameQuery, ignoreCase = true)) {
                        travellers.add(traveller)
                    }
                    if (snapshot == queryDocumentSnapshots.last()) {
                        travellers.sortBy { item -> item.name }
                        onSuccess(travellers)
                    }
                }
            }.addOnFailureListener { exception -> onError.invoke(exception) }
    }

    override suspend fun setTopTravellersPercent(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (currentUser != null) {
            // getting number of visited Countries by current user
            val visitedCountriesRef: CollectionReference =
                usersRef.document(currentUser.uid).collection(COLLECTION_VISITED_COUNTRIES)
            visitedCountriesRef.get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val travellerCounter: Int = queryDocumentSnapshots.size()
                    // getting total number of all users
                    val usersRef: Query = usersRef
                    usersRef.get().addOnSuccessListener { snapshots ->
                        val userCount: Int = snapshots.size()
                        //   getting number of users with bigger counter
                        usersRef.whereGreaterThan(KEY_COUNTER, travellerCounter).get()
                            .addOnSuccessListener { documents ->
                                val countOfTopTravellers: Int = documents.size()
                                /* getting percent of travellers who visited more countries
                                 * than current user */
                                onSuccess(countOfTopTravellers * 100 / userCount)
                            }.addOnFailureListener { exception -> onError.invoke(exception) }
                    }.addOnFailureListener { exception -> onError.invoke(exception) }
                }.addOnFailureListener { exception -> onError.invoke(exception) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }
}