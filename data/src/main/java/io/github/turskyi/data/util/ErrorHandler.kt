package io.github.turskyi.data.util

import io.github.turskyi.data.util.exceptions.*
import java.net.UnknownHostException

/**
 * @param message function returns message if unknown code was detected
 */
fun Int.throwException(message: String): Exception {
    return when (this) {
        429 -> RequestsLimitException()
        401 -> UnauthorizedException()
        400 -> BadRequestException()
        404 -> NotFoundException()
        403 -> NotValidException()
        409 -> ConflictException()
        else -> if (this >= 500) {
            UnknownHostException()
        } else {
            NetworkErrorException(message)
        }
    }
}