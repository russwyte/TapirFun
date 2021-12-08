ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.0"

val tapirVersion = "0.19.0"

def tapirDep(name: String): ModuleID =
  "com.softwaremill.sttp.tapir" %% name % tapirVersion

val tapirLibs = Seq(
  "tapir-core",
  "tapir-zio-http-server",
  "tapir-json-zio",
  "tapir-swagger-ui-bundle"
) map tapirDep

lazy val root = (project in file("."))
  .settings(
    name := "TapirFun",
    libraryDependencies ++= tapirLibs
  )
