package mpsc

import zio.test._
import zio.test.Assertion.hasSameElements
import zio.Scope
import zio.ZIO

object QueueSpec extends ZIOSpecDefault {

  def spec =
    suite("Queue")(
      suite("Custom")(
        suite("Basic")(
          preservers.elements(new custom.Basic()),
          preservers.order(new custom.Basic())
        ),
        suite("Padded128")(
          preservers.elements(new custom.Padded128()),
          preservers.order(new custom.Padded128())
        ),
        suite("Padded64")(
          preservers.elements(new custom.Padded64()),
          preservers.order(new custom.Padded64())
        )
      ),
      suite("Jiffy")(
        suite("Basic")(
          preservers.elements(new jiffy.Basic()),
          preservers.order(new jiffy.Basic())
        ),
        suite("Padded128")(
          preservers.elements(new jiffy.Padded128()),
          preservers.order(new jiffy.Padded128())
        ),
        suite("Padded64")(
          preservers.elements(new jiffy.Padded64()),
          preservers.order(new jiffy.Padded64())
        ),
        suite("(without eager growth)")(
          suite("Basic")(
            preservers.elements(new jiffy.Basic(4, -1)),
            preservers.order(new jiffy.Basic(4, -1))
          ),
          suite("Padded128")(
            preservers.elements(new jiffy.Padded128(4, -1)),
            preservers.order(new jiffy.Padded128(4, -1))
          ),
          suite("Padded64")(
            preservers.elements(new jiffy.Padded64(4, -1)),
            preservers.order(new jiffy.Padded64(4, -1))
          )
        )
      ),
      suite("Vyukov")(
        suite("Basic")(
          preservers.elements(new vyukov.Basic()),
          preservers.order(new vyukov.Basic())
        ),
        suite("Padded128")(
          preservers.elements(new vyukov.Padded128()),
          preservers.order(new vyukov.Padded128())
        ),
        suite("Padded64")(
          preservers.elements(new vyukov.Padded64()),
          preservers.order(new vyukov.Padded64())
        )
      )
    )

  object preservers {

    def elements(q: Queue[AnyRef]) =
      test("preserves elements")(
        check(Gen.chunkOf(Gen.uuid))(expected =>
          for {
            consumer <- ZIO
              .succeed(q.poll())
              .repeatWhile(_ == null)
              .replicateZIO(expected.length)
              .fork
            _ <-
              ZIO
                .withParallelism(expected.length)(
                  ZIO
                    .foreachParDiscard(expected)(s =>
                      ZIO.succeedBlocking(q.add(s))
                    )
                )
                .fork
            actual <- consumer.join
          } yield assert(actual)(hasSameElements(expected))
        )
      )

    def order(q: Queue[AnyRef]) =
      test("preserves insertion order with a single producer")(
        check(Gen.chunkOf(Gen.uuid))(expected =>
          for {
            consumer <- ZIO
              .succeed(q.poll())
              .repeatWhile(_ == null)
              .replicateZIO(expected.length)
              .fork
            _ <- ZIO.succeedBlocking(expected.foreach(q.add)).fork
            actual <- consumer.join
          } yield assert(actual)(Assertion.equalTo(expected))
        )
      )
  }
}
