/*
 * Accio is a program whose purpose is to study location privacy.
 * Copyright (C) 2016 Vincent Primault <vincent.primault@liris.cnrs.fr>
 *
 * Accio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Accio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Accio.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnrs.liris.privamov.ops

import java.time.Duration

import com.google.common.geometry.S2CellId
import com.google.inject.Inject
import fr.cnrs.liris.accio.core.api._
import fr.cnrs.liris.common.util.Requirements._
import fr.cnrs.liris.privamov.core.io.Decoder
import fr.cnrs.liris.privamov.core.model.Trace
import fr.cnrs.liris.privamov.core.sparkle.SparkleEnv

@Op(
  category = "metric",
  help = "Compute area coverage difference between two datasets of traces")
class TemporalAreaCoverageOp @Inject()(
  override protected val env: SparkleEnv,
  override protected val decoders: Set[Decoder[_]])
  extends Operator[TemporalAreaCoverageIn, TemporalAreaCoverageOut] with SparkleReadOperator {

  override def execute(in: TemporalAreaCoverageIn, ctx: OpContext): TemporalAreaCoverageOut = {
    val train = read[Trace](in.train)
    val test = read[Trace](in.test)
    val metrics = train.zip(test).map { case (ref, res) => evaluate(ref, res, in.level) }.toArray
    /*TemporalAreaCoverageOut(
      precision = metrics.map { case (k, v) => k -> v._1 }.toMap,
      recall = metrics.map { case (k, v) => k -> v._2 }.toMap,
      fscore = metrics.map { case (k, v) => k -> v._3 }.toMap)*/
    null.asInstanceOf[TemporalAreaCoverageOut]
  }

  private def evaluate(ref: Trace, res: Trace, level: Int) = {
    requireState(ref.id == res.id, s"Trace mismatch: ${ref.id} / ${res.id}")
    val refCells = getCells(ref, level)
    val resCells = getCells(res, level)
    val matched = resCells.intersect(refCells).size
    (ref.id, (MetricUtils.precision(resCells.size, matched), MetricUtils.recall(refCells.size, matched), MetricUtils.fscore(refCells.size, resCells.size, matched)))
  }

  private def getCells(trace: Trace, level: Int) =
    trace.events.map(rec => S2CellId.fromLatLng(rec.point.toLatLng.toS2).parent(level)).toSet
}

case class TemporalAreaCoverageIn(
  @Arg(help = "S2 cells levels") level: Int,
  @Arg(help = "Train dataset") train: Dataset,
  @Arg(help = "Test dataset") test: Dataset)

case class TemporalAreaCoverageOut(
  @Arg(help = "Spatial distortion min") min: Map[String, Duration],
  @Arg(help = "Spatial distortion max") max: Map[String, Duration],
  @Arg(help = "Spatial distortion stddev") stddev: Map[String, Duration],
  @Arg(help = "Spatial distortion avg") avg: Map[String, Duration],
  @Arg(help = "Spatial distortion median") median: Map[String, Duration])