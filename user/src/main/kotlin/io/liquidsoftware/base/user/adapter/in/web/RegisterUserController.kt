package io.liquidsoftware.base.user.adapter.`in`.web

import io.liquidsoftware.base.user.application.port.`in`.RegisterUserCommand
import io.liquidsoftware.base.user.application.port.`in`.RoleDto
import io.liquidsoftware.base.user.application.port.`in`.UserDto
import io.liquidsoftware.base.user.application.port.`in`.UserExistsError
import io.liquidsoftware.base.user.application.port.`in`.UserRegisteredEvent
import io.liquidsoftware.common.validation.Msisdn
import io.liquidsoftware.common.workflow.WorkflowDispatcher
import io.liquidsoftware.common.workflow.WorkflowError
import io.liquidsoftware.common.workflow.WorkflowValidationError
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

// input DTO

data class RegisterUserInputDto(
  @NotEmpty @Msisdn val msisdn: String,
  @NotEmpty @Email val email: String,
  @NotEmpty val password: String,
  val role: RoleDto
)

sealed class RegisterUserOutputDto
data class RegisteredUserDto(val user: UserDto) : RegisterUserOutputDto()
data class RegisterUserErrorsDto(val errors: String) : RegisterUserOutputDto()

@RestController
internal class RegisterUserController(val dispatcher: WorkflowDispatcher) {

  @PostMapping("/user/register")
  suspend fun register(@Valid @RequestBody registerUser: RegisterUserInputDto)
    : ResponseEntity<RegisterUserOutputDto> {

    return dispatcher.dispatch<UserRegisteredEvent>(registerUser.toCommand())
      .fold(
        {
          when (it) {
            is UserExistsError -> ResponseEntity.badRequest().body(it.toOutputDto())
            is WorkflowValidationError -> ResponseEntity.badRequest().body(it.toOutputDto())
            else -> ResponseEntity.status(500).body(it.toOutputDto())
          }
        },
        { ResponseEntity.ok(it.toOutputDto()) }
      )
  }

  fun RegisterUserInputDto.toCommand(): RegisterUserCommand =
    RegisterUserCommand(
      msisdn = this.msisdn,
      email = this.email,
      password = this.password,
      role = this.role.name
    )

  fun UserExistsError.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto(this.message)

  fun WorkflowValidationError.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto(this.message)

  fun WorkflowError.toOutputDto(): RegisterUserOutputDto =
    RegisterUserErrorsDto("Server Error: $this")

  fun UserRegisteredEvent.toOutputDto(): RegisterUserOutputDto = RegisteredUserDto(this.userDto)
}

