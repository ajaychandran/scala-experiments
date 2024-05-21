package mpsc

object QueueFactory {

  def apply[A <: AnyRef](name: String, step: Int, grow: Int): Queue[A] =
    name match {
      case "ConcurrentLinkedQueue" =>
        new JWrapper(new java.util.concurrent.ConcurrentLinkedQueue())
      case "Jiffy" =>
        new QWrapper(new Jiffy(step, grow))
      case "JiffyAligned" =>
        new QWrapper(new JiffyAligned(step, grow))
      case "MpscLinkedQueue" =>
        new JWrapper(new org.jctools.queues.MpscLinkedQueue())
      case "Vyukov" =>
        new QWrapper(new Vyukov())
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
