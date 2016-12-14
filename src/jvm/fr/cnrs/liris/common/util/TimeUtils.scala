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

package fr.cnrs.liris.common.util

import com.twitter.util.Duration

/**
 * Helpers dealing with time and duration.
 */
object TimeUtils {
  /**
   * Convert a duration to a user-friendly string representation.
   *
   * @param time Duration.
   */
  def prettyTime(time: Duration): String = {
    val ms = time.inMillis.toDouble
    if (ms < 10.0) {
      f"$ms%.2f ms"
    } else if (ms < 100.0) {
      f"$ms%.1f ms"
    } else if (ms < 1000.0) {
      f"$ms%.0f ms"
    } else {
      f"${ms / 1000}%.3f s"
    }
  }
}
