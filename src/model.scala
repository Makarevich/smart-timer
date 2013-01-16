/*
    Copyright 2013 Yury Makarevich

    This file is part of Smart Timer.

    Smart Timer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Smart Timer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Smart Timer.  If not, see <http://www.gnu.org/licenses/>.
*/

package makarevich.test1
package model

import collection.mutable.ArrayBuffer

sealed abstract class AbstractDelay

case class DelayItem (                // TODO: add more fields...
  var amount:       Int,
  var color:        Int
) extends AbstractDelay

case class DelayGroup (
  var k:            Int,
  var items:        ArrayBuffer[AbstractDelay]
) extends AbstractDelay


