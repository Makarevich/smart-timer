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

package makarevich.smart_timer

import android.app.Application
import android.content.Context
import android.os.Parcel
import android.util.Log

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

    def log(s: String): Unit = Log.v("MyApplication.onCreate", s)


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
        parcel.setDataPosition(0)

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

  def find_model_node(intent_path: Array[Byte]): DelayGroup = {
    Log.v("MyApplication::find_model_node",
      "intent_path: " + intent_path.mkString(","))

    class InvalidModelPath extends Throwable

    try {
      if(intent_path.isEmpty) model else {
        @tailrec def descend_model(path: List[Byte], group: DelayGroup): DelayGroup = {
          if(path == Nil) group else {
            val node = group.items(path.head)

            if(node.isInstanceOf[DelayGroup])
              descend_model(path.tail, node.asInstanceOf[DelayGroup])
              else throw new InvalidModelPath
          }
        }
        descend_model(intent_path.toList, model)
      }
    } catch { case e: InvalidModelPath =>
      null
    }
  }

  def save_state {
    def handler[T <: Throwable](e: T) {
      // Log.v("save_state", "Exception happened")
      throw e
    }

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
      case e: FileNotFoundException => handler(e)
      case e: IOException => handler(e)
    }
  }
}

