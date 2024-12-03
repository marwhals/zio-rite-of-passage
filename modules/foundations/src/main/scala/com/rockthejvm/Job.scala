package com.rockthejvm

import zio.json.{DeriveJsonCodec, JsonCodec}

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
