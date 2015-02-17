
lazy val commonSettings = Seq(
  scalaVersion := "2.11.5",

  // cached resolution appears to conflict with ctags generation for dependencies
  // updateOptions := updateOptions.value.withCachedResolution(true),

  // http://tpolecat.github.io/2014/04/11/scalac-flags.html
  scalacOptions in (Compile, compile) ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"),

    // TODO : running into an inferred any near forAll in tests
    wartremoverErrors in (Compile, compile) ++= Seq(Wart.Any, Wart.Serializable),

    resolvers += "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases",

    addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1.7"))

lazy val rootSettings = Seq(
  testOptions in Test += Tests.Argument("-verbosity", "1"),

  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.1.0-RC1",
    "org.scalaz" %% "scalaz-core" % "7.1.1",
    "org.spire-math" %% "spire" % "0.9.1",
    "org.scalacheck" %% "scalacheck" % "1.12.2",
    "com.github.alexarchambault" %% "scalacheck-shapeless" % "1.12.1"))

lazy val root = Project("ndim-rtree-exploration", file("."))
  .settings(commonSettings:_*)
  .settings(rootSettings:_*)
  .settings(tutSettings:_*)
  .settings(tutSourceDirectory := file("slides/tut"))

lazy val benchmarkSettings = Seq(
  fork in run := true,

  javaOptions in run += "-Xmx4G",

  resolvers += "bintray/meetup" at "http://dl.bintray.com/meetup/maven",

  libraryDependencies ++= Seq(
    "com.meetup" % "archery_2.11" % "0.3.0",
    "ichi.bench" % "thyme" % "0.1.1" from "http://plastic-idolatry.com/jars/thyme-0.1.1.jar"))

lazy val benchmark = (project in file("benchmark"))
  .dependsOn(root)
  .settings(commonSettings:_*)
  .settings(benchmarkSettings:_*)
