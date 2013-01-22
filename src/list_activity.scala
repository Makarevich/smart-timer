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

import android.graphics.{Color, Point}
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

    val group_adapter = new GroupAdapter(this, model_node)

    list_view.setAdapter(group_adapter)

    val listener =
      new AdapterView.OnItemClickListener
      with View.OnTouchListener
      with View.OnDragListener
    {
      private object TouchPoint {
        var   x:      Float = 0
        var   width:  Float = 0
      }

      private var drop_y: Int = 0

      def onTouch (v: View, event: MotionEvent): Boolean = {
        if(event.getAction != MotionEvent.ACTION_DOWN) false else {
          TouchPoint.x = event.getX
          // TouchPoint.y = event.getRawY
          TouchPoint.width = v.getWidth
          Log.v("MotionEvent.ACTION_DOWN",
            TouchPoint.x.toString + "/" + TouchPoint.width.toString)

          if(TouchPoint.x * 3 <= TouchPoint.width * 2) false else
          if(group_adapter.isStashBusy) false else {
            // start drag&drop

            val ey = event.getRawY

            val selected_info = get_list_view_visible_items_info(list_view) find {
              case (n, ch, y, dy) =>
                y <= ey && ey <= (y + dy)
            }

            // Log.v("onTouch", "Found a child: " + selected_child.toString)

            if(selected_info.isEmpty) false else {
              val (n, ch, y, dy) = selected_info.get

              val shadow_x = event.getRawX.toInt
              val shadow_y = (ey - y).toInt

              val shadow_builder = new View.DragShadowBuilder(ch) {
                override def onProvideShadowMetrics (sz: Point, pos: Point) {
                  super.onProvideShadowMetrics(sz, pos)
                  pos.set(shadow_x, shadow_y)
                }
              }

              val start_result = ch.startDrag(null, shadow_builder, null, 0)
              Log.v("view.startDrag", start_result.toString)

              if(start_result) group_adapter.stashItemAt(n)

              true
            }
          }
        }
      }

      def onDrag(view: View, event: DragEvent): Boolean = {
        val action = event.getAction
        if(action == DragEvent.ACTION_DROP) {
          // memorize drop site for the logic in ACTION_DRAG_ENDED
          drop_y = event.getY.toInt

        }else if(action == DragEvent.ACTION_DRAG_ENDED){
          val event_abs_y = {
            val loc = Array[Int](0, 0)
            view.getLocationOnScreen(loc)
            val abs_y = drop_y + loc(1)

            drop_y = 0    // zeroize drop_y

            abs_y
          }

          Log.v("ACTION_DRAG_ENDED", "event_abs_y: " + event_abs_y.toString)

          val insert_info = get_list_view_visible_items_info(list_view) find {
            case (n, ch, y, dy) =>

            Log.v("ACTION_DRAG_ENDED", "middle: " + (y + dy/2).toString)

            event_abs_y < (y + dy/2)
          }

          val insert_pos = if(insert_info.isEmpty) 99 else {
            val (n, ch, y, dy) = insert_info.get
            n
          }

          Log.v("ACTION_DRAG_ENDED", "Inserting at " + insert_pos)

          group_adapter.unstashItemAt(insert_pos)
        }

        true
      }

      def onItemClick(parent: AdapterView[_], view: View, pos: Int, id: Long) {
        if(TouchPoint.x * 3 < TouchPoint.width) {
          // display item configuration

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
        }else if(TouchPoint.x * 3 > TouchPoint.width * 2) {
          // drag&drop is started onTouch
        }else{
          // start selection
        }
      }
    }

    list_view.setOnItemClickListener(listener)
    list_view.setOnTouchListener(listener)
    list_view.setOnDragListener(listener)
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

  //////


  def get_list_view_visible_items_info(list_view: ListView) = {
    val first_pos = list_view.getFirstVisiblePosition
    val last_pos  = list_view.getLastVisiblePosition
    
    first_pos to last_pos map { n =>
      val ch = list_view.getChildAt(n)

      val loc = Array[Int](0, 0)
      ch.getLocationOnScreen(loc)

      (n, ch, loc(1), ch.getHeight)
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

  ////
  
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
