package mpsc
package custom

import java.util.concurrent.atomic.AtomicReference

private[custom] abstract class Padded64Write[A]
    extends AtomicReference(List.empty[A])
    with Serializable

private[custom] abstract class Padded64WritePad[A] extends Padded64Write[A] {
  protected val _0 = 0L;
  protected val _1 = 0L;
  protected val _2 = 0L;
  protected val _3 = 0L;
  protected val _4 = 0L;
  protected val _5 = 0L;
  protected val _6 = 0L;
  protected val _7 = 0L;
}

final class Padded64[A] extends Padded64WritePad[A] with Queue[A] {

  @transient private var read = List.empty[A]

  def add(head: A): Unit = {
    var tail = null.asInstanceOf[List[A]]
    var prev = get()
    do {
      tail = prev
      prev = compareAndExchange(tail, head :: tail)
    } while (prev ne tail)
  }

  def poll(): A = {
    var read = this.read
    if (read.isEmpty) {
      read = getAndSet(Nil)
      if (read eq Nil) return null.asInstanceOf[A]
      if (read.tail ne Nil) read = read.reverse
    }
    this.read = read.tail
    read.head
  }
}
