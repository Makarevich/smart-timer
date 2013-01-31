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
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log


import android.view._
import android.widget._

import model._


class CounterActivity extends Activity
  with IntentPathActivity
{
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(new View(this) {
      override def onDraw(canvas: Canvas) {
        canvas.drawColor(getResources.getColor(R.color.orange))
      }
    })

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
}

