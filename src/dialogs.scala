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

import android.app.AlertDialog
import android.app.{Dialog,DialogFragment}

import android.os.Bundle

import android.widget.NumberPicker

import android.util.Log

private class DelayGroupCoeffDialogFragment extends DialogFragment {
  override def onCreateDialog(b: Bundle): Dialog = {
    val ctxt = getActivity

    Log.v("onCreateDialog", "Creating number picker")

    val picker = new NumberPicker(ctxt)

    picker.setMinValue(0)
    picker.setMaxValue(10)


    Log.v("onCreateDialog", "Creating AlertDialog")

    new AlertDialog.Builder(ctxt)
      .setTitle(ctxt.getString(R.string.dialog_title_delay_group_coeff))
      .setView(picker)
      .create
  }

}
