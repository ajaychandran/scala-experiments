package mpsc

import mpsc.QueueFactory
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

/** Defines (asymetric) benchmarks to measure the relative performance of MPSC
  * queues in the presence of concurrent operations. The benchmarks are
  * organized into groups where each group consists of one or more producer
  * threads that enqueue an element and a consumer thread that dequeues all
  * elements.
  *
  * The total number of elements transiting the queue in an iteration is
  * `number_of_producers * batch_size * operations_per_invocation`.
  *
  * In an ideal system, that scales linearly with the number of threads, the
  * measurement for all groups is expected to be the same.
  *
  * A `SingleShotTime` mode with a `batchSize` is used because
  *   - a precise number of elements need to be dequeued
  *   - the operation does not have a steady state
  *   - the operation measurement is too small
  */
@BenchmarkMode(Array(Mode.SingleShotTime))
@OperationsPerInvocation(1000)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Group)
@Measurement(batchSize = 1000, iterations = 20)
@Warmup(batchSize = 1000, iterations = 20)
class QueueBenchmark {

  /** _USAGE_: `-opi 1 -jvmArgsAppend -Dopi=1` */
  val opi = Option(Integer.getInteger("opi")).fold(1000)(_.intValue())

  @Param(
    Array(
      "ConcurrentLinkedQueue",
      "MpscLinkedQueue",
      // "custom.Basic",
      // "custom.Padded64",
      // "custom.Padded128",
      // "jiffy.Basic(16)",
      // "jiffy.Padded64(16)",
      // "jiffy.Padded128(16)",
      "vyukov.Basic",
      "vyukov.Padded32",
      "vyukov.Padded64",
      "vyukov.Padded128"
      // "vyukov.RPadded64",
      // "vyukov.RPadded128"
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
    val n = opi
    var i = 0
    while (i < n) {
      val a = new Object()
      q.add(a)
      i += 1
    }
    i
  }

  def poll(producers: Int) = {
    val n = opi * producers
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
  @Group("p04")
  @GroupThreads(4)
  def add_04() =
    add()

  @Benchmark
  @Group("p04")
  @GroupThreads(1)
  def poll_04() =
    poll(4)

  @Benchmark
  @Group("p08")
  @GroupThreads(8)
  def add_08() =
    add()

  @Benchmark
  @Group("p08")
  @GroupThreads(1)
  def poll_08() =
    poll(8)

  @Benchmark
  @Group("p16")
  @GroupThreads(16)
  def add_16() =
    add()

  @Benchmark
  @Group("p16")
  @GroupThreads(1)
  def poll_16() =
    poll(16)
}
