package io.github.turskyi.travellingpro.utils

import com.github.mikephil.charting.formatter.ValueFormatter

class IntFormatter: ValueFormatter() {
    override fun getFormattedValue(value: Float)= value.toInt().toString()
}