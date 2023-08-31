package com.graytsar.timezoneconverter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*

class ViewModelCron : ViewModel() {
    private val zoneDateTime: ZonedDateTime = ZonedDateTime.now()
    val localTime: LocalTime = zoneDateTime.toLocalTime()

    val currentLongName: MutableLiveData<String> = MutableLiveData<String>(
        zoneDateTime.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
    )
    val currentId: MutableLiveData<String> = MutableLiveData<String>(zoneDateTime.zone.id)
    val currentTime: MutableLiveData<String> = MutableLiveData<String>(
        "${String.format("%02d", localTime.hour)}:${
            String.format(
                "%02d",
                localTime.minute
            )
        } UTC${zoneDateTime.offset}"
    )

    val diffTime: MutableLiveData<String> = MutableLiveData<String>()

    val selectedLongName: MutableLiveData<String> = MutableLiveData<String>()
    val selectedId: MutableLiveData<String> = MutableLiveData<String>()
    val selectedOffset: MutableLiveData<String> = MutableLiveData<String>()

    val selectedHour: MutableLiveData<Int> = MutableLiveData<Int>()
    val selectedMinute: MutableLiveData<Int> = MutableLiveData<Int>()
    var selectedTimeZone: UITimeZone? = null


    val timeZones = ZoneId.getAvailableZoneIds().map {
        val long = ZoneId.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault())
        val offset = ZonedDateTime.now(ZoneId.of(it)).offset.toString()

        var shortName = ""
        long.split(" ").forEach { ch ->
            shortName += ch[0]
        }

        UITimeZone(it, long, "UTC$offset", shortName)
    }.sortedBy { it.longName }

}