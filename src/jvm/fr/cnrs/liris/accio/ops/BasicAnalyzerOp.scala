/*
 * Copyright LIRIS-CNRS (2016)
 * Contributors: Vincent Primault <vincent.primault@liris.cnrs.fr>
 *
 * This software is a computer program whose purpose is to study location privacy.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.cnrs.liris.accio.ops

import com.github.nscala_time.time.Imports._
import fr.cnrs.liris.accio.core.dataset.DataFrame
import fr.cnrs.liris.accio.core.framework._
import fr.cnrs.liris.accio.core.model.Trace
import fr.cnrs.liris.accio.ops.BasicAnalyzerOp._

/**
 * An analyzer computing basic statistics about a trace: its size, length and duration.
 */
@Op(
  help = "Compute basic statistics about traces",
  category = "metric",
  metrics = Array("size", "length", "duration")
)
case class BasicAnalyzerOp() extends Analyzer[Input, Output] {
  override def execute(in: Input, ctx: OpContext): Output = {
    val metrics = in.data.map { trace =>
      (trace.size, trace.length.meters, trace.duration.seconds)
    }.toArray

    Output(
      size = metrics.map(_._1.toLong),
      length = metrics.map(_._2),
      duration = metrics.map(_._3))
  }

  override def analyze(trace: Trace): Seq[Metric] = {
    Seq(
      Metric("size", trace.size),
      Metric("length", trace.length.meters),
      Metric("duration", trace.duration.seconds))
  }
}

object BasicAnalyzerOp {

  case class Input(@In(help = "Input dataset") data: DataFrame[Trace])

  case class Output(
    @Out(help = "Traces sizes") size: Array[Long],
    @Out(help = "Traces lengths") length: Array[Double],
    @Out(help = "Traces durations") duration: Array[Long])

}