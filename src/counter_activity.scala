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
{
  import CounterActivity.AnimatedView

  private val this_activity = this

  private var view: AnimatedView = null

  private def log(s: String) {
    //Log.v("CounterActivity",s)
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    view = new AnimatedView(this)

    setContentView(view)

    /*
    intent_path = getIntent.getByteArrayExtra(
      ListActivity.IntentExtraModelPathByteArray
    )

    if(intent_path == null) intent_path = Array.empty[Byte]

    val model_node = {
      val cand_model =
        getApplication.asInstanceOf[MyApplication]
        .find_model_node(intent_path)

      if(cand_model == null) return finish(); else cand_model
    }

    {
      val bar = getActionBar
      
      bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE)
      bar.setTitle(R.string.activity_name_kill_view)
    }
    */
  }

  override def onResume {
    super.onResume

    log("onResume")

    view.run
  }

  override def onPause {
    super.onPause

    log("onPause")

    view.stop
  }

}

object CounterActivity {
  private class AnimatedView(activity: Activity)
    extends View(activity)
    with Runnable
  {
    private val handler: Handler = new Handler(activity.getMainLooper)

    private var n = 10

    private def log(s: String) {
      //Log.v("CounterActivity.AnimatedView",s)
    }

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

    def run {
      log("running")

      handler.postDelayed(this, 1000)

      //Toast.makeText(activity, "Tick", Toast.LENGTH_SHORT).show


      if(n == 0) {
        activity.finish
        return
      }

      n = n - 1

      invalidate
    }

    def stop {
      log("stopping")

      handler.removeCallbacks(this)
    }
  }

}
