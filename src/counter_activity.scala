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
import android.content.{Context,Intent}
import android.os.{Bundle,Handler,Vibrator}
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

  private var state: ConcreteStateMachine = null

  //
  // Lifecycle
  //

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val view = new AnimatedView(this)

    state = new ConcreteStateMachine(get_model_node, this, view)

    state.prepare

    setContentView(view)

    view.setOnTouchListener {
      val sens = getResources.getInteger(R.integer.counter_view_sensitivity)

      new TimerGestureController(state.timer, sens) with Logger
    }
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
    /*
    def toast(text: String): Boolean = {
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show
      true
    }
    */
    def ok(action: => Unit) = {
      action
      true
    }

    item.getItemId match {
      case R.id.action_replay       => ok(state.reset)
      case R.id.action_rewind       => ok(state.go_backwards)
      case R.id.action_fast_forward => ok(state.go_forward)

      case _ => super.onOptionsItemSelected(item)
    }
  }

}

object CounterActivity {
  //////////////////////
  // StateMachine
  //

  private class ConcreteStateMachine(
    group: DelayGroup,
    activity: Activity,
    view: AnimatedView
  ) extends AbstractDelayStateMachine(group) {
    val timer = new Timer(activity, this, view)

    def update_state(item: DelayItem) {
      timer.n = item.amount
      view.color = item.color
    }

    def state_failure {
      activity.finish
    }
  }

  private abstract class AbstractDelayStateMachine(group: DelayGroup) {

    //
    // Abstract methods
    //

    def update_state(item: DelayItem): Unit
    def state_failure: Unit

    //////////

    //
    // StackFrame
    //

    private object StackFrame {
      def apply(group: DelayGroup): StackFrame = StackFrame(group, group.k, 0)
    }

    private case class StackFrame (
      val group: DelayGroup,
      var k: Int,
      var index: Int
    ) {
      private var subframe: StackFrame = null

      /**
       * The following functions return "false",
       * if the frame becomes invalid.
       */

      /** "Falls" into for next valid state. */
      @tailrec
      final def prepare: Boolean = {
        if(index >= group.items.size) {
          assert(k > 0)
          k = k - 1
          if(k == 0) {
            return false
          }
          index = 0
          this.prepare
        } else {
          group.items(index) match {
            case it: DelayItem => true

            case gr: DelayGroup =>
              assert(subframe == null)
              subframe = StackFrame(gr)
              if(!subframe.prepare_aux) {
                subframe = null
                index = index + 1
                this.prepare
              } else true
          }
        }
      }

      /** Searches for next valid state. */
      final def next: Boolean = {
        if(subframe != null) {
          if(subframe.next) {
            return true
          } else {
            subframe = null
          }
        }

        index = index + 1
        return this.prepare
      }

      /** Searches for previous valid state. */
      @tailrec
      final def prev: Boolean = {
        if(subframe != null) {
          if(subframe.prev_aux){
            return true
          } else {
            subframe = null
          }
        }

        index = index - 1

        if(index < 0) {
          k = k + 1

          if(k > group.k) return false

          index = group.items.size

          this.prev
        } else {
          group.items(index) match {
            case it: DelayItem => true

            case gr: DelayGroup =>
              assert(subframe == null)
              subframe = StackFrame(gr, 1, gr.items.size)
              this.prev
          }
        }
      }

      /** Recursively computes current item of the stack. */
      final def item: DelayItem =
        if(subframe != null) {
          subframe.item
        } else {
          group.items(index).asInstanceOf[DelayItem]
        }

      /** Resets this frame to something suitable for 'prepare'. */
      final def hard_reset {
        assert(subframe == null)
        k = group.k
        index = 0
      }

      final def print_trace {
        Log.v("StackFrame", k.toString + "x" + index.toString)
        if(subframe != null) subframe.print_trace
      }



      /** Auxilliary helpers to assist tailrec methods. */
      private def prev_aux = prev
      private def prepare_aux = prepare
    }

    /** This is automaton's memory stack. */
    private val stack = StackFrame(group)

    //////////

    private def set_up_valid_state (advance_result: Boolean) (on_failure: => Unit) {
      if(advance_result) {
        update_state(stack.item)
      } else on_failure
    }

    /** Initializes the state machine. May invoke state_failure. */
    def prepare {
      set_up_valid_state (stack.prepare) {
        state_failure
      }
    }

    /** Efectively, updates current state. */
    def reset {
      update_state(stack.item)
    }

    /** Scans for the previous valid state.
     * If there is no previous valid state, invokes 'reset' to force
     * the machine info the first valid state.
     */
    def go_backwards {
      set_up_valid_state (stack.prev) {
        stack.hard_reset
        stack.prepare
      }
    }

    /** Scans for the next valid state.
     * Basically, increments 'offset' and invokes 'reset'.
     */
    def go_forward {
      set_up_valid_state (stack.next) {
        state_failure
      }

      // stack.print_trace
    }
  }

  //////////////////////
  // Timer
  //

  private class Timer(
    activity: Activity,
    state: AbstractDelayStateMachine,
    view: AnimatedView
  )
    extends Runnable
    with FakeLogger
  {
    private val handler: Handler =
      new Handler(activity.getMainLooper)

    private val vibrator: Vibrator = activity
      .getSystemService(Context.VIBRATOR_SERVICE).asInstanceOf[Vibrator]

    private val vibra_ticks = Set[Int](1, 2, 3, 5, 8, 11)

    private var _n: Int = 0

    def run {
      log("running")
      handler.postDelayed(this, 1000)

      _n = _n - 1

      if(_n <= 0) {
        state.go_forward
        return
      }

      view.n = _n

      if(vibra_ticks.contains(_n)) {
        vibrator.vibrate(500)
      }
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
      this._n = n
      this.invalidate
    }

    def color = _c
    def color_= (c: Int) {
      this._c = c
      this.invalidate
    }
  }

  private class TimerGestureController(timer: Timer, sens: Int)
    extends FakeLogger
    with View.OnTouchListener
  {
    private var px: Float = 0
    private var py: Float = 0
    private var timer_n: Float = 0
    

    def onTouch(v: View, event: MotionEvent): Boolean = {
      def log_coords(prefix: String) = {
        log(prefix + ": " + event.getX.toString + " " + event.getY.toString)
        true
      }

      def memorize_coords {
        px = event.getX
        py = event.getY

        timer_n = timer.n
      }

      event.getAction match {
        case MotionEvent.ACTION_DOWN =>
          memorize_coords
          log_coords("ACTION_DOWN")

        case MotionEvent.ACTION_MOVE =>
          val delta: Float = (px - event.getX) + (py - event.getY)

          timer_n = timer_n + (delta / sens)

          log("Timer n: " + timer_n.toString)

          timer.n = timer_n.toInt

          memorize_coords
          log_coords("ACTION_MOVE")

        case MotionEvent.ACTION_UP   => log_coords("ACTION_UP  ")

        case _ => false
      }

    }
  }

}
