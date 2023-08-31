package com.graytsar.timezoneconverter

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.graytsar.timezoneconverter.databinding.ActivityCronBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class CronActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCronBinding
    private val viewModel: ViewModelCron by viewModels()

    private lateinit var textCurrentLongName: TextView
    private lateinit var textCurrentId: TextView
    private lateinit var textCurrentTime: TextView

    private lateinit var textDiffTime: TextView

    private lateinit var textSelectLongName: TextView
    private lateinit var textSelectId: TextView
    private lateinit var textSelectOffset: TextView

    private lateinit var timePicker: TimePicker

    private lateinit var adapterTimeZone: AdapterTimeZone

    private var searchDialog: Dialog? = null
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCronBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        initViews()
        initObserver()

        timePicker.setOnTimeChangedListener(timeChangedListener)
        adapterTimeZone.submitList(viewModel.timeZones)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchView -> openSearch()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun initViews() {
        textCurrentLongName = binding.textCurrentLongName
        textCurrentId = binding.textCurrentId
        textCurrentTime = binding.textCurrentTime

        textDiffTime = binding.textDiffTime

        textSelectLongName = binding.textSelectLongName
        textSelectId = binding.textSelectId
        textSelectOffset = binding.textSelectOffset

        timePicker = binding.timePicker
        timePicker.setIs24HourView(true)

        adapterTimeZone = AdapterTimeZone(this, itemSelectListener)

        timePicker.hour = viewModel.localTime.hour
        timePicker.minute = viewModel.localTime.minute
    }

    private fun initObserver() {
        viewModel.currentLongName.observe(this) { textCurrentLongName.text = it }

        viewModel.currentId.observe(this) { textCurrentId.text = it }

        viewModel.currentTime.observe(this) { textCurrentTime.text = it }

        viewModel.diffTime.observe(this) { textDiffTime.text = it }

        viewModel.selectedLongName.observe(this) { textSelectLongName.text = it }

        viewModel.selectedId.observe(this) {
            textSelectId.text = it
            if (!it.isNullOrBlank()) timePicker.visibility = View.VISIBLE
        }

        viewModel.selectedOffset.observe(this) { textSelectOffset.text = it }

        viewModel.selectedHour.observe(this) { timePicker.hour = it }

        viewModel.selectedMinute.observe(this) { timePicker.minute = it }
    }

    private fun openSearch() {
        searchDialog = Dialog(this)
        searchDialog!!.setContentView(R.layout.searchable_dialog)
        searchDialog!!.window!!.setLayout(
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        val recycler = searchDialog!!.findViewById<RecyclerView>(R.id.recyclerSearch)
        val inputSearchQuery = searchDialog!!.findViewById<EditText>(R.id.inputSearch)

        val adapter = adapterTimeZone
        recycler.adapter = adapter
        setupSearch(adapter, inputSearchQuery)

        searchDialog!!.show()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch(adapterTimeZone: AdapterTimeZone, inputSearchQuery: EditText) {
        adapterTimeZone.submitList(viewModel.timeZones)
        searchJob = lifecycleScope.launch(Dispatchers.Main) {
            inputSearchQuery.afterTextChangedFlow()
                .debounce(600)
                .collectLatest {
                    val timeZone = if (it.isBlank()) {
                        viewModel.timeZones
                    } else {
                        viewModel.timeZones.filter { timeZone ->
                            timeZone.id.contains(it, true)
                                || timeZone.longName.contains(it, true)
                                || timeZone.offset.contains(it, true)
                                || timeZone.shortName.contains(it, true)
                        }
                    }
                    adapterTimeZone.submitList(timeZone)
                }
        }
    }

    private val itemSelectListener: (UITimeZone) -> Unit = {
        viewModel.selectedLongName.value = it.longName
        viewModel.selectedId.value = it.id
        viewModel.selectedOffset.value = it.offset
        viewModel.selectedTimeZone = it

        val zonedTime = ZonedDateTime.now(ZoneId.of(it.id)).toLocalTime()
        viewModel.selectedHour.value = zonedTime.hour
        viewModel.selectedMinute.value = zonedTime.minute

        val localTime = ZonedDateTime.now()
        viewModel.currentTime.value =
            "${String.format("%02d", localTime.hour)}:${
                String.format(
                    "%02d",
                    localTime.minute
                )
            } UTC${localTime.offset}"
        searchDialog?.dismiss()
        searchDialog = null
        searchJob?.cancel()
        searchJob = null
    }

    private val timeChangedListener = object : TimePicker.OnTimeChangedListener {
        override fun onTimeChanged(timepicker: TimePicker?, hour: Int, min: Int) {
            val selectedTimeZone = viewModel.selectedTimeZone ?: return

            val currentZonedDateTime = ZonedDateTime.now()
            val selectedZonedDateTime =
                ZonedDateTime.now(ZoneId.of(selectedTimeZone.id)).withHour(hour).withMinute(min)
            val epochSecond =
                selectedZonedDateTime.toEpochSecond() - currentZonedDateTime.toEpochSecond()

            if (epochSecond >= 0) {
                val h = TimeUnit.SECONDS.toHours(epochSecond)
                val m = TimeUnit.SECONDS.toMinutes(epochSecond) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.SECONDS.toHours(epochSecond)
                )
                viewModel.diffTime.value =
                    "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                currentZonedDateTime.plusMinutes(m).plusHours(h).let {
                    viewModel.currentTime.value = "${String.format("%02d", it.hour)}:${
                        String.format(
                            "%02d",
                            it.minute
                        )
                    } UTC${it.offset}"
                }
            } else {
                val s1 = 86400 + epochSecond

                val h = TimeUnit.SECONDS.toHours(s1)
                val m = TimeUnit.SECONDS.toMinutes(s1) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.SECONDS.toHours(s1)
                )
                viewModel.diffTime.value =
                    "${getString(R.string.timeIn)} ${String.format("%02d:%02d", h, m)}"

                currentZonedDateTime.plusMinutes(m).plusHours(h).let {
                    viewModel.currentTime.value = "${String.format("%02d", it.hour)}:${
                        String.format(
                            "%02d",
                            it.minute
                        )
                    } UTC${it.offset}"
                }
            }
        }
    }
}