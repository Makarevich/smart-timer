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

import android.app.Activity
import android.app.AlertDialog
import android.app.{Dialog,DialogFragment}
import android.content.DialogInterface

import android.os.Bundle

import android.view.View

import android.widget._

import android.util.Log


private trait CommonDialogFragment extends DialogFragment {
  protected case class DialogInfo(
    title_resource:   Int,
    view:             View,
    ok_cb:            () => Unit
  )

  protected def customize_dialog(ctxt: Activity): DialogInfo

  override def onCreateDialog(b: Bundle): Dialog = {
    val ctxt = getActivity

    // Log.v("onCreateDialog", "Creating number picker")
    // Log.v("onCreateDialog", "Creating AlertDialog")

    val info = customize_dialog(ctxt)

    new AlertDialog.Builder(ctxt)
      .setTitle(info.title_resource)
      .setView(info.view)
      .setPositiveButton(R.string.dialog_button_ok,
        new DialogInterface.OnClickListener {
          def onClick(dialog: DialogInterface, which: Int) {
            info.ok_cb()
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

private class DelayGroupCoeffDialogFragment (
  start_value: Int,
  cb: Int => Unit
) extends CommonDialogFragment {
  protected def customize_dialog(ctxt: Activity): DialogInfo = {
    val picker = new NumberPicker(ctxt)

    picker.setMinValue(1)
    picker.setMaxValue(ctxt.getResources.getInteger(R.integer.max_group_k))

    picker.setValue(start_value)

    DialogInfo(
      R.string.dialog_title_delay_group_coeff,
      picker,
      () => cb(picker.getValue)
    )
  }

}

private class DelayItemConfigDialogFragment (
  start_value:  Int,
  start_color:  Int,

  cb: (Int, Int) => Unit    // a (Delay, Color) pair
) extends CommonDialogFragment {
  protected def customize_dialog(ctxt: Activity): DialogInfo = {
    val view =
      ctxt.getLayoutInflater.inflate(R.layout.dialog_delay_item_config, null)

    val picker =
      view.findViewById(R.id.delay_picker).asInstanceOf[NumberPicker]

    picker.setMinValue(1)
    picker.setMaxValue(ctxt.getResources.getInteger(R.integer.max_delay))

    picker.setValue(start_value)

    val spinner =
      view.findViewById(R.id.delay_color_spinner).asInstanceOf[Spinner]

    val spinner_data = new ColorAdapter(ctxt)

    spinner.setAdapter(spinner_data)

    // select the item, whose color is equal to start_color
    0 until spinner_data.getCount map {
      i => (i, spinner_data.getItemId(i).toInt)
    } find ( _._2 == start_color ) foreach { pair =>
      spinner.setSelection(pair._1)
    }


    DialogInfo (
      R.string.dialog_title_delay_item_config,
      view,
      () => cb(picker.getValue, spinner.getSelectedItemId.toInt)
    )
  }
}

