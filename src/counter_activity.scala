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

import model._


class CounterActivity extends Activity
  with IntentPathActivity
  with FakeLogger
{
  import CounterActivity._

  private val this_activity = this

  private var timer: Timer = null

  //
  // Lifecycle
  //

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val view = new AnimatedView(this)

    timer = new Timer(this, view)

    setContentView(view)
  }

  override def onResume {
    super.onResume

    log("onResume")

    timer.run
  }

  override def onPause {
    super.onPause

    log("onPause")

    timer.stop
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
  // AnimatedView
  //

  private class AnimatedView(activity: Activity)
    extends View(activity)
    with FakeLogger
  {
    private var n: Int = 0

    private def get_res_color(res_id: Int) = activity.getResources.getColor(res_id)

    override def onDraw(canvas: Canvas) {
      canvas.drawColor(get_res_color(R.color.orange))

      val paint = new Paint

      paint.setColor(get_res_color(R.color.black))
      paint.setTextSize(60)
      paint.setTextAlign(Paint.Align.CENTER)
      paint.setTypeface(Typeface.DEFAULT_BOLD);

      paint.setShadowLayer(5, 0, 0, get_res_color(R.color.white)); 

      canvas.drawText(n.toString, canvas.getWidth / 2, canvas.getHeight / 2, paint)
    }

    def update_number(n: Int) {
      this.n = n
      this.invalidate
    }
  }

  //////////////////////
  // Timer
  //

  private class Timer(activity: Activity, view: AnimatedView)
    extends Runnable
    with FakeLogger
  {
    private val handler: Handler = new Handler(activity.getMainLooper)

    private var n = 10

    def run {
      log("running")

      handler.postDelayed(this, 1000)

      //Toast.makeText(activity, "Tick", Toast.LENGTH_SHORT).show


      if(n == 0) {
        activity.finish
        return
      }

      n = n - 1

      view.update_number(n)
    }

    def stop {
      log("stopping")

      handler.removeCallbacks(this)
    }
  }

}
