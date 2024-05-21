package mpsc

import mpsc.QueueFactory
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@State(Scope.Group)
@OperationsPerInvocation(1)
@Fork(2)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class QueueBenchmark extends QueueBenchmarkConfig {

  @Param(
    Array(
      "ConcurrentLinkedQueue",
      "Jiffy",
      "JiffyAligned",
      "MpscLinkedQueue",
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

  def dequeue(n: Int): Int = {
    var i = 0
    while (i < n) {
      q.add(element())
      while (null == q.poll()) {}
      i += 1
    }
    i
  }

  def enqueue(n: Int): Int = {
    var i = 0
    while (i < n) {
      val a = element()
      q.add(a)
      i += 1
    }
    i
  }

  @Benchmark
  @Group("p01")
  @GroupThreads(1)
  def dequeue_01() = dequeue(opi)

  @Benchmark
  @Group("p01")
  @GroupThreads(1)
  def enqueue_01() = enqueue(opi)

  // @Benchmark
  // @Group("p02")
  // @GroupThreads(1)
  // def dequeue_02() = dequeue(opi)

  // @Benchmark
  // @Group("p02")
  // @GroupThreads(2)
  // def enqueue_02() = enqueue(opi)
}
