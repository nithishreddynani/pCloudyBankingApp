package com.pcloudy.bankingg

import com.github.mikephil.charting.formatter.ValueFormatter

class DurationAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()}ms"
    }
}