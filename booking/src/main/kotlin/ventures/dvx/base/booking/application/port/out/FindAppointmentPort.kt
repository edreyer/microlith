package ventures.dvx.base.booking.application.port.out

import ventures.dvx.base.booking.domain.Appointment
import java.time.LocalDate

internal interface FindAppointmentPort {
  suspend fun findById(apptId: String): Appointment?
  suspend fun findByUserId(userId: String): List<Appointment>
  suspend fun findAll(date: LocalDate): List<Appointment>
}
