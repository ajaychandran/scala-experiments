package mpsc

import mpsc.QueueFactory
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@State(Scope.Thread)
@OperationsPerInvocation(1)
@Fork(2)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class EnqueueBenchmark extends QueueBenchmarkConfig {

  @Param(
    Array(
      "ConcurrentLinkedQueue",
      "Jiffy",
      "JiffyAligned",
      "Vyukov"
    )
  )
  var name: String = _

  var q: Queue[AnyRef] = _

  def element(): AnyRef =
    new Object()

  @Setup(Level.Iteration)
  def create(): Unit = {
    q = QueueFactory(name, step, grow)
  }

  @Benchmark
  def enqueue(): Int = {
    val n = opi
    var i = 0
    while (i < n) {
      val a = element()
      q.add(a)
      i += 1
    }
    i
  }
}
