package io.liquidsoftware.base.user.application.port.out

import io.liquidsoftware.base.user.domain.User

internal interface FindUserPort {
  suspend fun findUserById(userId: String): User?
  suspend fun findUserByEmail(email: String): User?
  suspend fun findUserByMsisdn(msisdn: String): User?
}
