// app/src/main/java/com/ggetters/app/ui/central/views/components/PitchView.kt
package com.ggetters.app.ui.central.views.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ggetters.app.R
import com.ggetters.app.data.model.RosterPlayer

class PitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Callbacks
    var onPositionClick: ((String) -> Unit)? = null
    var onPlayerDrop: ((String, RosterPlayer) -> Unit)? = null
    var onPlayerRemove: ((String) -> Unit)? = null

    // Paint objects for drawing
    private val pitchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val positionPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Formation and positions
    private var currentFormation = "4-3-3"
    private val positionedPlayers = mutableMapOf<String, RosterPlayer>()
    private val formationPositions = mutableMapOf<String, PointF>()

    // Colors
    private val pitchColor = ContextCompat.getColor(context, R.color.success)
    private val lineColor = Color.WHITE
    private val playerColor = ContextCompat.getColor(context, R.color.primary)
    private val positionColor = ContextCompat.getColor(context, R.color.surface_container)

    // Dimensions
    private var pitchWidth = 0f
    private var pitchHeight = 0f
    private var margin = 40f
    private val playerRadius = 30f
    private val positionRadius = 25f

    init {
        setupPaints()
        setupFormationPositions()
        setupDragListener()
    }

    private fun setupPaints() {
        pitchPaint.color = pitchColor
        pitchPaint.style = Paint.Style.FILL

        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 4f

        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        playerPaint.color = playerColor
        playerPaint.style = Paint.Style.FILL

        positionPaint.color = positionColor
        positionPaint.style = Paint.Style.FILL
        positionPaint.alpha = 180
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pitchWidth = w - (margin * 2)
        pitchHeight = h - (margin * 2)
        setupFormationPositions()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPitch(canvas)
        drawPitchLines(canvas)
        drawFormationPositions(canvas)
        drawPositionedPlayers(canvas)
    }

    private fun drawPitch(canvas: Canvas) {
        val rect = RectF(margin, margin, margin + pitchWidth, margin + pitchHeight)
        canvas.drawRoundRect(rect, 20f, 20f, pitchPaint)
    }

    private fun drawPitchLines(canvas: Canvas) {
        val left = margin
        val top = margin
        val right = margin + pitchWidth
        val bottom = margin + pitchHeight
        val centerX = margin + pitchWidth / 2
        val centerY = margin + pitchHeight / 2

        canvas.drawRoundRect(RectF(left, top, right, bottom), 20f, 20f, linePaint)
        canvas.drawLine(centerX, top, centerX, bottom, linePaint)
        canvas.drawCircle(centerX, centerY, 60f, linePaint)

        val goalWidth = pitchWidth * 0.4f
        val goalHeight = pitchHeight * 0.15f
        val goalLeft = centerX - goalWidth / 2
        val goalRight = centerX + goalWidth / 2
        canvas.drawRect(goalLeft, top, goalRight, top + goalHeight, linePaint)
        canvas.drawRect(goalLeft, bottom - goalHeight, goalRight, bottom, linePaint)

        val penaltyWidth = pitchWidth * 0.6f
        val penaltyHeight = pitchHeight * 0.25f
        val penaltyLeft = centerX - penaltyWidth / 2
        val penaltyRight = centerX + penaltyWidth / 2
        canvas.drawRect(penaltyLeft, top, penaltyRight, top + penaltyHeight, linePaint)
        canvas.drawRect(penaltyLeft, bottom - penaltyHeight, penaltyRight, bottom, linePaint)
    }

    private fun drawFormationPositions(canvas: Canvas) {
        formationPositions.forEach { (position, point) ->
            if (!positionedPlayers.containsKey(position)) {
                canvas.drawCircle(point.x, point.y, positionRadius, positionPaint)
                val labelPaint = Paint(textPaint).apply {
                    textSize = 16f
                    color = ContextCompat.getColor(context, R.color.on_surface_variant)
                }
                canvas.drawText(position, point.x, point.y + 6f, labelPaint)
            }
        }
    }

    private fun drawPositionedPlayers(canvas: Canvas) {
        positionedPlayers.forEach { (position, player) ->
            formationPositions[position]?.let { point ->
                canvas.drawCircle(point.x, point.y, playerRadius, playerPaint)
                canvas.drawText(player.jerseyNumber.toString(), point.x, point.y + 8f, textPaint)

                val namePaint = Paint(textPaint).apply {
                    textSize = 12f
                    color = Color.WHITE
                }
                canvas.drawText(
                    player.playerName.split(" ").last(),
                    point.x,
                    point.y + playerRadius + 20f,
                    namePaint
                )
            }
        }
    }

    fun setFormation(formation: String) {
        currentFormation = formation
        setupFormationPositions()
    }

    fun updatePlayerPosition(position: String, player: RosterPlayer) {
        positionedPlayers[position] = player
        invalidate()
    }

    fun removePlayerFromPosition(position: String) {
        positionedPlayers.remove(position)
        invalidate()
    }

    fun clearAllPositions() {
        positionedPlayers.clear()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            getPositionAtPoint(event.x, event.y)?.let { position ->
                onPositionClick?.invoke(position)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getPositionAtPoint(x: Float, y: Float): String? {
        formationPositions.forEach { (position, point) ->
            val distance = kotlin.math.sqrt(
                ((x - point.x) * (x - point.x) + (y - point.y) * (y - point.y)).toDouble()
            ).toFloat()
            if (distance <= playerRadius + 10f) return position
        }
        return null
    }

    private fun setupDragListener() {
        setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DROP -> {
                    val player = event.localState as? RosterPlayer
                    val position = player?.let { getPositionAtPoint(event.x, event.y) }
                    if (player != null && position != null) {
                        onPlayerDrop?.invoke(position, player)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    invalidate()
                    true
                }
                else -> true
            }
        }
    }






    private fun setupFormationPositions() {
            if (pitchWidth <= 0 || pitchHeight <= 0) return

            formationPositions.clear()

            when (currentFormation) {
                "4-3-3" -> setup433Formation()
                "4-4-2" -> setup442Formation()
                "3-5-2" -> setup352Formation()
                "4-5-1" -> setup451Formation()
            }

            invalidate()
        }

        private fun setup433Formation() {
            val centerX = margin + pitchWidth / 2
            val defenseY = margin + pitchHeight * 0.8f
            val midfieldY = margin + pitchHeight * 0.55f
            val attackY = margin + pitchHeight * 0.25f

            // Goalkeeper
            formationPositions["GK"] = PointF(centerX, margin + pitchHeight * 0.9f)

            // Defense (4)
            val defenseSpacing = pitchWidth * 0.15f
            formationPositions["LB"] = PointF(centerX - defenseSpacing * 1.5f, defenseY)
            formationPositions["CB1"] = PointF(centerX - defenseSpacing * 0.5f, defenseY)
            formationPositions["CB2"] = PointF(centerX + defenseSpacing * 0.5f, defenseY)
            formationPositions["RB"] = PointF(centerX + defenseSpacing * 1.5f, defenseY)

            // Midfield (3)
            val midfieldSpacing = pitchWidth * 0.2f
            formationPositions["CM1"] = PointF(centerX - midfieldSpacing, midfieldY)
            formationPositions["CM2"] = PointF(centerX, midfieldY)
            formationPositions["CM3"] = PointF(centerX + midfieldSpacing, midfieldY)

            // Attack (3)
            val attackSpacing = pitchWidth * 0.25f
            formationPositions["LW"] = PointF(centerX - attackSpacing, attackY)
            formationPositions["ST"] = PointF(centerX, attackY)
            formationPositions["RW"] = PointF(centerX + attackSpacing, attackY)
        }

        private fun setup442Formation() {
            val centerX = margin + pitchWidth / 2
            val defenseY = margin + pitchHeight * 0.8f
            val midfieldY = margin + pitchHeight * 0.55f
            val attackY = margin + pitchHeight * 0.25f

            // Goalkeeper
            formationPositions["GK"] = PointF(centerX, margin + pitchHeight * 0.9f)

            // Defense (4)
            val defenseSpacing = pitchWidth * 0.15f
            formationPositions["LB"] = PointF(centerX - defenseSpacing * 1.5f, defenseY)
            formationPositions["CB1"] = PointF(centerX - defenseSpacing * 0.5f, defenseY)
            formationPositions["CB2"] = PointF(centerX + defenseSpacing * 0.5f, defenseY)
            formationPositions["RB"] = PointF(centerX + defenseSpacing * 1.5f, defenseY)

            // Midfield (4)
            val midfieldSpacing = pitchWidth * 0.17f
            formationPositions["LM"] = PointF(centerX - midfieldSpacing * 1.5f, midfieldY)
            formationPositions["CM1"] = PointF(centerX - midfieldSpacing * 0.5f, midfieldY)
            formationPositions["CM2"] = PointF(centerX + midfieldSpacing * 0.5f, midfieldY)
            formationPositions["RM"] = PointF(centerX + midfieldSpacing * 1.5f, midfieldY)

            // Attack (2)
            val attackSpacing = pitchWidth * 0.15f
            formationPositions["ST1"] = PointF(centerX - attackSpacing, attackY)
            formationPositions["ST2"] = PointF(centerX + attackSpacing, attackY)
        }

        private fun setup352Formation() {
            val centerX = margin + pitchWidth / 2
            val defenseY = margin + pitchHeight * 0.8f
            val midfieldY = margin + pitchHeight * 0.55f
            val attackY = margin + pitchHeight * 0.25f

            // Goalkeeper
            formationPositions["GK"] = PointF(centerX, margin + pitchHeight * 0.9f)

            // Defense (3)
            val defenseSpacing = pitchWidth * 0.2f
            formationPositions["CB1"] = PointF(centerX - defenseSpacing, defenseY)
            formationPositions["CB2"] = PointF(centerX, defenseY)
            formationPositions["CB3"] = PointF(centerX + defenseSpacing, defenseY)

            // Midfield (5)
            val midfieldSpacing = pitchWidth * 0.15f
            formationPositions["LWB"] = PointF(centerX - midfieldSpacing * 2f, midfieldY)
            formationPositions["CM1"] = PointF(centerX - midfieldSpacing, midfieldY)
            formationPositions["CM2"] = PointF(centerX, midfieldY)
            formationPositions["CM3"] = PointF(centerX + midfieldSpacing, midfieldY)
            formationPositions["RWB"] = PointF(centerX + midfieldSpacing * 2f, midfieldY)

            // Attack (2)
            val attackSpacing = pitchWidth * 0.15f
            formationPositions["ST1"] = PointF(centerX - attackSpacing, attackY)
            formationPositions["ST2"] = PointF(centerX + attackSpacing, attackY)
        }

        private fun setup451Formation() {
            val centerX = margin + pitchWidth / 2
            val defenseY = margin + pitchHeight * 0.8f
            val midfieldY = margin + pitchHeight * 0.55f
            val attackY = margin + pitchHeight * 0.25f

            // Goalkeeper
            formationPositions["GK"] = PointF(centerX, margin + pitchHeight * 0.9f)

            // Defense (4)
            val defenseSpacing = pitchWidth * 0.15f
            formationPositions["LB"] = PointF(centerX - defenseSpacing * 1.5f, defenseY)
            formationPositions["CB1"] = PointF(centerX - defenseSpacing * 0.5f, defenseY)
            formationPositions["CB2"] = PointF(centerX + defenseSpacing * 0.5f, defenseY)
            formationPositions["RB"] = PointF(centerX + defenseSpacing * 1.5f, defenseY)

            // Midfield (5)
            val midfieldSpacing = pitchWidth * 0.15f
            formationPositions["LM"] = PointF(centerX - midfieldSpacing * 2f, midfieldY)
            formationPositions["CM1"] = PointF(centerX - midfieldSpacing, midfieldY)
            formationPositions["CM2"] = PointF(centerX, midfieldY)
            formationPositions["CM3"] = PointF(centerX + midfieldSpacing, midfieldY)
            formationPositions["RM"] = PointF(centerX + midfieldSpacing * 2f, midfieldY)

            // Attack (1)
            formationPositions["ST"] = PointF(centerX, attackY)
        }
    }
