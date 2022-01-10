package io.liquidsoftware.base.user.adapter.`in`.web.api.v1

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import io.liquidsoftware.base.user.application.port.`in`.DisableUserCommand
import io.liquidsoftware.base.user.application.port.`in`.EnableUserCommand
import io.liquidsoftware.base.user.application.port.`in`.FindUserByEmailQuery
import io.liquidsoftware.base.user.application.port.`in`.FindUserByIdQuery
import io.liquidsoftware.base.user.application.port.`in`.FindUserByMsisdnQuery
import io.liquidsoftware.base.user.application.port.`in`.UserDisabledEvent
import io.liquidsoftware.base.user.application.port.`in`.UserDto
import io.liquidsoftware.base.user.application.port.`in`.UserEnabledEvent
import io.liquidsoftware.base.user.application.port.`in`.UserFoundEvent
import io.liquidsoftware.base.user.application.port.`in`.UserNotFoundError
import io.liquidsoftware.common.workflow.Query
import io.liquidsoftware.common.workflow.WorkflowDispatcher
import javax.validation.Valid
import javax.validation.constraints.NotBlank

sealed class FindUserOutputDto
data class FoundUserUserOutputDto(val userDto: UserDto) : FindUserOutputDto()
data class RegisterUserErrorsDto(val errors: List<String>) : FindUserOutputDto()

@RestController
internal class UserV1Controller {

  private suspend inline fun dispatchQuery(query: Query, msgOnError: String) : ResponseEntity<FindUserOutputDto> {
    val event: Result<UserFoundEvent> = WorkflowDispatcher.dispatch(query)
    return event
      .fold(
        { ResponseEntity.ok(FoundUserUserOutputDto(it.userDto)) },
        {
          when (it) {
            is UserNotFoundError -> ResponseEntity.badRequest().body(RegisterUserErrorsDto(listOf(msgOnError)))
            else -> ResponseEntity.badRequest().body(RegisterUserErrorsDto(listOf("User not found: ${it.message}")))
          }
        }
      )
  }

  @GetMapping(path = [V1UserPaths.USER_BY_ID])
  suspend fun getUser(@PathVariable @Valid @NotBlank userId: String) : ResponseEntity<FindUserOutputDto> =
    dispatchQuery(FindUserByIdQuery(userId), "User not found with ID $userId")

  @GetMapping(path = [V1UserPaths.USER_BY_EMAIL])
  suspend fun getUserByEmail(@PathVariable @Valid @NotBlank email: String) : ResponseEntity<FindUserOutputDto> =
    dispatchQuery(FindUserByEmailQuery(email), "User not found with email $email")

  @GetMapping(path = [V1UserPaths.USER_BY_MSISDN])
  suspend fun getUserByMsisdn(@PathVariable @Valid @NotBlank msisdn: String) : ResponseEntity<FindUserOutputDto> =
    dispatchQuery(FindUserByMsisdnQuery(msisdn), "User not found with msisdn $msisdn")

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = [V1UserPaths.ENABLE_USER])
  suspend fun enableUser(@PathVariable @Valid @NotBlank userId: String) : ResponseEntity<String> =
    WorkflowDispatcher.dispatch<UserEnabledEvent>(EnableUserCommand(userId))
      .fold(
        { ResponseEntity.ok("OK")},
        { ex -> throw ex }
      )

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = [V1UserPaths.DISABLE_USER])
  suspend fun disableUser(@PathVariable @Valid @NotBlank userId: String) : ResponseEntity<String> =
    WorkflowDispatcher.dispatch<UserDisabledEvent>(DisableUserCommand(userId))
      .fold(
        { ResponseEntity.ok("OK")},
        { ex -> throw ex }
      )

}
