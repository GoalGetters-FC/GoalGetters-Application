package com.ggetters.app.ui.central.views.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.PlayerAvailability

class FormationPitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint objects for drawing
    private val pitchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Colors
    private val pitchColor = ContextCompat.getColor(context, R.color.pitch_green)
    private val lineColor = Color.WHITE
    private val playerColor = ContextCompat.getColor(context, R.color.surface)
    private val playerBorderColor = ContextCompat.getColor(context, R.color.outline)
    private val textColor = ContextCompat.getColor(context, R.color.on_surface)

    // Formation and players
    private var currentFormation = "4-3-3"
    private var positionedPlayers = mutableMapOf<String, PlayerAvailability?>()
    private var playerPositions = mutableMapOf<String, PointF>()

    // Drawing properties
    private var pitchRect = RectF()
    private val playerRadius = 25f
    private val lineWidth = 3f

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // Pitch background
        pitchPaint.color = pitchColor
        pitchPaint.style = Paint.Style.FILL

        // Pitch lines
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = lineWidth
        linePaint.strokeCap = Paint.Cap.ROUND

        // Player circles
        playerPaint.color = playerColor
        playerPaint.style = Paint.Style.FILL
        playerPaint.setShadowLayer(4f, 0f, 2f, Color.argb(50, 0, 0, 0))

        // Player text
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.DEFAULT_BOLD

        // Shadow for player circles
        shadowPaint.color = Color.argb(30, 0, 0, 0)
        shadowPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calculate pitch dimensions
        val padding = 20f
        pitchRect = RectF(
            padding,
            padding,
            width - padding,
            height - padding
        )
        
        // Recalculate player positions for current formation
        calculatePlayerPositions()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw pitch background
        canvas.drawRoundRect(pitchRect, 16f, 16f, pitchPaint)
        
        // Draw pitch markings
        drawPitchMarkings(canvas)
        
        // Draw positioned players
        drawPlayers(canvas)
    }

    private fun drawPitchMarkings(canvas: Canvas) {
        val pitchWidth = pitchRect.width()
        val pitchHeight = pitchRect.height()
        
        // Outer boundary
        canvas.drawRoundRect(pitchRect, 16f, 16f, linePaint)
        
        // Center line
        val centerX = pitchRect.centerX()
        canvas.drawLine(
            centerX, pitchRect.top,
            centerX, pitchRect.bottom,
            linePaint
        )
        
        // Center circle
        val centerCircleRadius = pitchWidth * 0.12f
        canvas.drawCircle(centerX, pitchRect.centerY(), centerCircleRadius, linePaint)
        
        // Center spot
        canvas.drawCircle(centerX, pitchRect.centerY(), 4f, linePaint)
        
        // Goal areas (18-yard boxes)
        val goalAreaWidth = pitchWidth * 0.35f
        val goalAreaHeight = pitchHeight * 0.25f
        val goalAreaX = (pitchWidth - goalAreaWidth) / 2f
        
        // Top goal area
        canvas.drawRect(
            pitchRect.left + goalAreaX,
            pitchRect.top,
            pitchRect.left + goalAreaX + goalAreaWidth,
            pitchRect.top + goalAreaHeight,
            linePaint
        )
        
        // Bottom goal area
        canvas.drawRect(
            pitchRect.left + goalAreaX,
            pitchRect.bottom - goalAreaHeight,
            pitchRect.left + goalAreaX + goalAreaWidth,
            pitchRect.bottom,
            linePaint
        )
        
        // 6-yard boxes
        val sixYardWidth = pitchWidth * 0.2f
        val sixYardHeight = pitchHeight * 0.12f
        val sixYardX = (pitchWidth - sixYardWidth) / 2f
        
        // Top 6-yard box
        canvas.drawRect(
            pitchRect.left + sixYardX,
            pitchRect.top,
            pitchRect.left + sixYardX + sixYardWidth,
            pitchRect.top + sixYardHeight,
            linePaint
        )
        
        // Bottom 6-yard box
        canvas.drawRect(
            pitchRect.left + sixYardX,
            pitchRect.bottom - sixYardHeight,
            pitchRect.left + sixYardX + sixYardWidth,
            pitchRect.bottom,
            linePaint
        )
        
        // Corner arcs
        val cornerRadius = 20f
        // Top-left corner
        canvas.drawArc(
            pitchRect.left - cornerRadius,
            pitchRect.top - cornerRadius,
            pitchRect.left + cornerRadius,
            pitchRect.top + cornerRadius,
            0f, 90f, false, linePaint
        )
        
        // Top-right corner
        canvas.drawArc(
            pitchRect.right - cornerRadius,
            pitchRect.top - cornerRadius,
            pitchRect.right + cornerRadius,
            pitchRect.top + cornerRadius,
            90f, 90f, false, linePaint
        )
        
        // Bottom-left corner
        canvas.drawArc(
            pitchRect.left - cornerRadius,
            pitchRect.bottom - cornerRadius,
            pitchRect.left + cornerRadius,
            pitchRect.bottom + cornerRadius,
            270f, 90f, false, linePaint
        )
        
        // Bottom-right corner
        canvas.drawArc(
            pitchRect.right - cornerRadius,
            pitchRect.bottom - cornerRadius,
            pitchRect.right + cornerRadius,
            pitchRect.bottom + cornerRadius,
            180f, 90f, false, linePaint
        )
    }

    private fun drawPlayers(canvas: Canvas) {
        for ((position, player) in positionedPlayers) {
            val point = playerPositions[position] ?: continue
            
            // Draw shadow
            canvas.drawCircle(point.x + 2f, point.y + 2f, playerRadius, shadowPaint)
            
            // Draw player circle
            canvas.drawCircle(point.x, point.y, playerRadius, playerPaint)
            
            // Draw border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = playerBorderColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawCircle(point.x, point.y, playerRadius, borderPaint)
            
            // Draw jersey number or position
            val displayText = player?.jerseyNumber?.toString() ?: position.take(2)
            canvas.drawText(
                displayText,
                point.x,
                point.y + (textPaint.textSize / 3),
                textPaint
            )
        }
    }

    private fun calculatePlayerPositions() {
        playerPositions.clear()
        
        when (currentFormation) {
            "4-3-3" -> calculate433Positions()
            "4-4-2" -> calculate442Positions()
            "3-5-2" -> calculate352Positions()
            "4-2-3-1" -> calculate4231Positions()
            "5-3-2" -> calculate532Positions()
        }
    }

    private fun calculate433Positions() {
        val pitchWidth = pitchRect.width()
        val pitchHeight = pitchRect.height()
        
        // Goalkeeper
        playerPositions["GK"] = PointF(
            pitchRect.centerX(),
            pitchRect.bottom - pitchHeight * 0.08f
        )
        
        // Defense (4 players)
        val defenseY = pitchRect.bottom - pitchHeight * 0.25f
        playerPositions["LB"] = PointF(pitchRect.left + pitchWidth * 0.15f, defenseY)
        playerPositions["CB1"] = PointF(pitchRect.left + pitchWidth * 0.35f, defenseY)
        playerPositions["CB2"] = PointF(pitchRect.left + pitchWidth * 0.65f, defenseY)
        playerPositions["RB"] = PointF(pitchRect.left + pitchWidth * 0.85f, defenseY)
        
        // Midfield (3 players)
        val midfieldY = pitchRect.bottom - pitchHeight * 0.55f
        playerPositions["CM1"] = PointF(pitchRect.left + pitchWidth * 0.25f, midfieldY)
        playerPositions["CM2"] = PointF(pitchRect.centerX(), midfieldY)
        playerPositions["CM3"] = PointF(pitchRect.left + pitchWidth * 0.75f, midfieldY)
        
        // Attack (3 players)
        val attackY = pitchRect.bottom - pitchHeight * 0.8f
        playerPositions["LW"] = PointF(pitchRect.left + pitchWidth * 0.2f, attackY)
        playerPositions["ST"] = PointF(pitchRect.centerX(), attackY)
        playerPositions["RW"] = PointF(pitchRect.left + pitchWidth * 0.8f, attackY)
    }

    private fun calculate442Positions() {
        // TODO: Implement 4-4-2 formation positions
    }

    private fun calculate352Positions() {
        // TODO: Implement 3-5-2 formation positions
    }

    private fun calculate4231Positions() {
        // TODO: Implement 4-2-3-1 formation positions
    }

    private fun calculate532Positions() {
        // TODO: Implement 5-3-2 formation positions
    }

    // Touch handling properties
    private var draggedPosition: String? = null
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var isDragging = false
    
    // Callbacks
    private var onPlayerClickListener: ((String, PlayerAvailability?) -> Unit)? = null
    private var onPositionClickListener: ((String) -> Unit)? = null
    private var onPlayerDroppedListener: ((String, PointF) -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStartX = event.x
                dragStartY = event.y
                
                // Check if touch is on a player or position
                val touchedPosition = getPositionAtPoint(event.x, event.y)
                if (touchedPosition != null) {
                    draggedPosition = touchedPosition
                    isDragging = false
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (draggedPosition != null) {
                    val distance = kotlin.math.sqrt(
                        ((event.x - dragStartX) * (event.x - dragStartX) + 
                         (event.y - dragStartY) * (event.y - dragStartY)).toDouble()
                    ).toFloat()
                    
                    if (distance > 20f) { // Start dragging after minimum distance
                        isDragging = true
                        // Update position for visual feedback
                        playerPositions[draggedPosition!!] = PointF(event.x, event.y)
                        invalidate()
                    }
                    return true
                }
            }
            
            MotionEvent.ACTION_UP -> {
                draggedPosition?.let { position ->
                    if (isDragging) {
                        // Handle drop
                        onPlayerDroppedListener?.invoke(position, PointF(event.x, event.y))
                        // Reset to original position (will be updated by parent if valid)
                        calculatePlayerPositions()
                    } else {
                        // Handle click
                        val player = positionedPlayers[position]
                        if (player != null) {
                            onPlayerClickListener?.invoke(position, player)
                        } else {
                            onPositionClickListener?.invoke(position)
                        }
                    }
                    invalidate()
                }
                draggedPosition = null
                isDragging = false
                return true
            }
            
            MotionEvent.ACTION_CANCEL -> {
                if (draggedPosition != null) {
                    // Reset to original position
                    calculatePlayerPositions()
                    invalidate()
                }
                draggedPosition = null
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getPositionAtPoint(x: Float, y: Float): String? {
        for ((position, point) in playerPositions) {
            val distance = kotlin.math.sqrt(
                ((x - point.x) * (x - point.x) + (y - point.y) * (y - point.y)).toDouble()
            ).toFloat()
            
            if (distance <= playerRadius + 10f) {
                return position
            }
        }
        return null
    }

    // Public methods
    fun setFormation(formation: String) {
        if (currentFormation != formation) {
            currentFormation = formation
            calculatePlayerPositions()
            invalidate()
        }
    }

    fun setPlayers(players: Map<String, PlayerAvailability?>) {
        positionedPlayers.clear()
        positionedPlayers.putAll(players)
        invalidate()
    }

    fun getPositionedPlayers(): Map<String, PlayerAvailability?> {
        return positionedPlayers.toMap()
    }

    fun getCurrentFormation(): String = currentFormation

    // Callback setters
    fun setOnPlayerClickListener(listener: (String, PlayerAvailability?) -> Unit) {
        onPlayerClickListener = listener
    }

    fun setOnPositionClickListener(listener: (String) -> Unit) {
        onPositionClickListener = listener
    }

    fun setOnPlayerDroppedListener(listener: (String, PointF) -> Unit) {
        onPlayerDroppedListener = listener
    }

    // Position validation
    fun isValidDropPosition(position: String, point: PointF): Boolean {
        // Check if drop position is within pitch bounds
        return point.x >= pitchRect.left && 
               point.x <= pitchRect.right && 
               point.y >= pitchRect.top && 
               point.y <= pitchRect.bottom
    }

    // Get all available positions for current formation
    fun getAvailablePositions(): List<String> {
        return playerPositions.keys.toList()
    }

    // Check if a position is occupied
    fun isPositionOccupied(position: String): Boolean {
        return positionedPlayers.containsKey(position) && positionedPlayers[position] != null
    }
}
