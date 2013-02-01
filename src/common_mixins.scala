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
import android.os.Bundle

import android.util.Log

import model._

import annotation.tailrec



/**
 * Fetches intent_path onCreate and provides get_model_node method
 * that searches for current model node.
 */
trait IntentPathActivity extends Activity {
  protected var intent_path: Array[Byte] = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    intent_path = getIntent.getByteArrayExtra(
      ListActivity.IntentExtraModelPathByteArray
    )

    if(intent_path == null) intent_path = Array.empty[Byte]
  }

  /**
   * NOTE: it halts the activity with finish(), if something fails
   */
  protected def get_model_node: DelayGroup = {
    Log.v("IntentPathActivity",
      "intent_path: " + intent_path.mkString(","))

    val model = getApplication.asInstanceOf[MyApplication].model

    class InvalidModelPath extends Throwable

    try {
      if(intent_path.isEmpty) model else {
        @tailrec def descend_model(path: List[Byte], group: DelayGroup)
          : DelayGroup =
        {
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
      finish();
      model
    }
  }
}



/**
 * A convenience trait to operate with the action bar.
 */
trait ActionBarActivity extends Activity {
  protected def set_action_bar_title(t: Int) {
    val bar = getActionBar
    
    bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE)
    bar.setTitle(t)
  }
}


/**
 * A loggin mixins
 */

trait FakeLogger {
  def log(msg: String) {
  }
}

trait Logger extends FakeLogger {
  override def log(msg: String) {
    Log.v(this.getClass.getName, msg)
  }
}

