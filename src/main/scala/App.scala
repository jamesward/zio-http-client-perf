import zio.*
import zio.Console.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.http.netty.client.NettyClientDriver

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

object App extends ZIOAppDefault:

  def run =
    val urlHttp = "http://google.com"
    val urlHttps = "https://google.com"

    def jdkReq(url: String, client: HttpClient) =
      val req = HttpRequest.newBuilder(URI(url)).build()
      client.send(req, HttpResponse.BodyHandlers.ofString()).body()

    {
      val startTime = java.lang.System.nanoTime()
      val client = HttpClient.newHttpClient()
      jdkReq(urlHttps, client)
      val endTime = java.lang.System.nanoTime()
      val ms = (endTime - startTime) / 1000000
      java.lang.System.out.println(s"1 https jdk client = ${ms}ms / req")
    }

    {
      val startTime = java.lang.System.nanoTime()
      val client = HttpClient.newHttpClient()
      (1 to 10).foreach {
        jdkReq(urlHttps, client)
      }
      val endTime = java.lang.System.nanoTime()
      val ms = (endTime - startTime) / 1000000
      java.lang.System.out.println(s"10 seq https jdk client = ${ms}ms / req")
    }

    def req(url: String) = for
      resp <- Client.request(url)
      _ <- resp.body.asString
    yield
      ()

    for
      oneHttp <- req(urlHttp).provide(Client.default).timed
      _ <- printLine(s"1 http = ${oneHttp._1.toMillis}ms / req")
      tenHttp <- req(urlHttp).repeatN(10).provide(Client.default).timed
      _ <- printLine(s"10 seq http = ${tenHttp._1.dividedBy(10).toMillis}ms / req")

      oneHttps <- req(urlHttps).provide(Client.default).timed
      _ <- printLine(s"1 https = ${oneHttps._1.toMillis}ms / req")
      tenHttps <- req(urlHttps).repeatN(10).provide(Client.default).timed
      _ <- printLine(s"10 seq https = ${tenHttps._1.dividedBy(10).toMillis}ms / req")

      clientLayer = (
        (
          DnsResolver.default ++
          (ZLayer.succeed(NettyConfig.default) >>> NettyClientDriver.live) ++
          ZLayer.succeed(Client.Config.default.withFixedConnectionPool(10))
        ) >>> Client.customized
      ).fresh

      onePool <- req(urlHttps).provide(clientLayer).timed
      _ <- printLine(s"1 https fixed pool = ${onePool._1.toMillis}ms / req")
      tenPool <- req(urlHttps).repeatN(10).provide(clientLayer).timed
      _ <- printLine(s"10 seq https fixed pool = ${tenPool._1.dividedBy(10).toMillis}ms / req")
    yield
      ()
