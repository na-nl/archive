import Dependencies._

lazy val commonSettings = Seq(
  version      := "0.1.0",
  scalaVersion := ScalaLanguageVersion
)

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val core = (project in file("na-core"))
  .settings(
    commonSettings,
    name := "na-core",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val server = (project in file("na-server"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name := "na-server",
    libraryDependencies ++= platformDependencies
  )

lazy val integration = (project in file("na-it"))
  .dependsOn(server)
  .settings(
    commonSettings,
    name := "na-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
