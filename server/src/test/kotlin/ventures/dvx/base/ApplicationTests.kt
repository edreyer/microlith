package ventures.dvx.base

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ventures.dvx.base.server.DvxApplication

@SpringBootTest(
  classes = [DvxApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApplicationTests {

  @Test
  fun contextLoads() {}

}
