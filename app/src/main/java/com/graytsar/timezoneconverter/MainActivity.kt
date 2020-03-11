package com.graytsar.timezoneconverter

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Filter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private val list = ArrayList<ModelTimeZone>()
    private lateinit var adapterTimeZone:AdapterTimeZone

    //this feels not right, but can not find anything better
    var mMenu:Menu? = null
    var sTimeZone:ModelTimeZone? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidThreeTen.init(this)

        ZoneId.getAvailableZoneIds().forEach {
            val long = ZoneId.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault())
            val offset = ZonedDateTime.now(ZoneId.of(it)).offset.toString()

            var shortName = ""
            long.split(" ").forEach {ch ->
                shortName += ch[0]
            }

            list.add(ModelTimeZone(it, long, "UTC$offset", shortName))
        }
        list.sortBy {
            it.longName
        }

        val zoneDateTime = ZonedDateTime.now()
        textCurrentLongName.text = zoneDateTime.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
        textCurrentId.text = zoneDateTime.zone.id

        val localTime = zoneDateTime.toLocalTime()
        textCurrentTime.text = "${String.format("%02d", localTime.hour)}:${String.format("%02d", localTime.minute)} UTC${zoneDateTime.offset}"

        timePicker.setIs24HourView(true)

        if(Build.VERSION.SDK_INT < 23){
            timePicker.currentHour = localTime.hour
            timePicker.currentMinute = localTime.minute
        } else {
            timePicker.hour = localTime.hour
            timePicker.minute = localTime.minute
        }

        adapterTimeZone = AdapterTimeZone(this)
        recyclerMain.layoutManager = LinearLayoutManager(this)
        recyclerMain.adapter = adapterTimeZone
        adapterTimeZone.submitList(list)

        timePicker.setOnTimeChangedListener{ picker,hour,min ->
            if(sTimeZone != null){
                val zdt = ZonedDateTime.now()
                val zonedTime = ZonedDateTime.now(ZoneId.of(sTimeZone!!.id)).withHour(hour).withMinute(min)
                val s = zonedTime.toEpochSecond() - zdt.toEpochSecond()

                textCurrentLongName.text = zdt.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
                textCurrentId.text = zdt.zone.id
                //textCurrentTime.text = "${String.format("%02d", zdt.hour)}:${String.format("%02d", zdt.minute)} UTC${zdt.offset}"

                if(s >= 0){
                    val h = TimeUnit.SECONDS.toHours(s)
                    val m = TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s))
                    textDiffTime.text = "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                    val z = zdt.plusMinutes(m).plusHours(h)
                    textCurrentTime.text = "${String.format("%02d", z.hour)}:${String.format("%02d", z.minute)} UTC${z.offset}"
                } else {
                    val h = 24 - abs(TimeUnit.SECONDS.toHours(s))
                    val m = abs(TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)))
                    textDiffTime.text = "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                    val z = zdt.plusMinutes(m).plusHours(h)
                    textCurrentTime.text = "${String.format("%02d", z.hour)}:${String.format("%02d", z.minute)} UTC${z.offset}"
                }
            }
        }
    }

    val itemFilter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<ModelTimeZone>()

            if(constraint == null){
                filteredList.addAll(list)
            } else {
                val defaultLocale = Locale.getDefault()

                val pattern = constraint.toString().toLowerCase(defaultLocale).trim()

                list.forEach{
                    if
                    (
                        it.id.toLowerCase(defaultLocale).contains(pattern) ||
                        it.longName.toLowerCase(defaultLocale).contains(pattern) ||
                        it.offset.toLowerCase(defaultLocale).contains(pattern) ||
                        it.shortName.toLowerCase(defaultLocale).contains(pattern)
                    ){
                        filteredList.add(it)
                    }
                }
            }

            val result = FilterResults()
            result.values = filteredList
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if(results!!.values is ArrayList<*>){
                adapterTimeZone.submitList(results.values as ArrayList<ModelTimeZone>)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        mMenu = menu

        val item = menu.findItem(R.id.searchView)
        val searchView = item.actionView as SearchView

        item.setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                recyclerMain.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                recyclerMain.visibility = View.GONE
                return true
            }
        })

        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                itemFilter.filter(newText)
                return false
            }
        })

        return true
    }
}