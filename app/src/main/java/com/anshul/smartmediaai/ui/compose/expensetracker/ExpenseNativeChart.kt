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
fun ExpenseNativeChart(viewModel: ExpenseTrackerViewModel = hiltViewModel()){

    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
  AndroidView( modifier = Modifier.fillMaxWidth().height(200.dp), factory = { context ->
      PieChart(context).apply {
          description.isEnabled = false
          isDrawHoleEnabled = true
          setHoleColor(Color.WHITE)
          setUsePercentValues(true)
          setEntryLabelColor(Color.BLACK)
          legend.isEnabled = true
      }

  }, update = {
          chart ->
      val entries = state.nativeChart.map {
          PieEntry(it.value.toFloat(), it.key)
      }

      val dataSet = PieDataSet(entries, "Expenses").apply {
          colors = listOf(
              Color.rgb(244, 67, 54),   // red
              Color.rgb(33, 150, 243),  // blue
              Color.rgb(76, 175, 80),   // green
              Color.rgb(255, 193, 7)    // yellow
          )
          valueTextColor = Color.WHITE
          valueTextSize = 12f
      }

      chart.data = PieData(dataSet)
      chart.invalidate()
  })
}