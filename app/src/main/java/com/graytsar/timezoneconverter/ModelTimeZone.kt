package com.graytsar.timezoneconverter

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ModelTimeZone(val id:String, val longName:String, val offset:String){


    @SuppressLint("SetTextI18n")
    fun onClick(view: View){
        val ctx = view.context as MainActivity
        ctx.mMenu?.findItem(R.id.searchView)?.collapseActionView()

        ctx.textSelectLongDescription.text = longName
        ctx.textSelectId.text = id
        ctx.textSelectOffset.text = offset

        val zonedTime = ZonedDateTime.now(ZoneId.of(id)).toLocalTime()
        ctx.textSelectTime.text = "${String.format("%02d", zonedTime.hour)}:${String.format("%02d", zonedTime.minute)}"

        val localTime = ZonedDateTime.now().toLocalTime()
        ctx.textCurrentTime.text = "${String.format("%02d", localTime.hour)}:${String.format("%02d", localTime.minute)}"


        Log.d("DBG: ", "click")
    }
}