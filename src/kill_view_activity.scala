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


import android.view._
import android.widget._

import model._


class KillViewActivity extends Activity
  with ActionBarActivity
{
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    set_action_bar_title(R.string.activity_name_kill_view)

    setContentView(R.layout.group_view)
    val list_view = findViewById(R.id.list_view).asInstanceOf[ListView]

    val group_adapter = {
      val buffer = getApplication.asInstanceOf[MyApplication].kill_buffer
      new GroupAdapter(this, buffer)
    }


    list_view.setAdapter(group_adapter)

    list_view.setOnItemClickListener(new AdapterView.OnItemClickListener {
      def onItemClick(parent: AdapterView[_], view: View, pos: Int, id: Long) {
        setResult(Activity.RESULT_FIRST_USER + pos)
        finish
      }
    })
  }
}

