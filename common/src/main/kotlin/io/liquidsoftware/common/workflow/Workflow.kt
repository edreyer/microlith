package io.liquidsoftware.common.workflow

import arrow.core.computations.result
import io.liquidsoftware.common.ext.className
import io.liquidsoftware.common.logging.LoggerDelegate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

// Workflow Input type
sealed interface Request
interface Command : Request
interface Query : Request

// Workflow Output type
abstract class Event {
  val eventId: UUID = UUID.randomUUID()
  val instant: Instant = Instant.now()
}

interface Workflow<E : Event>
interface SafeWorkflow<R: Request, E : Event> : Workflow<E> {
  suspend fun invoke(request: R): Result<E>
}
interface SecuredWorkflow<R: Request, E : Event> : Secured<R>

abstract class BaseSafeWorkflow<R: Request, E : Event> : SafeWorkflow<R, E> {
  private val log by LoggerDelegate()
  final override suspend operator fun invoke(request: R): Result<E> =
    result {
      log.debug("Executing workflow ${this.className()} with request $request")
      execute(request)
    }
  abstract suspend fun execute(request: R): E
}

interface MonoWorkflow<R: Request, E : Event> : Workflow<E> {
  suspend operator fun invoke(request: R): Mono<E> {
    return execute(request)
  }
  suspend fun execute(request: R): Mono<E>
}

interface FluxWorkflow<R: Request, E : Event> : Workflow<E> {
  suspend operator fun invoke(request: R): Flux<E> {
    return execute(request)
  }
  suspend fun execute(request: R): Flux<E>
}


