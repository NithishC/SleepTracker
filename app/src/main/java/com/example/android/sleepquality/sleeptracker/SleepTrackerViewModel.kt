package com.example.android.sleepquality.sleeptracker

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.sleepquality.database.SleepDatabaseDao
import com.example.android.sleepquality.database.SleepNight
import com.example.android.sleepquality.formatNights
import kotlinx.coroutines.*


class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight>()
    val allnights = database.getAllNights()
    val nightString = Transformations.map(allnights) { nights ->
        formatNights(nights, application.resources)
    }
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }
    val clearButtonVisible = Transformations.map(allnights) {
        it?.isNotEmpty()
    }


    private val _navigateToSleep = MutableLiveData<SleepNight>()
    val navigateToSleep: LiveData<SleepNight>
        get() = _navigateToSleep

    fun donenavigate() {
        _navigateToSleep.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleep.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null

        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    fun showText(nightId: Long) {
        Toast.makeText(getApplication(), "Nothing to show on ${nightId}", Toast.LENGTH_SHORT).show()
    }

    fun showTextClear() {
        onClear()
        Toast.makeText(getApplication(), "All Data has been cleared", Toast.LENGTH_SHORT).show()
    }


}

