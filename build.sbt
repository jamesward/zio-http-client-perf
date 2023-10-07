name := "zio-http-client-perf"

scalaVersion := "3.3.1"

val zioVersion = "2.0.18"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio"                % zioVersion,
  "dev.zio" %% "zio-http"           % "3.0.0-RC2",
)

fork := true
