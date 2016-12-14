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

package fr.cnrs.liris.privamov.core.clustering

import fr.cnrs.liris.common.geo.{Distance, Point}
import fr.cnrs.liris.privamov.core.model.Event

import scala.collection.mutable

/**
 * Density joinable clustering algorithm.
 *
 * Changqing Zhou, Dan Frankowski, Pamela Ludford, Shashi Shekhar and Loren
 * Terveen. Discovering Personal Gazetteers: An Interactive Clustering Approach.
 * In GIS 2004.
 */
class DJClusterer(epsilon: Distance, minPoints: Int) extends Clusterer {
  require(minPoints > 0, s"minPoints must be > 0 (got $minPoints)")
  require(epsilon > Distance.Zero, s"Epsilon must be > 0 (got $epsilon)")

  override def cluster(events: Seq[Event]): Seq[Cluster] = {
    var clusters = mutable.ListBuffer.empty[Cluster]
    for (event <- events) {
      val neighborhood = neighbors(event.point, events)
      if (neighborhood.length >= minPoints) {
        val newCluster = mutable.ListBuffer.empty[Event]
        newCluster ++= neighborhood
        val intersecting = clusters.filter(cluster => cluster.events.intersect(newCluster).nonEmpty)
        intersecting.foreach(cluster => newCluster ++= cluster.events)
        clusters = clusters.diff(intersecting)
        clusters += new Cluster(newCluster.toList)
      }
    }
    clusters
  }

  /**
   * Naive function returning neighbors of a given point.
   *
   * @param point Point we want to get the neighbors.
   * @param world All points.
   * @return Points that are within a threshold fixed by `epsilon`.
   */
  private def neighbors(point: Point, world: Seq[Event]): Seq[Event] =
  world.filter(r => r.point.distance(point) <= epsilon)
}