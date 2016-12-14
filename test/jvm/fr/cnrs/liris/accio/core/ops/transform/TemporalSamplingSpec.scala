package fr.cnrs.liris.accio.core.ops.transform

import com.github.nscala_time.time.Imports._
import fr.cnrs.liris.accio.core.model.{Event, Trace}
import fr.cnrs.liris.accio.testing.WithTraceGenerator
import fr.cnrs.liris.testing.UnitSpec

class TemporalSamplingSpec extends UnitSpec with WithTraceGenerator {
  "TemporalSampling" should "downsample traces" in {
    val trace = Trace(Seq(
      Event(Me, Here, Now),
      Event(Me, Here, Now + 10.seconds),
      Event(Me, Here, Now + 19.seconds),
      Event(Me, Here, Now + 25.seconds),
      Event(Me, Here, Now + 34.seconds),
      Event(Me, Here, Now + 44.seconds)))
    val res = transform(trace, 10.seconds)
    res.user shouldBe trace.user
    res.events should contain theSameElementsInOrderAs Seq(
      Event(Me, Here, Now),
      Event(Me, Here, Now + 10.seconds),
      Event(Me, Here, Now + 25.seconds),
      Event(Me, Here, Now + 44.seconds))
  }

  it should "handle empty traces" in {
    val trace = Trace.empty(Me)
    val res = transform(trace, 10.seconds)
    res.user shouldBe trace.user
    res.events shouldBe trace.events
  }

  it should "handle singleton traces" in {
    val trace = Trace(Seq(Event(Me, Here, Now)))
    val res = transform(trace, 10.seconds)
    res.user shouldBe trace.user
    res.events shouldBe trace.events
  }

  private def transform(trace: Trace, duration: Duration) = {
    val res = TemporalSamplingOp(duration).transform(trace)
    res should have size 1
    res.head
  }
}