package mpsc

import mpsc.QueueFactory
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.SingleShotTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Group)
@Measurement(batchSize = 100000, iterations = 10)
@Warmup(batchSize = 100000, iterations = 10)
class QueueBenchmark {

  @Param(
    Array(
      "ConcurrentLinkedQueue",
      "MpscLinkedQueue",
      "custom.Basic",
      "custom.Padded128",
      "custom.Padded64",
      "jiffy.Basic(4)",
      "jiffy.Padded128(4)",
      "jiffy.Padded64(4)",
      "jiffy.Basic(16)",
      "jiffy.Padded128(16)",
      "jiffy.Padded64(16)",
      "vyukov.Basic",
      "vyukov.Padded128",
      "vyukov.Padded64"
    )
  )
  var queue: String = _

  var q: Queue[AnyRef] = _

  def element(): AnyRef =
    new Object()

  @Setup(Level.Iteration)
  def setup(): Unit =
    q = QueueFactory(queue)

  def add() = {
    val a = new Object()
    q.add(a)
    a
  }

  def poll(n: Int) = {
    var i = 0
    while (i < n) {
      while (null == q.poll()) {}
      i += 1
    }
    i
  }

  @Benchmark
  @Group("p01")
  @GroupThreads(1)
  def add_01() =
    add()

  @Benchmark
  @Group("p01")
  @GroupThreads(1)
  def poll_01() =
    poll(1)

  @Benchmark
  @Group("p02")
  @GroupThreads(2)
  def add_02() =
    add()

  @Benchmark
  @Group("p02")
  @GroupThreads(1)
  def poll_02() =
    poll(2)

  @Benchmark
  @Group("p03")
  @GroupThreads(3)
  def add_03() =
    add()

  @Benchmark
  @Group("p03")
  @GroupThreads(1)
  def poll_03() =
    poll(3)

  @Benchmark
  @Group("p07")
  @GroupThreads(7)
  def add_07() =
    add()

  @Benchmark
  @Group("p07")
  @GroupThreads(1)
  def poll_07() =
    poll(7)
}
