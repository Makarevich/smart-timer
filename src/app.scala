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

import android.app.Application
import android.content.Context
import android.os.Parcel

import java.io.{FileNotFoundException, IOException}

import collection.mutable.{ArrayBuffer, ArrayBuilder}

import annotation.tailrec

import model._
import model.serializer._

object MyApplication {
  private val state_file_name = "state.dat"
}

class MyApplication extends Application {
  val model: DelayGroup = DelayGroup(1, ArrayBuffer.empty)

  val kill_buffer = DelayGroup(1, ArrayBuffer.empty)

  override def onCreate {
    super.onCreate

    def default_init {
      // some defaults
      model.items = ArrayBuffer(
          DelayItem(10, 0),
          DelayItem(20, 0),
          DelayGroup(3, ArrayBuffer(
            DelayItem(10, 0),
            DelayGroup(5, ArrayBuffer(
              DelayItem(20, 0),
              DelayItem(20, 0),
              DelayItem(20, 0)
            )),
            DelayItem(20, 0)
          )),
          DelayItem(10, 0)
        )
    }

    try {
      val data: Array[Byte] = {
        val buf = ArrayBuilder.make[Byte]

        val stream =
          openFileInput(MyApplication.state_file_name)

        try {
          @tailrec def read_whole_stream {
            val b = stream.read
            if(b >= 0 && b < 256) {
              buf += b.toByte
              read_whole_stream
            }
          }

          read_whole_stream
        } finally {
          stream.close
        }

        buf.result
      }

      val parcel = Parcel.obtain

      try {
        parcel.unmarshall(data, 0, data.size)

        model.items = ModelParceller.CREATOR.createFromParcel(parcel)
                        .delay.asInstanceOf[DelayGroup].items
      } finally {
        parcel.recycle
      }

    } catch {
      case e: FileNotFoundException => default_init
      case e: IOException => default_init
    }
  }

  def save_state {
    try {
      val data = {
        val parcel = Parcel.obtain

        try {
          new ModelParceller(model).writeToParcel(parcel, 0)

          parcel.marshall
        } finally {
          parcel.recycle
        }
      }

      val stream =
        openFileOutput(MyApplication.state_file_name, Context.MODE_PRIVATE)

      try stream.write(data, 0, data.size)
      finally stream.close
    } catch {
      case e: FileNotFoundException =>
      case e: IOException =>
    }
  }
}

