package mpsc

import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.vm.VM

object JolApp extends App {

  println(VM.current.details())
}
