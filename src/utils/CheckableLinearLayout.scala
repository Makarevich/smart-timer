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


package makarevich

import android.view.View

import android.widget.LinearLayout
import android.widget.Checkable

import android.content.Context
import android.util.AttributeSet

import android.R

class CheckableLinearLayout(ctxt: Context, attr: AttributeSet)
  extends LinearLayout(ctxt, attr) with Checkable
{

  private[this] var flag: Boolean = false

  def isChecked: Boolean = flag
  def toggle = setChecked(!flag)
  def setChecked(new_flag: Boolean): Unit = {
    flag = new_flag
    refreshDrawableState
  }


  ///

  override protected def onCreateDrawableState(extraSpace: Int) = {
    val drawableState = super.onCreateDrawableState(extraSpace + 1)
    if (isChecked) View.mergeDrawableStates(drawableState, Array(R.attr.state_checked))
    drawableState
  }
}
