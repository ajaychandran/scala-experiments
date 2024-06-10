import $ivy.`com.lihaoyi::mill-contrib-jmh:`
import mill._
import mill.contrib.jmh.JmhModule
import mill.scalalib._
import mill.scalalib.scalafmt.ScalafmtModule

object `zio-8807` extends Zio8807
object `zio-8861` extends Zio8861

trait Zio8807 extends BaseScalaModule {

  object jmh extends ScalaJmhModule {
    override def ivyDeps =
      super.ivyDeps() ++ Agg(Deps.`jct-tools`, Deps.`jol-core`)
  }

  object test extends ScalaTestModule
}

trait Zio8861 extends BaseScalaModule {

  override def ivyDeps = Agg(Deps.`zio`)

  object jmh extends ScalaJmhModule

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

  trait ScalaJmhModule extends ScalaModule with ScalafmtModule {

    def jmhCompile = T {
      val dest = T.ctx().dest

      os.remove(dest)
      os.makeDir(dest)

      val cmd = util.Jvm.jdkTool("javac")
      val cp =
        super.runClasspath().map(_.path).mkString(java.io.File.pathSeparator)
      val (sources, resources) = jmhGenerate()
      val files =
        os.walk(sources).collect { case s if s.ext == "java" => s.toString }

      os.proc(cmd, files, "-cp", cp, "-d", dest).call(dest)

      Agg(PathRef(dest), PathRef(resources))
    }

    def jmhGenerate = T {
      val dest = T.ctx().dest
      val sources = dest / "src"
      val resources = dest / "resources"

      os.remove(sources)
      os.remove(resources)
      os.makeDir(sources)
      os.makeDir(resources)

      util.Jvm.runSubprocess(
        "org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator",
        (super.runClasspath() ++ jmhGeneratorDeps()).map(_.path),
        mainArgs = Seq(
          compile().classes.path.toString,
          sources.toString,
          resources.toString,
          jmhGenerator()
        )
      )
      (sources, resources)
    }

    def jmhGenerator = T { "default" }

    def jmhGeneratorDeps = T {
      resolveDeps(
        T.task {
          Agg(ivy"org.openjdk.jmh:jmh-generator-bytecode:${jmhVersion()}")
            .map(bindDependency())
        }
      )()
    }

    def jmhVersion = T { "1.37" }

    override def ivyDeps =
      super.ivyDeps() ++ Agg(ivy"org.openjdk.jmh:jmh-core:${jmhVersion()}")
    override def mainClass =
      Some("org.openjdk.jmh.Main")
    override def runClasspath =
      super.runClasspath() ++ jmhCompile()
    override def upstreamAssemblyClasspath =
      super.upstreamAssemblyClasspath() ++ jmhCompile()

    override def mandatoryScalacOptions = outer.mandatoryScalacOptions()
    override def moduleDeps = Seq(outer)
    override def scalaOrganization = outer.scalaOrganization()
    override def scalaVersion = outer.scalaVersion()
    override def scalacPluginIvyDeps = outer.scalacPluginIvyDeps()
    override def scalacPluginClasspath = outer.scalacPluginClasspath()
    override def scalacOptions = outer.scalacOptions()
  }
}

object Deps {

  val `jol-core` = ivy"org.openjdk.jol:jol-core:0.17"

  val `jct-tools` = ivy"org.jctools:jctools-core:4.0.3"

  val `zio` = ivy"dev.zio::zio:2.1.1"
  val `zio-test-sbt` = ivy"dev.zio::zio-test-sbt:2.1.1"
}
