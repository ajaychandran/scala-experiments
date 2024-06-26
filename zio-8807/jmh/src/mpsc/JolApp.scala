package mpsc

import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.vm.VM
import org.jctools.queues.MpscLinkedQueue

object JolApp extends App {

  println(VM.current.details())
  println(
    ClassLayout.parseClass(classOf[MpscLinkedQueue[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[custom.Basic[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[custom.Padded64[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[custom.Padded128[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[jiffy.Basic[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[jiffy.Padded64[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[jiffy.Padded128[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.Basic[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.Padded32[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.Padded64[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.Padded128[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.RPadded64[AnyRef]]).toPrintable()
  )
  println(
    ClassLayout.parseClass(classOf[vyukov.RPadded128[AnyRef]]).toPrintable()
  )
}
