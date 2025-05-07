package com.pcloudy.bankingg

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateAxisValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date())
    }
}
