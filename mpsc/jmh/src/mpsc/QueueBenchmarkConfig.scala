package mpsc

trait QueueBenchmarkConfig {

  val opi = Option(Integer.getInteger("opi")).fold(1)(_.intValue())
  val step = Option(Integer.getInteger("step")).fold(8)(_.intValue())
  val grow = Option(Integer.getInteger("grow")).fold(step / 2)(_.intValue())
}
