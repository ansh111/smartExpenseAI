package com.anshul.smartmediaai.ui.compose.expensetracker

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


@Composable
fun ExpenseNativeChart(viewModel: ExpenseTrackerViewModel = hiltViewModel()) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.WHITE)
                setUsePercentValues(true)
                setDrawEntryLabels(false) // Hide slice labels (optional)
                legend.isEnabled = true

                // ✅ Configure legend placement and style
                legend.apply {
                    textColor = Color.BLACK
                    textSize = 12f
                    formSize = 10f
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
            }
        },
        update = { chart ->
            val entries = state.nativeChart.map {
                PieEntry(it.value.toFloat(), it.key) // key → category name
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    Color.rgb(244, 67, 54),   // red
                    Color.rgb(33, 150, 243),  // blue
                    Color.rgb(76, 175, 80),   // green
                    Color.rgb(255, 193, 7)    // yellow
                )
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                sliceSpace = 2f
            }

            chart.data = PieData(dataSet)

            // ✅ Refresh legend to reflect categories
            chart.legend.isEnabled = true
            chart.invalidate()
        }
    )
}
