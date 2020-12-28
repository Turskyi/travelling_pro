package io.github.turskyi.domain.model

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T? = null, val responseCode: Int? = null) :
        Result<T>()

    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}