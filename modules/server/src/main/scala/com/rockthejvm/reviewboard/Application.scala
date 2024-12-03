package com.rockthejvm.reviewboard


import com.rockthejvm.reviewboard.http.controllers.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server

object Application extends ZIOAppDefault {

  val serverProgram = for {
    controller <- HealthController.makeZIO
    server <- Server.serve(
      ZioHttpInterpreter(ZioHttpServerOptions.default)
        .toHttp(controller.health)
    )
    _ <- Console.printLine("Rock the JVM!")
  } yield ()

  override def run = serverProgram.provide(
    Server.default
  )

}
