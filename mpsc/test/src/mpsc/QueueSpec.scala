package mpsc

import zio.test._
import zio.test.Assertion.hasSameElements
import zio.test.TestAspect._
import zio.Scope
import zio.ZIO

object QueueSpec extends ZIOSpecDefault {

  def spec =
    suite("Queue")(
      spec("Jiffy", new Jiffy[AnyRef](4, 1)),
      spec("Vyukov", new Vyukov())
    )

  def spec(name: String, q: Queue[AnyRef]) =
    suite(name)(
      test("preserves elements")(
        check(Gen.chunkOf1(Gen.uuid))(expected =>
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
      ),
      test("preserves insertion order with a single producer")(
        check(Gen.chunkOf1(Gen.uuid))(expected =>
          for {
            consumer <- ZIO
              .succeed(q.poll())
              .repeatWhile(_ == null)
              .replicateZIO(expected.length)
              .fork
            _ <- ZIO.succeedBlocking(expected.foreach(q.add)).fork
            actual <- consumer.join
          } yield assert(actual)(Assertion.equalTo(expected.toChunk))
        )
      )
    ) @@ sequential
}
