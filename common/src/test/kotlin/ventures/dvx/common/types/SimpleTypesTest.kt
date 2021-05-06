package ventures.dvx.common.types

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ventures.dvx.common.types.EmailAddress
import ventures.dvx.common.types.Msisdn
import ventures.dvx.common.types.NonEmptyString
import ventures.dvx.common.types.PostalCode


class SimpleTypesTest {

  @Test
  fun `Test NonEmptyString`() {
    assertTrue(NonEmptyString.of("good").isValid)
    assertTrue(NonEmptyString.of("").isInvalid)
  }

  @Test
  fun `Test EmailAddress`() {
    assertTrue(EmailAddress.of("erik@curbee.com").isValid)
    assertTrue(EmailAddress.of("erik@curbee").isInvalid)
    assertTrue(EmailAddress.of("").isInvalid)
  }

  @Test
  fun `Test PostalCode`() {
    assertTrue(PostalCode.of("12345").isValid)
    assertTrue(PostalCode.of("123").isInvalid)
    assertTrue(PostalCode.of("").isInvalid)
  }

  @Test
  fun `Test Msisdn`() {
    assertTrue(Msisdn.of("+15125551212").isValid)
    assertTrue(Msisdn.of("5125551212").isInvalid)
    assertTrue(Msisdn.of("5551212").isInvalid)
  }

}
