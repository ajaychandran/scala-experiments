package mpsc
package custom

import java.util.concurrent.atomic.AtomicReference

final class Basic[A] extends AtomicReference(List.empty[A]) with Queue[A] {

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
