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
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.graphics.Color
import android.graphics.drawable._
import android.view._
import android.widget._

import annotation.tailrec


import model._

class ListActivity extends Activity {
  private var intent_path: Array[Byte] = null

  private val this_activity = this

  private class InvalidModelPath extends Throwable

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.group_view)
    val list_view = findViewById(R.id.list_view).asInstanceOf[ListView]

    intent_path = getIntent.getByteArrayExtra(
      ListActivity.IntentExtraModelPathByteArray
    )

    if(intent_path == null) intent_path = Array.empty[Byte]

    val model_node = {
      Log.v("intent_path", intent_path.mkString(","))

      val model = getApplication.asInstanceOf[MyApplication].model

      try {
        if(intent_path == null) model else {
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
        return finish()
      }
    }

    list_view.setAdapter(new GroupAdapter(this, model_node))

    list_view.setOnItemClickListener(new AdapterView.OnItemClickListener {
      def onItemClick(parent: AdapterView[_], view: View, pos: Int, id: Long) {
        val item_at_pos = parent.getItemAtPosition(pos)
        
        if(item_at_pos.isInstanceOf[DelayGroup]) {
          val intent = new Intent(this_activity, this_activity.getClass)

          val new_path: Array[Byte] = intent_path :+ pos.toByte

          Log.v("onItemClick", "New path: " + new_path.mkString(", "))

          intent.putExtra(
            ListActivity.IntentExtraModelPathByteArray,
            new_path
          )
          startActivity(intent)
        }
      }
    })
  }

  override def onSaveInstanceState (savedInstanceState: Bundle) {
    super.onSaveInstanceState(savedInstanceState)
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.action_bar, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    def toast(text: String): Boolean = {
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show
      true
    }

    item.getItemId match {
      case R.id.action_play => toast ("Playing")
      case R.id.action_yank => toast ("Yanking")

      case _ => super.onOptionsItemSelected(item)
    }
  }
}

object ListActivity {
  private val IntentExtraModelPathByteArray = "MODEL_PATH"
}

private class GroupAdapter (ctxt: Activity, group: DelayGroup) extends BaseAdapter {
  override def getViewTypeCount = 2
  override def getItemViewType(pos: Int) = group.items(pos) match {
    case _: DelayItem => 0
    case _: DelayGroup => 1
  }

  /////

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
}



/*

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

  def getItem(i: Int) = null
  def getItemId(i: Int) = 0

  def getCount = views.size

  def getView(i: Int, another: View, parent: ViewGroup): View = {
    views(i)
  }
}

private class TestMultiChoiceModeListener extends AbsListView.MultiChoiceModeListener {
  def onCreateActionMode(mode: ActionMode,x$2: Menu): Boolean = true
  def onDestroyActionMode(mode: ActionMode): Unit = {}

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

    list.setMultiChoiceModeListener(new TestMultiChoiceModeListener)

    list.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick (parent: AdapterView[_], view: View, pos: Int, id: Long) {

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

*/
