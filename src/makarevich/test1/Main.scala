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

import android.view._

import android.widget._

private class DataList(ctxt: Activity) extends BaseAdapter {
  def getItem(i: Int) = null
  def getItemId(i: Int) = 0

  def getCount = 100

  def getView(i: Int, another: View, parent: ViewGroup): View = {
    val btn = ctxt.getLayoutInflater.inflate(R.layout.main_item, null)

    btn.asInstanceOf[Button].setText(i.toString)

    btn
  }

}

class Main extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    findViewById(R.id.list_view).asInstanceOf[ListView].setAdapter(new DataList(this))
  }
}
