package com.example.scylier.istudyspot.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.scylier.istudyspot.models.studyroom.SeatInfo

class SeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val seatPaint = Paint()
    private val textPaint = Paint()
    private val seatSize = 40f
    private val seatPadding = 10f
    private val rowCount = 10
    private val colCount = 8
    private var seats = listOf<SeatInfo>()
    private var selectedSeat: SeatInfo? = null
    private var onSeatClickListener: ((SeatInfo) -> Unit)? = null

    init {
        seatPaint.isAntiAlias = true
        textPaint.isAntiAlias = true
        textPaint.textSize = 12f
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun setSeats(seats: List<SeatInfo>) {
        this.seats = seats
        invalidate()
    }

    fun setOnSeatClickListener(listener: (SeatInfo) -> Unit) {
        this.onSeatClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制座位
        for (seat in seats) {
            val x = (seat.col - 1) * (seatSize + seatPadding) + seatPadding
            val y = (seat.row - 1) * (seatSize + seatPadding) + seatPadding

            // 根据座位状态设置颜色
            when (seat.status) {
                "available" -> seatPaint.color = Color.GREEN
                "booked" -> seatPaint.color = Color.YELLOW
                "occupied" -> seatPaint.color = Color.RED
                "unavailable" -> seatPaint.color = Color.GRAY
                else -> seatPaint.color = Color.GRAY
            }

            // 绘制座位矩形
            canvas.drawRect(x, y, x + seatSize, y + seatSize, seatPaint)

            // 绘制座位号
            textPaint.color = Color.BLACK
            canvas.drawText(seat.id, x + seatSize / 2, y + seatSize / 2 + 4, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // 计算点击的座位
            val col = (x / (seatSize + seatPadding)).toInt() + 1
            val row = (y / (seatSize + seatPadding)).toInt() + 1

            // 查找对应的座位
            val seat = seats.find { it.row == row && it.col == col }
            if (seat != null) {
                selectedSeat = seat
                onSeatClickListener?.invoke(seat)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ((seatSize + seatPadding) * colCount + seatPadding).toInt()
        val height = ((seatSize + seatPadding) * rowCount + seatPadding).toInt()
        setMeasuredDimension(width, height)
    }
}
