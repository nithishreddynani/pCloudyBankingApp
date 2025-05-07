package com.pcloudy.bankingg

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pcloudy.bankingg.databinding.FragmentAnrBinding
import android.os.Process
import android.util.Log
import com.google.android.material.snackbar.Snackbar

class ANRFragment : Fragment() {
    private var _binding: FragmentAnrBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "ActivityManager"
    }

    private fun triggerANR() {
        // Log initial ANR state
        Log.e(TAG, """
            ANR in com.pcloudy.bankingg (com.pcloudy.bankingg/.MainActivity)
            PID: ${Process.myPid()}
            Reason: Input dispatching timed out (fe61093 com.pcloudy.bankingg/com.pcloudy.bankingg.MainActivity (server) is not responding. Waited 10005ms for MotionEvent)
            Parent: com.pcloudy.bankingg/.MainActivity
            Frozen: false
            Load: 11.93 / 11.62 / 11.76
        """.trimIndent())

        // Create intensive loop to block main thread
        var counter = 0L
        val startTime = System.currentTimeMillis()

        // This loop will block the main thread and trigger real ANR
        while (true) {
            counter++
            if (counter % 1_000_000 == 0L) {
                // Create memory pressure with array allocations
                val list = ArrayList<ByteArray>(100)
                for (i in 1..100) {
                    list.add(ByteArray(2048 * 2048)) // Allocate 1MB each time
                }

                // Log CPU usage
//                Log.e(TAG, """
//                    CPU usage from 0ms to ${System.currentTimeMillis() - startTime}ms later:
//                    104% ${Process.myPid()}/com.pcloudy.bankingg: 103% user + 1% kernel / faults: 6313 minor 16 major
//                    29% 5206/system_server: 12% user + 17% kernel / faults: 8807 minor 98 major
//                    8.4% 5564/com.android.systemui: 5.9% user + 2.4% kernel / faults: 6220 minor 40 major
//                """.trimIndent())
            }

            // Keep running until system kills the app
            if (counter > Long.MAX_VALUE - 1000) {
                counter = 0
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.more.setOnClickListener {
            triggerANR()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}