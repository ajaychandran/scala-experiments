package mpsc

object QueueFactory {

  val Jiffy = """jiffy.Basic[\(](\d+)[\)]""".r
  val Jiffy128 = """jiffy.Padded128[\(](\d+)[\)]""".r
  val Jiffy64 = """jiffy.Padded64[\(](\d+)[\)]""".r

  def apply[A <: AnyRef](name: String): Queue[A] =
    name match {
      case "ConcurrentLinkedQueue" =>
        new JWrapper(new java.util.concurrent.ConcurrentLinkedQueue())
      case "MpscLinkedQueue" =>
        new JWrapper(new org.jctools.queues.MpscLinkedQueue())
      case "custom.Basic" =>
        new QWrapper(new custom.Basic())
      case "custom.Padded64" =>
        new QWrapper(new custom.Padded64())
      case "custom.Padded128" =>
        new QWrapper(new custom.Padded128())
      case Jiffy(size) =>
        new QWrapper(new jiffy.Basic(size.toInt))
      case Jiffy64(size) =>
        new QWrapper(new jiffy.Padded64(size.toInt))
      case Jiffy128(size) =>
        new QWrapper(new jiffy.Padded128(size.toInt))
      case "vyukov.Basic" =>
        new QWrapper(new vyukov.Basic())
      case "vyukov.Padded32" =>
        new QWrapper(new vyukov.Padded32())
      case "vyukov.Padded64" =>
        new QWrapper(new vyukov.Padded64())
      case "vyukov.Padded128" =>
        new QWrapper(new vyukov.Padded128())
      case "vyukov.RPadded64" =>
        new QWrapper(new vyukov.RPadded64())
      case "vyukov.RPadded128" =>
        new QWrapper(new vyukov.RPadded128())
      case _ =>
        throw new RuntimeException(s"unsupported: $name")
    }
}

private class JWrapper[A](q: java.util.Queue[A]) extends Queue[A] {
  def poll(): A = q.poll()
  def add(a: A): Unit = q.offer(a)
}

private class QWrapper[A](q: Queue[A]) extends Queue[A] {

  def poll(): A = q.poll()
  def add(data: A): Unit = q.add(data)
}
