import sbt.Keys._
import sbt.Project.projectToRef

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedDependencies.value
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

lazy val macros: Project = (project in file("macros"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.macrosDependencies.value
  )

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.scalajsDependencies.value,
    // by default we do development build, no eliding
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    persistLauncher := true,
    persistLauncher in Test := false,
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJS)
  .dependsOn(macros)


// Client projects (just one in this case)
lazy val clients = Seq(client)

// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
    commands += ReleaseCmd,
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    // compress CSS
    LessKeys.compress in Assets := true
  )
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)


lazy val deploy = TaskKey[Unit]("deploy", "Copy files into dist directory")

lazy val root = (project in file(".")).settings(
  deploy := {
    val clientTarget = (crossTarget in client).value
    val clientProjectName = (name in client).value
    val mainJsSource = clientTarget / (clientProjectName + "-opt.js")
    val depsJsSource = clientTarget / (clientProjectName + "-jsdeps.min.js")
    //val cssSource = WebKeys.public.value


    println(mainJsSource)
    println(depsJsSource)
    //println(cssSource)

    IO.copyFile(file("server/target/web/public/main/stylesheets/main.min.css"),
      file("dist/stylesheets/main.min.css"), false)
    IO.copyFile(mainJsSource, file("dist/scripts/main.js"))
    IO.copyFile(depsJsSource, file("dist/scripts/deps.js"))
  }

)


lazy val DevServerCmd = Command.args("devServer", "<port>") { case (state, args) =>
  val cmd = ("server/run" +: args).mkString(" ")
  cmd :: state
}

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    //"production/clean" ::
    "client/clean" ::
    "server/clean" ::
    "client/fullOptJS" ::
    "server/assets" ::
    "deploy" ::
    "set elideOptions in client := Seq()" ::
    state
}

commands ++= Seq(DevServerCmd, ReleaseCmd)

