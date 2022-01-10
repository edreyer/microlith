package io.liquidsoftware.base.user.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import io.liquidsoftware.base.user.application.port.`in`.RegisterUserCommand
import io.liquidsoftware.base.user.application.port.`in`.RegisterUserError.UserExistsError
import io.liquidsoftware.base.user.application.port.`in`.RoleDto
import io.liquidsoftware.base.user.application.port.`in`.UserDto
import io.liquidsoftware.base.user.application.port.`in`.UserRegisteredEvent
import io.liquidsoftware.common.types.ValidationException
import io.liquidsoftware.common.types.toErrStrings
import io.liquidsoftware.common.validation.Msisdn
import io.liquidsoftware.common.workflow.WorkflowDispatcher
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

// input DTO

data class RegisterUserInputDto(
  @NotEmpty @Msisdn val msisdn: String,
  @NotEmpty @Email val email: String,
  @NotEmpty val password: String,
  val role: RoleDto
)

sealed class RegisterUserOutputDto
data class RegisteredUserDto(val user: UserDto) : RegisterUserOutputDto()
data class RegisterUserErrorsDto(val errors: List<String>) : RegisterUserOutputDto()

@RestController
internal class RegisterUserController {

  @PostMapping("/user/register")
  suspend fun register(@Valid @RequestBody registerUser: RegisterUserInputDto)
    : ResponseEntity<RegisterUserOutputDto> =
    WorkflowDispatcher.dispatch<UserRegisteredEvent>(registerUser.toCommand())
      .fold(
        { ResponseEntity.ok(it.toOutputDto()) },
        {
          when (it) {
            is UserExistsError -> ResponseEntity.badRequest().body(it.toOutputDto())
            is ValidationException -> ResponseEntity.badRequest().body(it.toOutputDto())
            else -> ResponseEntity.status(500).body(it.toOutputDto())
          }
        }
      )

  suspend fun RegisterUserInputDto.toCommand(): RegisterUserCommand =
    RegisterUserCommand(
      msisdn = this.msisdn,
      email = this.email,
      password = this.password,
      role = this.role.name
    )

  fun UserExistsError.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto(listOf(this.error))

  fun ValidationException.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto(this.errors.toErrStrings())

  fun Throwable.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto(listOf("Server Error: ${this.message}"))

  fun UserRegisteredEvent.toOutputDto(): RegisterUserOutputDto = RegisteredUserDto(this.userDto)
}

