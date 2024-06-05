package mpsc

object QueueFactory {

  def apply[A <: AnyRef](name: String): Queue[A] =
    name match {
      case "ConcurrentLinkedQueue" =>
        new JWrapper(new java.util.concurrent.ConcurrentLinkedQueue())
      case "Jiffy" =>
        val step = Option(Integer.getInteger("step")).fold(4)(_.intValue())
        val grow = Option(Integer.getInteger("grow")).fold(1)(_.intValue())
        new QWrapper(new Jiffy(step, grow))
      case "MpscLinkedQueue" =>
        new JWrapper(new org.jctools.queues.MpscLinkedQueue())
      case "Vyukov" =>
        new QWrapper(new Vyukov())
      case "VyukovExtend" =>
        new QWrapper(new VyukovExtend())
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
