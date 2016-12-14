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

import com.google.inject.Inject
import fr.cnrs.liris.accio.core.api._
import fr.cnrs.liris.privamov.core.model.Trace
import fr.cnrs.liris.privamov.core.sparkle.SparkleEnv

@Op(
  category = "prepare",
  help = "Regularly sample events inside traces using the modulo operator.",
  description = "It will ensure that the final number of events is exactly (+/- 1) the one required, and that events are regularly sampled (i.e., one out of x).")
class ModuloSamplingOp @Inject()(env: SparkleEnv) extends Operator[ModuloSamplingIn, ModuloSamplingOut] with SparkleOperator {

  override def execute(in: ModuloSamplingIn, ctx: OpContext): ModuloSamplingOut = {
    val input = read(in.data, env)
    val output = input.map(trace => transform(trace, in.n))
    ModuloSamplingOut(write(output, ctx.workDir))
  }

  private def transform(trace: Trace, n: Int) = {
    if (trace.size <= n) {
      trace
    } else {
      val modulo = trace.size.toDouble / n
      // We add an additional take(n) just in case some floating point operation gave an inadequate result, but it is
      // theoretically unnecessary.
      trace.replace(_.zipWithIndex.filter { case (_, idx) => (idx % modulo) < 1 }.map(_._1).take(n))
    }
  }
}

case class ModuloSamplingIn(
  @Arg(help = "Number of events to keep") n: Int,
  @Arg(help = "Input dataset") data: Dataset) {
  require(n >= 0, s"n must be >0 = (got $n)")
}

case class ModuloSamplingOut(
  @Arg(help = "Output dataset") data: Dataset)
