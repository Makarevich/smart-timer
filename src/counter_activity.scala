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
import android.os.{Bundle,Handler}
import android.util.Log


import android.graphics._
import android.view._
import android.widget._

import annotation.tailrec

import model._


class CounterActivity extends Activity
  with IntentPathActivity
  with FakeLogger
{
  import CounterActivity._

  private val this_activity = this

  private var state: StateMachine = null

  //
  // Lifecycle
  //

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val view = new AnimatedView(this)

    state = new StateMachine(get_model_node, this, view)

    state.reset

    setContentView(view)
  }

  override def onResume {
    super.onResume

    log("onResume")

    state.timer.run
  }

  override def onPause {
    super.onPause

    log("onPause")

    state.timer.stop
  }

  //
  // Menu handling
  //

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.counter_action_bar, menu)

    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    def toast(text: String): Boolean = {
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show
      true
    }

    item.getItemId match {
      case R.id.action_rewind       => toast("Rewind")
      case R.id.action_replay       => toast("Replay")
      case R.id.action_fast_forward => toast("Fast forward")

      case _ => super.onOptionsItemSelected(item)
    }
  }

}

object CounterActivity {
  //////////////////////
  // StateMachine
  //

  private class StateMachine(
    group: DelayGroup,

    activity: Activity,
    view: AnimatedView
    )
  {
    val timer = new Timer(activity, this, view)

    //////////

    private case class StackFrame(index: Int, group: DelayGroup)

    /** This automaton's memory stack. */
    private var stack =
      collection.mutable.ArrayStack.apply(StackFrame(0, group))

    /** Offset of the current delay item. */
    private var offset: Int = 0

    //////////

    /** "Falls" forwards into the nearest valid state.
     * If it finds a valid state, sets up 'timer' and 'view' according
     * to that state. Otherwise, finishes the activity.
     */
    def reset {
      def setup_everything(item: DelayItem) {
        timer.n = item.amount
        view.color = item.color
      }

      @tailrec def search_valid_state {
        val group = stack.top.group

        if(offset >= group.items.size) {
          val prev_frame = stack.pop

          offset = prev_frame.index

          if(stack.isEmpty) {
            activity.finish
          } else {
            offset = offset + 1
            search_valid_state
          }
        } else {
          group.items(offset) match {
            case gr: DelayGroup =>
              stack.push(StackFrame(offset, gr))
              offset = 0
              search_valid_state

            case it: DelayItem =>
              setup_everything(it)
          }
        }
      }

      search_valid_state
    }

    /** Scans for the previous valid state.
     * If there is no previous valid state, invokes 'reset' to force
     * the machine info the first valid state.
     */
    def go_backwards {
    }

    /** Scans for the next valid state.
     * Basically, increments 'offset' and invokes 'reset'.
     */
    def go_forward {
      offset = offset + 1
      reset
    }
  }

  //////////////////////
  // Timer
  //

  private class Timer(
    activity: Activity,
    state: StateMachine,
    view: AnimatedView
  )
    extends Runnable
    with FakeLogger
  {
    private val handler: Handler = new Handler(activity.getMainLooper)

    private var _n: Int = 0

    def run {
      log("running")
      handler.postDelayed(this, 1000)

      if(_n == 0) {
        state.go_forward
        return
      }

      _n = _n - 1

      view.n = _n
    }

    def stop {
      log("stopping")

      handler.removeCallbacks(this)
    }

    def n = _n
    def n_= (value: Int) {
      _n = value
      view.n = _n
    }
  }

  //////////////////////
  // AnimatedView
  //

  private class AnimatedView(activity: Activity)
    extends View(activity)
    with FakeLogger
  {
    private var _n : Int = 0

    private var _c : Int = 0

    private def get_res_color(res_id: Int) = activity.getResources.getColor(res_id)

    //////

    override def onDraw(canvas: Canvas) {
      canvas.drawColor(_c)

      val paint = new Paint

      paint.setColor(get_res_color(R.color.black))
      paint.setTextSize(60)
      paint.setTextAlign(Paint.Align.CENTER)
      paint.setTypeface(Typeface.DEFAULT_BOLD);

      paint.setShadowLayer(5, 0, 0, get_res_color(R.color.white)); 

      canvas.drawText(_n.toString,
        canvas.getWidth / 2, canvas.getHeight / 2,
        paint)
    }

    def n = _n
    def n_= (n: Int) {
      this._n = n + 1
      this.invalidate
    }

    def color = _c
    def color_= (c: Int) {
      this._c = c
      this.invalidate
    }
  }

}
