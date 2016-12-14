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

import fr.cnrs.liris.common.geo.Distance
import fr.cnrs.liris.privamov.testing.WithCabspotting
import fr.cnrs.liris.testing.UnitSpec
import org.joda.time.Duration

/**
 * Unit tests for [[IdentityClusterer]].
 */
class IdentityClustererSpec extends UnitSpec with WithCabspotting {
  behavior of "IdentityClusterer"

  it should "cluster a trace" in {
    val clusters = IdentityClusterer.cluster(abboipTrace)
    clusters should have size abboipTrace.size
    clusters.foreach { cluster =>
      cluster.events should have size 1
    }
  }

  it should "be deterministic" in {
    val clusterer = new DTClusterer(Duration.standardMinutes(15), Distance.meters(100))
    val clustersByRun = Seq.fill(5)(clusterer.cluster(abboipTrace))
    (1 until 5).foreach { i =>
      clustersByRun(i) should contain theSameElementsAs clustersByRun.head
    }
  }
}