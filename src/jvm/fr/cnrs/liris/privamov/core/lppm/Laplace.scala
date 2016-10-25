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

package fr.cnrs.liris.privamov.core.lppm

import com.google.common.geometry.S1Angle
import fr.cnrs.liris.privamov.core.model.Trace
import fr.cnrs.liris.common.geo.{LatLng, Point}
import fr.cnrs.liris.common.random.RandomUtils
import fr.cnrs.liris.common.util.Distance

import scala.util.Random

/**
 * Miguel E. Andrés, Nicolás E. Bordenabe, Konstantinos Chatzikokolakis and
 * Catuscia Palamidessi. 2013. Geo-indistinguishability: differential privacy for
 * location-based systems. In Proceedings of CCS'13.
 *
 * @param seed
 */
class Laplace(seed: Long = RandomUtils.random.nextLong) {
  private[this] val rnd = new Random(seed)

  def transform(trace: Trace, epsilon: Double): Trace = trace.map(rec => rec.copy(point = noise(epsilon, rec.point)))

  /**
   * Return a geo-indistinguishable version of a single point.
   *
   * @param point A point
   * @return A geo-indistinguishable version of this point
   */
  def noise(epsilon: Double, point: Point): Point = {
    val azimuth = math.toDegrees(rnd.nextDouble() * 2 * math.Pi)
    val z = rnd.nextDouble()
    val distance = inverseCumulativeGamma(epsilon, z)
    point.translate(S1Angle.degrees(azimuth), distance)
  }

  def noise(epsilon: Double, point: LatLng): LatLng = {
    val azimuth = math.toDegrees(rnd.nextDouble() * 2 * math.Pi)
    val z = rnd.nextDouble()
    val distance = inverseCumulativeGamma(epsilon, z)
    point.translate(S1Angle.degrees(azimuth), distance)
  }

  def inverseCumulativeGamma(epsilon: Double, z: Double): Distance = {
    val x = (z - 1) / math.E
    val r = -(LambertW.lambertWm1(x) + 1) / epsilon
    Distance.meters(r)
  }
}