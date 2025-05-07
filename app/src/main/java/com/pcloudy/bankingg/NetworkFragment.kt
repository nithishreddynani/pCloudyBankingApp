package com.pcloudy.bankingg

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentNetworkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NetworkFragment : Fragment() {
    private var _binding: FragmentNetworkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeNetworkCalls()
        fetchNetworkData()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = 45f
            }

            axisLeft.apply {
                axisMinimum = 0f
            }
        }
    }

    private fun fetchNetworkData() {
        lifecycleScope.launch {
            try {
                viewModel.simulateApiCall(
                    endpoint = "/api/network/stats",
                    operationType = MainViewModel.OperationType.NETWORK
                ) {
                    withContext(Dispatchers.Main) {
                        val currentCalls = viewModel.networkCalls.value ?: emptyList()
                        updateChart(currentCalls)
                        updateStatistics(currentCalls)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        binding.root,
                        "Network monitoring error: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun observeNetworkCalls() {
        viewModel.networkCalls.observe(viewLifecycleOwner) { calls ->
            if (calls.isNotEmpty()) {
                updateChart(calls)
                updateStatistics(calls)
            }
        }
    }

    private fun updateChart(calls: List<NetworkCall>) {
        val entries = calls.mapIndexed { index, call ->
            Entry(index.toFloat(), call.duration.toFloat())
        }

        val dataSet = LineDataSet(entries, "Response Times").apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple_500)
            circleColors = calls.map { call ->
                if (call.isSuccess) Color.GREEN else Color.RED
            }
            lineWidth = 2f
            setDrawValues(true)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            animateX(500)
            invalidate()
        }
    }

    private fun updateStatistics(calls: List<NetworkCall>) {
        val avgDuration = calls.sumOf { it.duration } / calls.size
        val successCount = calls.count { it.isSuccess }
        val successRate = (successCount * 100f / calls.size)

        binding.apply {
            textAvgResponse.text = "Average Response Time: ${avgDuration}ms"
            textSuccessRate.text = "Success Rate: ${successRate.toInt()}%"
            textTotalCalls.text = "Total Calls: ${calls.size}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}