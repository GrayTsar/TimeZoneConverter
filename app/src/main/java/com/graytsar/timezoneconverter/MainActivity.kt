package com.graytsar.timezoneconverter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Filter
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.graytsar.timezoneconverter.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs

const val keyPreferenceTheme="preferenceTheme"
const val keyTheme="theme"

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private val viewModelMain:ViewModelMain by viewModels()

    private lateinit var textCurrentLongName: TextView
    private lateinit var textCurrentId: TextView
    private lateinit var textCurrentTime: TextView

    private lateinit var textDiffTime: TextView

    private lateinit var textSelectLongName: TextView
    private lateinit var textSelectId: TextView
    private lateinit var textSelectOffset: TextView

    private lateinit var timePicker:TimePicker

    private lateinit var recyclerTimeZone:RecyclerView

    private val list = ArrayList<ModelTimeZone>()
    private lateinit var adapterTimeZone:AdapterTimeZone

    var mMenu:Menu? = null

    var selectedTimeZone:ModelTimeZone? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = this.getSharedPreferences(keyPreferenceTheme, Context.MODE_PRIVATE)
        if(sharedPref.getBoolean(keyTheme, false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        AndroidThreeTen.init(this)

        initViews()

        initTimeZones()

        initObserver()

        ZonedDateTime.now().let { zoneDateTime ->
            viewModelMain.currentLongName.value = zoneDateTime.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
            viewModelMain.currentId.value = zoneDateTime.zone.id

            zoneDateTime.toLocalTime().let { localTime ->
                viewModelMain.currentTime.value = "${String.format("%02d", localTime.hour)}:${String.format("%02d", localTime.minute)} UTC${zoneDateTime.offset}"

                if(Build.VERSION.SDK_INT < 23){
                    timePicker.currentHour = localTime.hour
                    timePicker.currentMinute = localTime.minute
                } else {
                    timePicker.hour = localTime.hour
                    timePicker.minute = localTime.minute
                }
            }
        }

        timePicker.setOnTimeChangedListener{ picker,hour,min ->
            timeChangedListener(picker, hour, min)
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
                recyclerTimeZone.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                recyclerTimeZone.visibility = View.GONE
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.lightTheme -> {
                val sharedPref = this.getSharedPreferences(keyPreferenceTheme, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean(keyTheme, false)
                editor.apply()

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            R.id.darkTheme -> {
                val sharedPref = this.getSharedPreferences(keyPreferenceTheme, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean(keyTheme, true)
                editor.apply()

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        textCurrentLongName = binding.textCurrentLongName
        textCurrentId = binding.textCurrentId
        textCurrentTime = binding.textCurrentTime

        textDiffTime = binding.textDiffTime

        textSelectLongName = binding.textSelectLongName
        textSelectId = binding.textSelectId
        textSelectOffset = binding.textSelectOffset

        recyclerTimeZone = binding.recyclerTimeZone

        timePicker = binding.timePicker
        timePicker.setIs24HourView(true)

        adapterTimeZone = AdapterTimeZone(this)
        recyclerTimeZone.layoutManager = LinearLayoutManager(this)
        recyclerTimeZone.adapter = adapterTimeZone
    }

    private fun initTimeZones() {
        lifecycleScope.launch {
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
            adapterTimeZone.submitList(list)
        }
    }

    private fun initObserver() {
        viewModelMain.currentLongName.observe(this, androidx.lifecycle.Observer {
            textCurrentLongName.text = it
        })

        viewModelMain.currentId.observe(this, androidx.lifecycle.Observer {
            textCurrentId.text = it
        })

        viewModelMain.currentTime.observe(this, androidx.lifecycle.Observer {
            textCurrentTime.text = it
        })

        viewModelMain.diffTime.observe(this, androidx.lifecycle.Observer {
            textDiffTime.text = it
        })

        viewModelMain.selectedLongName.observe(this, androidx.lifecycle.Observer {
            textSelectLongName.text = it
        })

        viewModelMain.selectedId.observe(this, androidx.lifecycle.Observer {
            textSelectId.text = it
        })

        viewModelMain.selectedOffset.observe(this, androidx.lifecycle.Observer {
            textSelectOffset.text = it
        })
    }

    private fun timeChangedListener(picker: TimePicker, hour: Int, min: Int) {
        selectedTimeZone?.let { selectedTimeZone ->
            val zdt = ZonedDateTime.now()
            val zonedTime = ZonedDateTime.now(ZoneId.of(selectedTimeZone.id)).withHour(hour).withMinute(min)
            val s = zonedTime.toEpochSecond() - zdt.toEpochSecond()

            viewModelMain.currentLongName.value = zdt.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
            viewModelMain.currentId.value = zdt.zone.id
            //textCurrentTime.text = "${String.format("%02d", zdt.hour)}:${String.format("%02d", zdt.minute)} UTC${zdt.offset}"

            if(s >= 0){
                val h = TimeUnit.SECONDS.toHours(s)
                val m = TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s))
                viewModelMain.diffTime.value = "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                val z = zdt.plusMinutes(m).plusHours(h)
                viewModelMain.currentTime.value = "${String.format("%02d", z.hour)}:${String.format("%02d", z.minute)} UTC${z.offset}"
            } else {
                val h = 24 - abs(TimeUnit.SECONDS.toHours(s))
                val m = abs(TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)))
                viewModelMain.diffTime.value = "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                val z = zdt.plusMinutes(m).plusHours(h)
                viewModelMain.currentTime.value = "${String.format("%02d", z.hour)}:${String.format("%02d", z.minute)} UTC${z.offset}"
            }
        }
    }
}