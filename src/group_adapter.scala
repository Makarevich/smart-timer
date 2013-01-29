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


import model._

private class GroupAdapter (ctxt: Activity, group: DelayGroup) extends BaseAdapter {
  override def getViewTypeCount = 2
  override def getItemViewType(pos: Int) = group.items(pos) match {
    case _: DelayItem => 0
    case _: DelayGroup => 1
  }

  ///////////////////////////////////////
  // BaseAdapter abstract members
  //

  def getItemId(i: Int) = 0
  def getItem(i: Int) = group.items(i)

  def getCount = group.items.size

  def getView(i: Int, another: View, parent: ViewGroup): View = {
    def inflate(id: Int)(f: View => Unit): View = {
      val view = ctxt.getLayoutInflater.inflate(id, null)
      f(view)
      view
    }

    group.items(i) match {
      case item: DelayItem => inflate(R.layout.delay_item) { view =>
        val im = view.findViewById(R.id.image_view).asInstanceOf[ImageView]
        val tv = view.findViewById(R.id.item_text_view).asInstanceOf[TextView]

        im.setBackgroundColor(item.color)
        tv.setText(item.amount.toString)
      }

      case subgroup: DelayGroup => inflate(R.layout.delay_group_item) { view =>
        val tv = view.findViewById(R.id.group_text_view).asInstanceOf[TextView]

        tv.setText(subgroup.k.toString)
      }
    }
  }

  /////////////////////////////////
  // stash methods
  //
  
  def stashItemAt(pos: Int) {
    assert(stash == null, "Stashing an item to a non-empty stash")
    stash = group.items.remove(pos)
    notifyDataSetChanged
  }

  def unstashItemAt(pos: Int) {
    assert(stash != null, "Fetching an item from an empty stash")

    val sz = group.items.size
    val p = if(pos > sz) sz else pos

    group.items.insert(p, stash)

    stash = null

    notifyDataSetChanged
  }

  def isStashBusy: Boolean = stash != null

  private var stash: AbstractDelay = null

  /////////////////////////////////
  // clone/kill stuff
  //

  def cloneItemsAtPositions (poss: List[Int]) {
    group.items.insertAll(
      poss.max + 1,
      poss map { n => group.items(n).copy } 
    )
  }

  def killItemsAtPositions (poss: List[Int]) {
    var its = List.empty[AbstractDelay]

    poss.sortWith(_ > _).foreach { n =>
      its = (group.items remove n) :: its
    }

    ctxt.getApplication.asInstanceOf[MyApplication]
      .kill_buffer.items.prependAll(its)

    notifyDataSetChanged
  }

  def yankKilledItem(killed_pos: Int) {
    val item = ctxt.getApplication.asInstanceOf[MyApplication]
      .kill_buffer.items.remove(killed_pos)

    group.items.append(item)

    notifyDataSetChanged
  }

  ////////////////////////////////
  // k coeff
  //

  def group_k = group.k

  def set_group_k(new_k: Int) {
    group.k = new_k
  }
}

