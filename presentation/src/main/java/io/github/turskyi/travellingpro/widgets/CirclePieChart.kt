package io.github.turskyi.travellingpro.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.home.view.HomeActivityView
import io.github.turskyi.travellingpro.features.home.view.ui.ShareListBottomSheetFragment
import io.github.turskyi.travellingpro.features.traveller.view.TravellerActivityView
import io.github.turskyi.travellingpro.utils.IntFormatter
import io.github.turskyi.travellingpro.utils.extensions.convertPictureToSpannableString
import io.github.turskyi.travellingpro.utils.extensions.getAppCompatActivity
import io.github.turskyi.travellingpro.utils.extensions.getFragmentActivity
import io.github.turskyi.travellingpro.utils.extensions.showReportDialog
import io.github.turskyi.travellingpro.utils.extensions.spToPix
import io.github.turskyi.travellingpro.utils.extensions.toastLong

/**
 * This custom view is a convenient way to encapsulate all logic related to pie chart to a separate
 * class
 */
class CirclePieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : com.github.mikephil.charting.charts.PieChart(context, attrs, defStyleAttr),
    OnChartGestureListener {

    private var isCenterPieChartEnabled = false
    private var homeActivityViewListener: HomeActivityView? = null
    private var travellerActivityViewListener: TravellerActivityView? = null

    init {
        setNoDataText(null)
        // init callbacks for Home activity and Traveller activity
        try {
            when (context) {
                is HomeActivityView -> {
                    homeActivityViewListener = context
                }
                is TravellerActivityView -> {
                    travellerActivityViewListener = context
                }
                else -> {
                    context.showReportDialog()
                }
            }
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            toastLong(
                castException.localizedMessage ?: castException.stackTraceToString()
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        homeActivityViewListener = null
        travellerActivityViewListener = null
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) { // nothing has to be here
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) { // nothing has to be here
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        // hide info icon
        context.getAppCompatActivity()?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        // hide sync icon
        val toolbar: androidx.appcompat.widget.Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.menu.clear()
        //---------------------
        val bottomSheet = ShareListBottomSheetFragment()
        context.getFragmentActivity()?.supportFragmentManager?.let { fragmentManager ->
            bottomSheet.show(fragmentManager, null)
        }
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        // nothing has to be here
    }

    override fun onChartSingleTapped(me: MotionEvent) {
        if (homeActivityViewListener != null) {
            when (isDrawHoleEnabled) {
                false -> {
                    isDrawHoleEnabled = true
                    homeActivityViewListener!!.showTitleWithOnlyCountries()
                    centerText = context.convertPictureToSpannableString(
                        R.drawable.pic_pie_chart_center,
                    )
                    isCenterPieChartEnabled = true
                }
                true -> {
                    centerText = ""
                    isDrawHoleEnabled = false
                    isCenterPieChartEnabled = false
                    homeActivityViewListener!!.setTitle()
                }
            }
        } else if (travellerActivityViewListener != null) {
            when (isDrawHoleEnabled) {
                false -> {
                    isDrawHoleEnabled = true
                    travellerActivityViewListener!!.showTitleWithOnlyCountries()
                }
                true -> {
                    centerText = ""
                    isDrawHoleEnabled = false
                    isCenterPieChartEnabled = false
                    travellerActivityViewListener!!.initTitle()
                }
            }
        }
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) { // nothing has to be here
    }

    override fun onChartScale(
        me: MotionEvent?,
        scaleX: Float,
        scaleY: Float
    ) {// nothing has to be here
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
// nothing has to be here
    }

    fun initPieChart() {
        description.isEnabled = false

        // work around instead of click listener
        onChartGestureListener = this

        if (!isCenterPieChartEnabled) {
            // remove hole inside
            isDrawHoleEnabled = false
        }

        // removes color squares
        legend.isEnabled = false

        // rotate the pie chart to 45 degrees
        rotationAngle = -10f

        // set radius of an open eye
        holeRadius = 78F
        if (travellerActivityViewListener != null) {
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            travellerActivityViewListener!!.showTitleWithOnlyCountries()
            isCenterPieChartEnabled = true
        }
    }

    fun createPieChartWith(visitedCountries: List<VisitedCountry>, notVisitedCount: Float) {
        val entries: MutableList<PieEntry> = mutableListOf()
        entries.add(PieEntry(visitedCountries.size.toFloat()))
        entries.add(PieEntry(notVisitedCount))
        val pieChartColors: MutableList<Int> = mutableListOf()
        pieChartColors.add(ContextCompat.getColor(context, R.color.colorAccent))
        pieChartColors.add(ContextCompat.getColor(context, R.color.colorBrightBlue))

        val dataSet = PieDataSet(entries, null)
        dataSet.colors = pieChartColors

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(IntFormatter())
        pieData.setValueTextSize(context.spToPix(R.dimen.caption))
        pieData.setValueTextColor(Color.WHITE)

        data = pieData
        // updates data in pieChart every time
        invalidate()
    }

    // nice and smooth animation of a chart
    fun animatePieChart() = animateY(1500)
}