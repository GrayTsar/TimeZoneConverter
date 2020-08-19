package com.graytsar.timezoneconverter

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ModelTimeZone(val id:String, val longName:String, val offset:String, val shortName:String){

    @SuppressLint("SetTextI18n")
    fun onClick(view: View){
        val ctx = view.context as MainActivity
        ctx.mMenu?.findItem(R.id.searchView)?.collapseActionView()

        ctx.textSelectLongName.text = longName
        ctx.textSelectId.text = id
        ctx.textSelectOffset.text = offset
        ctx.selectedTimeZone = this

        val zonedTime = ZonedDateTime.now(ZoneId.of(id)).toLocalTime()

        //ctx.timePicker.hour = zonedTime.hour
        //ctx.timePicker.minute = zonedTime.minute

        if(Build.VERSION.SDK_INT < 23){
            ctx.timePicker.currentHour = zonedTime.hour
            ctx.timePicker.currentMinute = zonedTime.minute
        } else {
            ctx.timePicker.hour = zonedTime.hour
            ctx.timePicker.minute = zonedTime.minute
        }

        val localTime = ZonedDateTime.now()
        ctx.textCurrentTime.text = "${String.format("%02d", localTime.hour)}:${String.format("%02d", localTime.minute)} UTC${localTime.offset}"

        ctx.timePicker.visibility = View.VISIBLE
    }
}