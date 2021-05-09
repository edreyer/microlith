package ventures.dvx.user

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import ventures.dvx.base.user.adapter.`in`.web.RegisterUserInputDto
import ventures.dvx.base.user.adapter.`in`.web.RegisteredUserDto

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UseRegistrationTest {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @LocalServerPort
  lateinit var port: Integer

  private lateinit var testUser: RegisterUserInputDto

  @BeforeAll
  fun initUser() {
    testUser = RegisterUserInputDto(
      username = "foo",
      email = "foo@bar.com",
      password = "password"
    )
  }

  @Test
  fun `register new user`() {
    val regReq = testUser
    val expected = RegisteredUserDto(
      username = testUser.username,
      email = testUser.email
    )
    val actual = post("/auth/register", regReq)
      .then()
      .statusCode(200)
      .extract().`as`(RegisteredUserDto::class.java)
    assertThat(actual).isEqualTo(expected)
  }

  private fun get(path: String, token: String? = null): Response {
    var spec = given().baseUri("http://localhost:${port}")
    if (token != null) {
      spec = spec.header("Authorization", "Bearer ${token}")
    }
    return spec.get(path)
  }

  private fun post(path: String, body: Any): Response {
    return given().baseUri("http://localhost:${port}").contentType(ContentType.JSON).body(body).post(path)
  }

  private fun asJson(payload: Any) : String = objectMapper.writeValueAsString(payload)

}
