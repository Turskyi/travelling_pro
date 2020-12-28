package io.github.turskyi.data.util.exceptions

class NetworkErrorException(override val message: String = "network error occurred") :
    Exception(message)