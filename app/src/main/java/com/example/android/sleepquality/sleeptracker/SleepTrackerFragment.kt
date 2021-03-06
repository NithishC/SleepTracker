package com.example.android.sleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.sleepquality.R
import com.example.android.sleepquality.database.SleepDatabase
import com.example.android.sleepquality.databinding.FragmentSleepTrackerBinding

class SleepTrackerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        val sleepTrackerViewModel =
                ViewModelProviders.of(
                        this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        val adapter = SleepNightAdapter(SleepNightListener {
            sleepTrackerViewModel.showText()

        })
        binding.sleepList.adapter = adapter

        val manager = GridLayoutManager(activity, 3)
        binding.sleepList.layoutManager = manager
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        binding.lifecycleOwner = this
        sleepTrackerViewModel.allnights.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        sleepTrackerViewModel.navigateToSleep.observe(this, Observer { night ->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment2(night.nightId)
                )
                sleepTrackerViewModel.donenavigate()
            }
        })


        return binding.root
    }
}
