import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, EventLoopGroup, SimpleChannelInboundHandler}
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpClientCodec, HttpHeaderNames, HttpHeaderValues, HttpMethod, HttpObject, HttpObjectAggregator, HttpVersion}
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import zio.*
import zio.Console.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.http.netty.client.NettyClientDriver

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}


val urlHttp = "http://google.com"
val urlHttps = "https://google.com"

def timed(name: String, num: Int)(f: => Any) =
  val startTime = java.lang.System.nanoTime()
  f
  val endTime = java.lang.System.nanoTime()
  val ms = (endTime - startTime) / 1_000_000 / num
  java.lang.System.out.println(s"$num $name = ${ms}ms / req")


def curlReq(url: String) =
  import sys.process._
  s"curl --silent --output /dev/null $url".!

def jdkReq(url: String, client: HttpClient): String =
  val req = HttpRequest.newBuilder(URI(url)).build()
  client.send(req, HttpResponse.BodyHandlers.ofString()).body()


class HttpClientHandler extends SimpleChannelInboundHandler[HttpObject]:
  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit =
    val response = msg.asInstanceOf[io.netty.handler.codec.http.HttpResponse]
    //println(response.status())
    // todo: read body?
    ()


class HttpClientInitializer extends ChannelInitializer[SocketChannel]:
  val sslCtx = SslContextBuilder.forClient.trustManager(InsecureTrustManagerFactory.INSTANCE).build

  override def initChannel(ch: SocketChannel): Unit =
    val p = ch.pipeline()
    p.addLast(sslCtx.newHandler(ch.alloc()))
    p.addLast(new HttpClientCodec())
    p.addLast(new HttpObjectAggregator(1048576))
    p.addLast(new HttpClientHandler())


// just https for now
def nettyReq(url: String, eventLoopGroup: EventLoopGroup, channel: io.netty.channel.Channel) =
  val uri = new URI(url)
  val b = new Bootstrap()
  b.group(eventLoopGroup)
    .channel(channel.getClass)
    .handler(new HttpClientInitializer())

  val ch = b.connect(uri.getHost, 443).sync().channel()
  val req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "", Unpooled.EMPTY_BUFFER)
  req.headers().set(HttpHeaderNames.HOST, uri.getHost)
  req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
  ch.writeAndFlush(req)
  ch.closeFuture().sync()


def zioReq(url: String) = for
  resp <- Client.request(url)
  _ <- resp.body.asString
yield
  ()

object App extends ZIOAppDefault:

  def run =
    timed("https curl client", 1) {
      curlReq(urlHttps)
    }

    timed("https curl client", 10) {
      (1 to 10).foreach { _ =>
        curlReq(urlHttps)
      }
    }

    timed("https jdk client", 1) {
      val client = HttpClient.newHttpClient()
      jdkReq(urlHttps, client)
    }

    timed("https jdk client", 10) {
      val client = HttpClient.newHttpClient()
      (1 to 10).foreach { _ =>
        jdkReq(urlHttps, client)
      }
    }

    timed("https netty epoll client", 1) {
      val eventLoopGroup = new EpollEventLoopGroup(16)
      val channel = EpollSocketChannel()
      nettyReq(urlHttps, eventLoopGroup, channel)
      eventLoopGroup.shutdownGracefully()
    }

    timed("https netty epoll client", 10) {
      val eventLoopGroup = new EpollEventLoopGroup(16)
      val channel = EpollSocketChannel()
      (1 to 10).foreach { _ =>
        nettyReq(urlHttps, eventLoopGroup, channel)
      }
      eventLoopGroup.shutdownGracefully()
    }

    timed("https netty nio client", 1) {
      val eventLoopGroup = new NioEventLoopGroup(16)
      val channel = new NioSocketChannel()
      nettyReq(urlHttps, eventLoopGroup, channel)
      eventLoopGroup.shutdownGracefully()
    }

    timed("https netty nio client", 10) {
      val eventLoopGroup = new NioEventLoopGroup(16)
      val channel = new NioSocketChannel()
      (1 to 10).foreach { _ =>
        nettyReq(urlHttps, eventLoopGroup, channel)
      }
      eventLoopGroup.shutdownGracefully()
    }

    for
      oneHttp <- zioReq(urlHttp).provide(Client.default).timed
      _ <- printLine(s"1 http zio = ${oneHttp._1.toMillis}ms / req")
      tenHttp <- zioReq(urlHttp).repeatN(10).provide(Client.default).timed
      _ <- printLine(s"10 http zio = ${tenHttp._1.dividedBy(10).toMillis}ms / req")

      oneHttps <- zioReq(urlHttps).provide(Client.default).timed
      _ <- printLine(s"1 https zio = ${oneHttps._1.toMillis}ms / req")
      tenHttps <- zioReq(urlHttps).repeatN(10).provide(Client.default).timed
      _ <- printLine(s"10 https zio = ${tenHttps._1.dividedBy(10).toMillis}ms / req")

      clientLayer = (
        (
          DnsResolver.default ++
          (ZLayer.succeed(NettyConfig.default) >>> NettyClientDriver.live) ++
          ZLayer.succeed(Client.Config.default.withFixedConnectionPool(10))
        ) >>> Client.customized
      ).fresh

      onePool <- zioReq(urlHttps).provide(clientLayer).timed
      _ <- printLine(s"1 https zio fixed pool = ${onePool._1.toMillis}ms / req")
      tenPool <- zioReq(urlHttps).repeatN(10).provide(clientLayer).timed
      _ <- printLine(s"10 https zio fixed pool = ${tenPool._1.dividedBy(10).toMillis}ms / req")
    yield
      ()
