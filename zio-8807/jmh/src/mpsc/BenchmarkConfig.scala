package mpsc

trait BenchmarkConfig {

  val opi = Option(Integer.getInteger("opi")).fold(1)(_.intValue())
}
