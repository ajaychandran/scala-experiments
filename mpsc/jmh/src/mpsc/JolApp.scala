package mpsc

import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.vm.VM

object JolApp extends App {

  println(VM.current.details())
  println(ClassLayout.parseClass(classOf[JiffyAligned.Segment]).toPrintable())
  println(ClassLayout.parseClass(classOf[JiffyAligned[AnyRef]]).toPrintable())
}
