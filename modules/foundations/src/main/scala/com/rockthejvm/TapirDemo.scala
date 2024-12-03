package com.rockthejvm

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}

import scala.collection.mutable

object TapirDemo extends ZIOAppDefault {

  // Simplest endpoint
  val simplestEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("simple")
    .name("simple")
    .description("simplest endpoint possible")
    .get
    .in("simple")
    .out(plainBody[String])
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

  // Simulate a job board with an in-memory "database"
  val db: mutable.Map[Long, Job] = mutable.Map(
    1L -> Job(1L, "Instructor", "rockthejvm.com", "Rock the JVM")
  )

  // Create job endpoint
  val createEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("create")
    .description("Create a job")
    .in("jobs")
    .post
    .in(jsonBody[CreateJobRequest])
    .out(jsonBody[Job])
    .serverLogicSuccess(req => ZIO.succeed {
      val newId = db.keys.max + 1
      val newJob = Job(newId, req.title, req.url, req.company)
      db += (newId -> newJob)
      newJob
    })

  // Get job by ID endpoint
  val getByIdEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("getById")
    .description("Get a job by id")
    .in("jobs" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Job]])
    .serverLogicSuccess(id => ZIO.succeed(db.get(id)))

  // Get all jobs endpoint
  val getAllEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("getAll")
    .description("Get all jobs")
    .in("jobs")
    .get
    .out(jsonBody[List[Job]])
    .serverLogicSuccess(_ => ZIO.succeed(db.values.toList))

  val simpleServerProgram = Server.serve(
    ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(simplestEndpoint)
  )

  // Server program to serve the endpoints
  val serverProgram = Server.serve(
    ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(List(createEndpoint, getByIdEndpoint, getAllEndpoint))
  )

  // Run the server
  override def run: ZIO[Environment, Throwable, Unit] = serverProgram.provide(
    Server.default
  )
}

// Job model
case class Job(
                id: Long,
                title: String,
                url: String,
                company: String
              )

object Job {
  given codec: JsonCodec[Job] = DeriveJsonCodec.gen[Job] // Macro-based JSON codec
}

// Special request for the HTTP endpoint (Create job)
case class CreateJobRequest(title: String,
                            url: String,
                            company: String
                           )

object CreateJobRequest {
  given codec: JsonCodec[CreateJobRequest] = DeriveJsonCodec.gen[CreateJobRequest]
}
