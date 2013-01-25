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
package model.serializer

import android.os.Parcel
import android.os.Parcelable

import android.util.Log

import collection.mutable.ArrayBuffer

import model._

class ModelParceller (val delay: AbstractDelay) extends Parcelable {
  def describeContents(): Int = 0

  def writeToParcel(parcel: Parcel, flags: Int) {
    assert(delay != null, "ModelParceller.writeToParcel: cannot serialize a null object")

    def writer_rec(item: AbstractDelay): Unit = {
      item match {
        case i: DelayItem => {
          parcel.writeByte(ModelParceller.MarkerItemStart)
          parcel.writeInt(i.amount)
          parcel.writeInt(i.color)
        }
        case g: DelayGroup => {
          parcel.writeByte(ModelParceller.MarkerGroupStart)
          parcel.writeInt(g.k)
          g.items.foreach(writer_rec(_))
          parcel.writeByte(ModelParceller.MarkerGroupEnd)
        }
      }
    }

    writer_rec(delay)
  }
}

object ModelParceller{
  private val MarkerItemStart  : Byte = 0
  private val MarkerGroupStart : Byte = 1
  private val MarkerGroupEnd   : Byte = 2

  val CREATOR = new Parcelable.Creator[ModelParceller] {
    def newArray(n: Int) = Array.ofDim[ModelParceller](n)

    def createFromParcel(parcel: Parcel): ModelParceller = {
      def creator_rec: ArrayBuffer[AbstractDelay] = {
        val list = ArrayBuffer.empty[AbstractDelay]

        while(true) {
          if(parcel.dataAvail == 0) return list

          val tag = parcel.readByte
          if(tag == MarkerItemStart) {
            // Log.v("creator_rec", "MarkerItemStart")
            list += DelayItem(parcel.readInt, parcel.readInt)
          } else if(tag == MarkerGroupStart) {
            // Log.v("creator_rec", "MarkerGroupStart")
            val k = parcel.readInt
            val items = creator_rec

            list += DelayGroup(k, items)
          } else if(tag == MarkerGroupEnd) {
            // Log.v("creator_rec", "MarkerGroupEnd")
            return list
          } else {
            throw new Exception("ModelParceller.createFromParcel: invalid tag")
          }
        }

        list
      }

      // Log.v("createFromParcel", "Parcel: " + parcel.toString)

      // Log.v("createFromParcel", "Available bytes in parcel: " + parcel.dataAvail)

      val result = creator_rec

      // Log.v("createFromParcel", "Parsed: " + result.toString)

      assert(result.size == 1, "ModelParceller.createFromParcel: invalid object structure")

      new ModelParceller(result(0))
    }
  }
}


