package fun

import sttp.tapir.ztapir.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.openapi.Info
import zhttp.http.HttpApp
import zhttp.service.Server
import zio.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object HelloWorldZioHttpServer extends App:

  // a simple string-only endpoint
  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get
      .in("hello")
      .in(path[String]("name"))
      .out(stringBody)

  case class AddResult(x: Int, y: Int, result: Int)

  object AddResult:

    implicit val decoder: JsonDecoder[AddResult] =
      DeriveJsonDecoder.gen[AddResult]

    implicit val encoder: JsonEncoder[AddResult] =
      DeriveJsonEncoder.gen[AddResult]

  // an endpoint which responds which json, using zio-json
  val add: PublicEndpoint[(Int, Int), Unit, AddResult, Any] =
    endpoint.get
      .in("add")
      .in(path[Int]("x"))
      .in(path[Int]("y"))
      .out(jsonBody[AddResult])

  // swagger FTW!
  val swaggerEndpoints = SwaggerInterpreter().fromEndpoints[Task](
    List(helloWorld, add),
    Info(
      title = "Russ White's Silly API",
      version = "0.0.1",
      description = Some("Everything required to be polite and add two numbers")
    )
  )

  // converting the endpoint descriptions to the Http type
  val app: HttpApp[Any, Throwable] =
    ZioHttpInterpreter().toHttp(
      helloWorld.zServerLogic(name => ZIO.succeed(s"Hello, $name!"))
    ) +++
      ZioHttpInterpreter().toHttp(add.zServerLogic { case (x, y) =>
        ZIO.succeed(AddResult(x, y, x + y))
      }) +++
      ZioHttpInterpreter().toHttp(swaggerEndpoints)

  // starting the server
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, app).exitCode

end HelloWorldZioHttpServer
