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


import android.app.Activity
import android.view._
import android.widget._

import android.util.Log

private class ColorAdapter (ctxt: Activity)
extends BaseAdapter with SpinnerAdapter
{
  private val color_array: Array[Int] = {
    val ta = ctxt.getResources.obtainTypedArray(R.array.colors)

    0 until ta.length map { i =>
      //Log.v("ColorAdapter", "fetching color " + i.toString)
      ta getColor(i, 0)
      } toArray
  }

  ///////////////////////////////////////
  // BaseAdapter abstract members
  //

  override def getItemId(i: Int) = 0 // color_array(i)
  override def getItem(i: Int) = null

  override def getCount: Int = color_array.size

  override def getView(i: Int, another: View, parent: ViewGroup): View = {
    val view = ctxt.getLayoutInflater.inflate(R.layout.color_box, null)

    view.asInstanceOf[ImageView].setBackgroundColor(color_array(i))

    view
  }

  override def getDropDownView(pos: Int, convertView: View, parent: ViewGroup): View = {
    getView(pos, convertView, parent)
  }

  assert(getCount > 0, "Empty array detected")
}

