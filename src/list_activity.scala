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

import android.app.{Activity,ActionBar}
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.graphics.{Color, Point}
import android.graphics.drawable._
import android.view._
import android.widget._

import annotation.tailrec


import model._




object ListActivity {
  private val IntentExtraModelPathByteArray = "MODEL_PATH"

  private val KillBufferActivityRequestCode = 0
}



class ListActivity extends Activity {
  private var intent_path: Array[Byte] = null
  private var group_adapter: GroupAdapter = null

  private val this_activity = this

  private class InvalidModelPath extends Throwable

  private def get_list_view_visible_items_info(list_view: ListView) = {
    val first_pos = list_view.getFirstVisiblePosition
    val last_pos  = list_view.getLastVisiblePosition
    
    first_pos to last_pos map { n =>
      val ch = list_view.getChildAt(n)

      val loc = Array[Int](0, 0)
      ch.getLocationOnScreen(loc)

      (n, ch, loc(1), ch.getHeight)
    }
  }

  //////

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
        return finish()
      }
    }

    {
      val bar = getActionBar
      
      bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE)
      bar.setTitle(R.string.activity_name_list_view)
    }

    group_adapter = new GroupAdapter(this, model_node)

    list_view.setAdapter(group_adapter)

    val listener =
      new  AdapterView.OnItemClickListener
      with View.OnTouchListener
      with View.OnDragListener
      with AbsListView.MultiChoiceModeListener
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

          // Log.v("ACTION_DRAG_ENDED", "event_abs_y: " + event_abs_y.toString)

          val insert_info = get_list_view_visible_items_info(list_view) find {
            case (n, ch, y, dy) =>

            // Log.v("ACTION_DRAG_ENDED", "middle: " + (y + dy/2).toString)

            event_abs_y < (y + dy/2)
          }

          val insert_pos = if(insert_info.isEmpty) 99 else {
            val (n, ch, y, dy) = insert_info.get
            n
          }

          // Log.v("ACTION_DRAG_ENDED", "Inserting at " + insert_pos)

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
          }else if(item_at_pos.isInstanceOf[DelayItem]) {
            val delay_item = item_at_pos.asInstanceOf[DelayItem]

            val dia = new DelayItemConfigDialogFragment (
              delay_item.amount,
              delay_item.color,
              { (am, co) =>
                delay_item.amount = am
                delay_item.color  = co
                group_adapter.notifyDataSetChanged
              }
            )

            dia.show(getFragmentManager, "item_config_dialog")
          }
        }else if(TouchPoint.x * 3 > TouchPoint.width * 2) {
          // drag&drop is started onTouch
        }else{
          // start selection

          list_view.setItemChecked(pos, true)
        }
      }

      /*
      def onItemLongClick(
        parent: AdapterView[_], view: View, pos: Int, id: Long
      ): Boolean = {
        val item_at_pos = parent.getItemAtPosition(pos)
          
        if(item_at_pos.isInstanceOf[DelayGroup]) {
          val group = item_at_pos.asInstanceOf[DelayGroup]

          Toast.makeText(this_activity,
            "Group k: " + group.k.toString,
            Toast.LENGTH_SHORT).show
        }

        false
      }
      */


      /////////////////////////////
      /// selection action mode
      ///

      private val checked_items = collection.mutable.Set.empty[Int]

      def onPrepareActionMode(mode: ActionMode, menu: Menu) = false

      def onDestroyActionMode(mode: ActionMode): Unit =
        checked_items.clear

      def onItemCheckedStateChanged(
            mode: ActionMode,
            pos: Int,
            id: Long,
            checked: Boolean): Unit = 
        if(checked) checked_items.add(pos)
        else checked_items.remove(pos)

      def onCreateActionMode (mode: ActionMode, menu: Menu): Boolean = {
        mode.getMenuInflater.inflate(R.menu.action_menu, menu)
        true
      }

      def onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = {
        def get_checked_positions(f: List[Int] => Unit) = {
          f(checked_items.toList)

          mode.finish

          true
        }

        item.getItemId match {
        case R.id.action_copy =>
          get_checked_positions{ group_adapter cloneItemsAtPositions _ }
        case R.id.action_cut =>
          get_checked_positions{ group_adapter killItemsAtPositions _ }
        case _ => false
        }
      }

    }

    list_view.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL)

    list_view.setOnItemClickListener(listener)
    list_view.setOnTouchListener(listener)
    list_view.setOnDragListener(listener)
    list_view.setMultiChoiceModeListener(listener)
  }

  override def onResume {
    super.onResume

    group_adapter.notifyDataSetChanged
  }

  override def onStop {
    super.onStop

    // if we're the topmost activity (i.e. out intent_path is empty),
    // the the app to save its state
    if(intent_path.isEmpty)
      getApplication.asInstanceOf[MyApplication].save_state
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.action_bar, menu)

    val group_k = group_adapter.group_k

    menu.findItem(R.id.action_k).setTitle(group_k.toString + "x")

    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    def toast(text: String): Boolean = {
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show
      true
    }

    item.getItemId match {
      case R.id.action_play => toast ("Playing")


      case R.id.action_yank =>
        val intent = new Intent(this_activity, classOf[KillViewActivity])
        startActivityForResult(intent, ListActivity.KillBufferActivityRequestCode)
        true

      case R.id.action_k =>
        /*
        val v: Int = (Integer.parseInt(item.getTitle.toString.take(1)) + 1)
        val vs = v.toString
        Log.v("onOptionsItemSelected", "setting action bar to " + vs)
        item.setTitle(v.toString + "x")
        true
        */
        val dia = new DelayGroupCoeffDialogFragment(
          group_adapter.group_k,
          { k =>
            item.setTitle(k.toString + "x")
            group_adapter.set_group_k(k)
          }
        )

        dia.show(getFragmentManager, "coeff_dialog")
        true

        //toast("Modifying k")

      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onActivityResult (request: Int, result: Int, data: Intent) {
    if(request == ListActivity.KillBufferActivityRequestCode &&
       result >= Activity.RESULT_FIRST_USER)
    {

      val pos = result - Activity.RESULT_FIRST_USER

      group_adapter.yankKilledItem(pos)
    }
  }
}

