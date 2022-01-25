package io.liquidsoftware.base.user.application.workflows

import io.liquidsoftware.base.user.application.port.`in`.RoleDto
import io.liquidsoftware.base.user.application.port.`in`.SystemFindUserByEmailQuery
import io.liquidsoftware.base.user.application.port.`in`.SystemUserFoundEvent
import io.liquidsoftware.base.user.application.port.`in`.UserNotFoundError
import io.liquidsoftware.base.user.application.port.out.FindUserPort
import io.liquidsoftware.base.user.domain.AdminUser
import io.liquidsoftware.base.user.domain.DisabledUser
import io.liquidsoftware.base.user.domain.User
import io.liquidsoftware.common.security.UserDetailsWithId
import io.liquidsoftware.common.workflow.BaseSafeWorkflow
import io.liquidsoftware.common.workflow.WorkflowDispatcher
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
internal class SystemFindUserByEmailWorkflow(
  private val findUserPort: FindUserPort
) : BaseSafeWorkflow<SystemFindUserByEmailQuery, SystemUserFoundEvent>() {

  @PostConstruct
  fun registerWithDispatcher() = WorkflowDispatcher.registerQueryHandler(this)

  override suspend fun execute(request: SystemFindUserByEmailQuery): SystemUserFoundEvent =
    findUserPort.findUserByEmail(request.email)
      ?.let { SystemUserFoundEvent(it.toUserDetails()) }
      ?: throw UserNotFoundError(request.email)

  private fun User.toUserDetails(): UserDetailsWithId {
    val roles = when (this) {
      is AdminUser -> listOf(RoleDto.ROLE_ADMIN)
      else -> listOf(RoleDto.ROLE_USER)
    }
    return when (this) {
      is DisabledUser -> UserDetailsWithId(
        id = this.id.value,
        user = org.springframework.security.core.userdetails.User(
          this.email.value,
          this.encryptedPassword.value,
          false, false, false, false,
          listOf()
        )
      )
      else -> UserDetailsWithId(
        id = this.id.value,
        user = org.springframework.security.core.userdetails.User(
          this.email.value,
          this.encryptedPassword.value,
          true, true, true, true,
          roles.map { SimpleGrantedAuthority(it.name) }
        )
      )
    }
  }

}

