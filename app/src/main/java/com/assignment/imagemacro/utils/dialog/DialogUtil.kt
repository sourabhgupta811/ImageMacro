package com.assignment.imagemacro.utils.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.text.InputType
import android.widget.EditText
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder


object DialogUtil {
    fun showColorPickerDialog(context:Context,title:String, colorPickerListener: (color:Int)->Unit){
        ColorPickerDialogBuilder
            .with(context)
            .setTitle(title)
            .initialColor(Color.WHITE)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setPositiveButton("Ok") { d, lastSelectedColor, allColors ->
                colorPickerListener.invoke(lastSelectedColor)
            }.build().show()
    }

    fun showTextEditDialog(context: Context,title:String, textListener: (text:String)->Unit){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which -> textListener.invoke(input.text.toString()) })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }
}