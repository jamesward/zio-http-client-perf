name := "zio-http-client-perf"

scalaVersion := "3.2.2"

val zioVersion = "2.0.13"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio"                % zioVersion,
  "dev.zio" %% "zio-http"           % "3.0.0-RC1",
)

fork := true
