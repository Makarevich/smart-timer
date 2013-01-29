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
import android.content.DialogInterface

import android.os.Bundle

import android.widget.NumberPicker

import android.util.Log

private class DelayGroupCoeffDialogFragment (
  start_value: Int,
  cb: Int => Unit
) extends DialogFragment {
  override def onCreateDialog(b: Bundle): Dialog = {
    val ctxt = getActivity

    // Log.v("onCreateDialog", "Creating number picker")

    val picker = new NumberPicker(ctxt)

    picker.setMinValue(1)
    picker.setMaxValue(10)

    picker.setValue(start_value)

    // Log.v("onCreateDialog", "Creating AlertDialog")

    new AlertDialog.Builder(ctxt)
      .setTitle(ctxt.getString(R.string.dialog_title_delay_group_coeff))
      .setView(picker)
      .setPositiveButton(R.string.dialog_button_ok,
        new DialogInterface.OnClickListener {
          def onClick(dialog: DialogInterface, which: Int) {
            cb(picker.getValue)
          }
        }
      )
      .setNegativeButton(R.string.dialog_button_cancel,
        new DialogInterface.OnClickListener {
          def onClick(dialog: DialogInterface, which: Int) {
          }
        }
      )
      .create
  }

}
