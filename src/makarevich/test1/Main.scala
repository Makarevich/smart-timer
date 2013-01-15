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
import android.os.Bundle
import android.util.Log

import android.graphics.Color
import android.graphics.drawable._
import android.view._
import android.widget._

private class DataList(ctxt: Activity) extends BaseAdapter {
  import collection.mutable.Buffer

  private val views: Buffer[View] = {
    val color_count = ctxt.getResources.getInteger(R.integer.color_count)

    val builder = Buffer.newBuilder[View]
    
    for {
      i <- 0 to color_count
    } {
      val item = ctxt.getLayoutInflater.inflate(R.layout.main_item, null)

      val col = Color.HSVToColor(Array[Float](360 * i.toFloat / color_count, 1, 1))

      val im = item.findViewById(R.id.image_view).asInstanceOf[ImageView]
      val te = item.findViewById(R.id.text_view).asInstanceOf[TextView]

      im.setBackgroundColor(col)

      te.setText(col.toString)

      builder += item
    }

    builder.result
  }

  /*
  override def areAllItemsEnabled = false
  override def isEnabled(pos: Int) = if(pos == 3) false else super.isEnabled(pos)
  */

  def getItem(i: Int) = null
  def getItemId(i: Int) = 0

  def getCount = views.size

  def getView(i: Int, another: View, parent: ViewGroup): View = {
    views(i)
  }

  //////

  def dump_selected {
    val s = views map { _ isSelected }

    Log.v("Test", "Data selected: " + s.mkString(", "))
  }

  def deleteItem(pos: Int) {

    views.remove(pos)


    notifyDataSetChanged
  }

  def uncheck_all {
    views.foreach { _.asInstanceOf[Checkable].setChecked(false) }
  }

}

private class TestMultiChoiceModeListener(data_set: DataList) extends AbsListView.MultiChoiceModeListener {
  def onCreateActionMode(mode: ActionMode,x$2: Menu): Boolean = true
  def onDestroyActionMode(mode: ActionMode): Unit = { data_set.uncheck_all }

  def onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false
  def onPrepareActionMode(mode: ActionMode,x$2: Menu): Boolean = false

  def onItemCheckedStateChanged(mode: ActionMode, pos: Int, id: Long, checked: Boolean) {
    Log.v("Test", "Item at " + pos + " is " +
      (if(checked) "checked" else "not checked"))
  }
}

class Main extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val list = findViewById(R.id.list_view).asInstanceOf[ListView]

    val data_set = new DataList(this)

    list.setAdapter(data_set)
    
    list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL)

    list.setMultiChoiceModeListener(new TestMultiChoiceModeListener(data_set))

    list.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick (parent: AdapterView[_], view: View, pos: Int, id: Long) {
        /*
        Log.v("Test", "Item at " + pos + " is " +
          (if(list.isItemChecked(pos)) "selected" else "not selected"))
        */

        /*
        view.setBackgroundColor(getResources.getColor(
          if(list.isItemChecked(pos)) R.color.orange else R.color.black
          ))
        */

        // val b = list.isItemChecked(pos)
        // list.setItemChecked(pos, ! list.isItemChecked(pos))


        list.setItemChecked(pos, true)
        Log.v("Test", "Checking item " + pos)

        val pp = list.getCheckedItemPositions

        val poss = 0 to pp.size filter { pp valueAt _ } map { pp keyAt _ }

        // Log.v("Test", "Bb: " + b)
        Log.v("Test", "Checked: " + poss.mkString(", "))

        //data_set.dump_selected
      }
    })
  }
}
