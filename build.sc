import $ivy.`com.lihaoyi::mill-contrib-jmh:`
import mill._
import mill.contrib.jmh.JmhModule
import mill.scalalib._
import mill.scalalib.scalafmt.ScalafmtModule

object `mpsc` extends Mpsc

trait Mpsc extends BaseScalaModule {

  object jmh extends ScalaJmhModule {
    override def ivyDeps =
      super.ivyDeps() ++ Agg(Deps.`jct-tools`, Deps.`jol-core`)
  }

  object test extends ScalaTestModule
}

trait BaseScalaModule extends ScalaModule with ScalafmtModule {
  outer =>

  def scalaVersion = "2.13.13"
  def scalacOptions =
    Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )

  trait ScalaTestModule
      extends ScalaTests
      with TestModule.ZioTest
      with ScalafmtModule {
    def ivyDeps = Agg(Deps.`zio-test-sbt`)
  }

  trait ScalaJmhModule extends JmhModule with ScalaModule with ScalafmtModule {

    def jmhCoreVersion = "1.37"

    override def defaultCommandName() = "runJmh"
    override def moduleDeps = Seq(outer)

    override def scalaOrganization = outer.scalaOrganization()
    override def scalaVersion = outer.scalaVersion()
    override def scalacPluginIvyDeps = outer.scalacPluginIvyDeps()
    override def scalacPluginClasspath = outer.scalacPluginClasspath()
    override def scalacOptions = outer.scalacOptions()
    override def mandatoryScalacOptions = outer.mandatoryScalacOptions()
  }
}

object Deps {

  val `jol-core` = ivy"org.openjdk.jol:jol-core:0.17"

  val `jct-tools` = ivy"org.jctools:jctools-core:4.0.3"

  val `zio-test-sbt` = ivy"dev.zio::zio-test-sbt:2.0.21"
}
