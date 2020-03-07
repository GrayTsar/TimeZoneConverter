package com.graytsar.timezoneconverter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Filter
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.*
import org.threeten.bp.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val list = ArrayList<ModelTimeZone>()
    private lateinit var adapterTimeZone:AdapterTimeZone

    //this feels not right, but can not find anything better
    var mMenu:Menu? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidThreeTen.init(this)

        val l = ArrayList<String>()
        val lid = ArrayList<String>()
        ZoneId.getAvailableZoneIds().forEach {
            val long = ZoneId.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault())
            val offset = ZonedDateTime.now(ZoneId.of(it)).offset.toString()
            l.add(long)
            lid.add(it)

            list.add(ModelTimeZone(it, long, "UTC$offset"))
            //Log.d("DBG:", ZonedDateTime.now(ZoneId.of(it)).toString() + " " + ZonedDateTime.now(ZoneId.of(it)).offset.toString())
            //Log.d("DBG:", "$it $long")
        }
        list.sortBy {
            it.longName
        }

        val zoneDateTime = ZonedDateTime.now()
        textCurrentLongName.text = zoneDateTime.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())
        textCurrentId.text = zoneDateTime.zone.id
        textCurrentOffset.text = "UTC ${zoneDateTime.offset}"

        val localTime = zoneDateTime.toLocalTime()
        textCurrentTime.text = "${String.format("%02d", localTime.hour)}:${String.format("%02d", localTime.minute)}"

        adapterTimeZone = AdapterTimeZone(this)
        recyclerMain.layoutManager = LinearLayoutManager(this)
        recyclerMain.adapter = adapterTimeZone
        adapterTimeZone.submitList(list)

        val a = 0
    }

    val itemFilter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<ModelTimeZone>()

            if(constraint == null || constraint.isEmpty()){
                filteredList.addAll(list)
            } else {
                val defaultLocale = Locale.getDefault()

                val pattern = constraint.toString().toLowerCase(defaultLocale).trim()

                list.forEach{
                    if(
                        it.id.toLowerCase(defaultLocale).contains(pattern) ||
                        it.longName.toLowerCase(defaultLocale).contains(pattern) ||
                        it.offset.toLowerCase(defaultLocale).contains(pattern)
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