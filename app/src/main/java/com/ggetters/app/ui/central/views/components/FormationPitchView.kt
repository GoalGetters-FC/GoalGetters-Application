package com.ggetters.app.ui.central.views.components

import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ggetters.app.R
import com.ggetters.app.data.model.RosterPlayer

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
    private var positionedPlayers = mutableMapOf<String, RosterPlayer?>()
    private var playerPositions = mutableMapOf<String, PointF>()

    // Drawing properties
    private var pitchRect = RectF()
    private val playerRadius = 35f  // Larger for better visibility
    private val dropZoneRadius = 45f  // Larger drop zones
    private val lineWidth = 4f
    private val grassPattern = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setupPaints()
        setupDragListener()
    }

    private fun setupPaints() {
        // Pitch background - solid green like the image
        pitchPaint.color = Color.parseColor("#4CAF50")  // Match image green
        pitchPaint.style = Paint.Style.FILL

        // White lines - crisp and clean
        linePaint.color = Color.WHITE
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 4f
        linePaint.strokeCap = Paint.Cap.ROUND

        // Player circles
        playerPaint.color = Color.WHITE
        playerPaint.style = Paint.Style.FILL
        playerPaint.setShadowLayer(6f, 0f, 4f, Color.argb(80, 0, 0, 0))

        // Player text
        textPaint.color = Color.parseColor("#1976D2")
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.setShadowLayer(2f, 0f, 1f, Color.argb(100, 255, 255, 255))

        // Shadow for player circles
        shadowPaint.color = Color.argb(50, 0, 0, 0)
        shadowPaint.style = Paint.Style.FILL
    }

    private fun setupDragListener() {
        setOnDragListener { _, event ->
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_STARTED -> {
                    // Accept the drag if it's a player
                    event.clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
                }
                
                android.view.DragEvent.ACTION_DRAG_ENTERED -> {
                    // Highlight drop zones when drag enters
                    invalidate()
                    true
                }
                
                android.view.DragEvent.ACTION_DRAG_LOCATION -> {
                    // Update visual feedback based on drag location
                    val dropPosition = getPositionAtPoint(event.x, event.y)
                    if (dropPosition != null) {
                        // Could add visual feedback here
                    }
                    true
                }
                
                android.view.DragEvent.ACTION_DROP -> {
                    val player = event.localState as? RosterPlayer
                    if (player != null) {
                        val position = getPositionAtPoint(event.x, event.y)
                        if (position != null) {
                            // Place dragged bench player at target position (occupied or empty)
                                val updatedPositions = positionedPlayers.toMutableMap()
                                updatedPositions[position] = player
                                positionedPlayers = updatedPositions
                                invalidate()
                                
                            // Notify fragment using a sentinel origin so it treats this as a grid drop
                            onPlayerDroppedListener?.invoke("__GRID__", PointF(event.x, event.y))
                        }
                    }
                    true
                }
                
                android.view.DragEvent.ACTION_DRAG_EXITED -> {
                    // Remove visual feedback
                    invalidate()
                    true
                }
                
                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    // Reset any visual feedback
                    invalidate()
                    true
                }
                
                else -> true
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calculate pitch dimensions to maximize space
        val padding = 8f  // Reduced padding
        val availableWidth = width - (padding * 2)
        val availableHeight = height - (padding * 2)
        
        // Adjust ratio to be slightly less vertical to use more space
        val pitchRatio = 1.3f  // Slightly reduced from 1.5 to use more width
        
        // Calculate dimensions to maintain aspect ratio while maximizing size
        val pitchWidth: Float
        val pitchHeight: Float
        val leftOffset: Float
        val topOffset: Float
        
        if (availableHeight / availableWidth > pitchRatio) {
            // Width is the limiting factor - use full width
            pitchWidth = availableWidth
            pitchHeight = pitchWidth * pitchRatio
            leftOffset = padding
            topOffset = (height - pitchHeight) / 2
        } else {
            // Height is the limiting factor - use full height
            pitchHeight = availableHeight
            pitchWidth = pitchHeight / pitchRatio
            leftOffset = (width - pitchWidth) / 2
            topOffset = padding
        }
        
        // Ensure minimum margins
        val minMargin = 4f
        pitchRect = RectF(
            maxOf(leftOffset, minMargin),
            maxOf(topOffset, minMargin),
            minOf(leftOffset + pitchWidth, width - minMargin),
            minOf(topOffset + pitchHeight, height - minMargin)
        )
        
        // Recalculate player positions for current formation
        calculatePlayerPositions()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw realistic grass pitch with stripes
        drawGrassPitch(canvas)
        
        // Draw professional pitch markings
        drawPitchMarkings(canvas)
        
        // Draw empty positions first (drop zones)
        drawDropZones(canvas)
        
        // Draw positioned players
        drawPlayers(canvas)
    }

    private fun drawGrassPitch(canvas: Canvas) {
        // Draw solid green background like the image
        canvas.drawRect(pitchRect, pitchPaint)
    }

    private fun drawDropZones(canvas: Canvas) {
        // Draw rounded square drop zones for empty positions
        val dropZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)  // Semi-transparent white
            style = Paint.Style.FILL
        }
        
        val dropZoneBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)  // More visible border
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)  // Dashed border
        }
        
        for ((position, point) in playerPositions) {
            if (!positionedPlayers.containsKey(position) || positionedPlayers[position] == null) {
                // Draw rounded square drop zone
                val left = point.x - dropZoneRadius
                val top = point.y - dropZoneRadius  
                val right = point.x + dropZoneRadius
                val bottom = point.y + dropZoneRadius
                val dropZoneRect = RectF(left, top, right, bottom)
                
                // Fill with rounded corners
                canvas.drawRoundRect(dropZoneRect, 15f, 15f, dropZonePaint)
                // Dashed border
                canvas.drawRoundRect(dropZoneRect, 15f, 15f, dropZoneBorderPaint)
                
                // Draw position label
                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(180, 255, 255, 255)
                    textAlign = Paint.Align.CENTER
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(
                    position,
                    point.x,
                    point.y + (labelPaint.textSize / 3),
                    labelPaint
                )
            }
        }
    }

    private fun drawPitchMarkings(canvas: Canvas) {
        val pitchWidth = pitchRect.width()
        val pitchHeight = pitchRect.height()
        
        // Outer boundary
        canvas.drawRect(pitchRect, linePaint)
        
        // Center line
        canvas.drawLine(
            pitchRect.left,
            pitchRect.centerY(),
            pitchRect.right,
            pitchRect.centerY(),
            linePaint
        )

        // Center circle
        val centerCircleRadius = pitchWidth * 0.2f
        canvas.drawCircle(
            pitchRect.centerX(),
            pitchRect.centerY(),
            centerCircleRadius,
            linePaint
        )

        // Center spot
        canvas.drawCircle(
            pitchRect.centerX(),
            pitchRect.centerY(),
            6f,
            linePaint
        )

        // Penalty areas (18-yard boxes)
        val penaltyAreaWidth = pitchWidth * 0.7f
        val penaltyAreaHeight = pitchHeight * 0.2f
        val penaltyAreaX = (pitchWidth - penaltyAreaWidth) / 2f

        // Top penalty area
        canvas.drawRect(
            pitchRect.left + penaltyAreaX,
            pitchRect.top,
            pitchRect.left + penaltyAreaX + penaltyAreaWidth,
            pitchRect.top + penaltyAreaHeight,
            linePaint
        )

        // Bottom penalty area
        canvas.drawRect(
            pitchRect.left + penaltyAreaX,
            pitchRect.bottom - penaltyAreaHeight,
            pitchRect.left + penaltyAreaX + penaltyAreaWidth,
            pitchRect.bottom,
            linePaint
        )

        // Goal areas (6-yard boxes)
        val goalAreaWidth = pitchWidth * 0.4f
        val goalAreaHeight = pitchHeight * 0.1f
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
        
        // Corner arcs
        val cornerRadius = pitchWidth * 0.05f

        // Top-left corner
        canvas.drawArc(
            pitchRect.left,
            pitchRect.top,
            pitchRect.left + (cornerRadius * 2),
            pitchRect.top + (cornerRadius * 2),
            0f,
            90f,
            false,
            linePaint
        )
        
        // Top-right corner
        canvas.drawArc(
            pitchRect.right - (cornerRadius * 2),
            pitchRect.top,
            pitchRect.right,
            pitchRect.top + (cornerRadius * 2),
            90f,
            90f,
            false,
            linePaint
        )
        
        // Bottom-left corner
        canvas.drawArc(
            pitchRect.left,
            pitchRect.bottom - (cornerRadius * 2),
            pitchRect.left + (cornerRadius * 2),
            pitchRect.bottom,
            270f,
            90f,
            false,
            linePaint
        )
        
        // Bottom-right corner
        canvas.drawArc(
            pitchRect.right - (cornerRadius * 2),
            pitchRect.bottom - (cornerRadius * 2),
            pitchRect.right,
            pitchRect.bottom,
            180f,
            90f,
            false,
            linePaint
        )
    }



    private fun drawPlayers(canvas: Canvas) {
        for ((position, player) in positionedPlayers) {
            val point = playerPositions[position] ?: continue
            player ?: continue  // Skip if no player assigned
            
            // Draw enhanced shadow
            canvas.drawCircle(point.x + 4f, point.y + 4f, playerRadius, shadowPaint)
            
            // Draw player circle with team colors
            val teamColorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1976D2")  // Team blue
                style = Paint.Style.FILL
                setShadowLayer(6f, 0f, 4f, Color.argb(80, 0, 0, 0))
            }
            canvas.drawCircle(point.x, point.y, playerRadius, teamColorPaint)
            
            // Draw white inner circle for contrast
            val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(point.x, point.y, playerRadius - 8f, innerCirclePaint)
            
            // Draw jersey number prominently
            val jerseyNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1976D2")
                textAlign = Paint.Align.CENTER
                textSize = 32f
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                player.jerseyNumber.toString(),
                point.x,
                point.y + 8f,
                jerseyNumberPaint
            )
            
            // Draw player surname below the circle
            val surnameY = point.y + playerRadius + 35f
            val surnamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(3f, 0f, 2f, Color.argb(150, 0, 0, 0))
            }
            
            // Extract surname (last word of player name)
            val surname = player.playerName.split(" ").lastOrNull()?.uppercase() ?: "PLAYER"
            canvas.drawText(
                surname,
                point.x,
                surnameY,
                surnamePaint
            )
            
            // Draw position abbreviation above the circle
            val positionY = point.y - playerRadius - 20f
            val positionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD400")  // Yellow for visibility
                textAlign = Paint.Align.CENTER
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(2f, 0f, 1f, Color.argb(150, 0, 0, 0))
            }
            canvas.drawText(
                position,
                point.x,
                positionY,
                positionPaint
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
        
        // Midfield (4 players)
        val midfieldY = pitchRect.bottom - pitchHeight * 0.55f
        playerPositions["LM"] = PointF(pitchRect.left + pitchWidth * 0.1f, midfieldY)
        playerPositions["CM1"] = PointF(pitchRect.left + pitchWidth * 0.35f, midfieldY)
        playerPositions["CM2"] = PointF(pitchRect.left + pitchWidth * 0.65f, midfieldY)
        playerPositions["RM"] = PointF(pitchRect.left + pitchWidth * 0.9f, midfieldY)
        
        // Attack (2 players)
        val attackY = pitchRect.bottom - pitchHeight * 0.8f
        playerPositions["ST1"] = PointF(pitchRect.left + pitchWidth * 0.35f, attackY)
        playerPositions["ST2"] = PointF(pitchRect.left + pitchWidth * 0.65f, attackY)
    }

    private fun calculate352Positions() {
        val pitchWidth = pitchRect.width()
        val pitchHeight = pitchRect.height()
        
        // Goalkeeper
        playerPositions["GK"] = PointF(
            pitchRect.centerX(),
            pitchRect.bottom - pitchHeight * 0.08f
        )
        
        // Defense (3 players)
        val defenseY = pitchRect.bottom - pitchHeight * 0.25f
        playerPositions["CB1"] = PointF(pitchRect.left + pitchWidth * 0.25f, defenseY)
        playerPositions["CB2"] = PointF(pitchRect.centerX(), defenseY)
        playerPositions["CB3"] = PointF(pitchRect.left + pitchWidth * 0.75f, defenseY)
        
        // Wing-backs and Midfield (5 players)
        val midfieldY = pitchRect.bottom - pitchHeight * 0.55f
        playerPositions["LWB"] = PointF(pitchRect.left + pitchWidth * 0.05f, midfieldY)
        playerPositions["CM1"] = PointF(pitchRect.left + pitchWidth * 0.3f, midfieldY)
        playerPositions["CM2"] = PointF(pitchRect.centerX(), midfieldY)
        playerPositions["CM3"] = PointF(pitchRect.left + pitchWidth * 0.7f, midfieldY)
        playerPositions["RWB"] = PointF(pitchRect.left + pitchWidth * 0.95f, midfieldY)
        
        // Attack (2 players)
        val attackY = pitchRect.bottom - pitchHeight * 0.8f
        playerPositions["ST1"] = PointF(pitchRect.left + pitchWidth * 0.35f, attackY)
        playerPositions["ST2"] = PointF(pitchRect.left + pitchWidth * 0.65f, attackY)
    }

    private fun calculate4231Positions() {
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
        
        // Defensive Midfield (2 players)
        val dmY = pitchRect.bottom - pitchHeight * 0.45f
        playerPositions["CDM1"] = PointF(pitchRect.left + pitchWidth * 0.35f, dmY)
        playerPositions["CDM2"] = PointF(pitchRect.left + pitchWidth * 0.65f, dmY)
        
        // Attacking Midfield (3 players)
        val amY = pitchRect.bottom - pitchHeight * 0.65f
        playerPositions["LW"] = PointF(pitchRect.left + pitchWidth * 0.15f, amY)
        playerPositions["CAM"] = PointF(pitchRect.centerX(), amY)
        playerPositions["RW"] = PointF(pitchRect.left + pitchWidth * 0.85f, amY)
        
        // Attack (1 player)
        val attackY = pitchRect.bottom - pitchHeight * 0.8f
        playerPositions["ST"] = PointF(pitchRect.centerX(), attackY)
    }

    private fun calculate532Positions() {
        val pitchWidth = pitchRect.width()
        val pitchHeight = pitchRect.height()
        
        // Goalkeeper
        playerPositions["GK"] = PointF(
            pitchRect.centerX(),
            pitchRect.bottom - pitchHeight * 0.08f
        )
        
        // Defense (5 players)
        val defenseY = pitchRect.bottom - pitchHeight * 0.25f
        playerPositions["LB"] = PointF(pitchRect.left + pitchWidth * 0.1f, defenseY)
        playerPositions["CB1"] = PointF(pitchRect.left + pitchWidth * 0.3f, defenseY)
        playerPositions["CB2"] = PointF(pitchRect.centerX(), defenseY)
        playerPositions["CB3"] = PointF(pitchRect.left + pitchWidth * 0.7f, defenseY)
        playerPositions["RB"] = PointF(pitchRect.left + pitchWidth * 0.9f, defenseY)
        
        // Midfield (3 players)
        val midfieldY = pitchRect.bottom - pitchHeight * 0.55f
        playerPositions["CM1"] = PointF(pitchRect.left + pitchWidth * 0.25f, midfieldY)
        playerPositions["CM2"] = PointF(pitchRect.centerX(), midfieldY)
        playerPositions["CM3"] = PointF(pitchRect.left + pitchWidth * 0.75f, midfieldY)
        
        // Attack (2 players)
        val attackY = pitchRect.bottom - pitchHeight * 0.8f
        playerPositions["ST1"] = PointF(pitchRect.left + pitchWidth * 0.35f, attackY)
        playerPositions["ST2"] = PointF(pitchRect.left + pitchWidth * 0.65f, attackY)
    }

    // Touch handling properties
    private var draggedPosition: String? = null
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var isDragging = false
    
    // Callbacks
    private var onPlayerClickListener: ((String, RosterPlayer?) -> Unit)? = null
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
            
            // Use larger detection area for easier interaction
            val detectionRadius = if (positionedPlayers[position] != null) {
                playerRadius + 15f  // Players
            } else {
                dropZoneRadius + 10f  // Empty positions (drop zones)
            }
            
            if (distance <= detectionRadius) {
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

    fun setPlayers(players: Map<String, RosterPlayer?>) {
        positionedPlayers.clear()
        positionedPlayers.putAll(players)
        invalidate()
    }

    fun getPositionedPlayers(): Map<String, RosterPlayer?> {
        return positionedPlayers.toMap()
    }

    fun getCurrentFormation(): String = currentFormation

    // Callback setters
    fun setOnPlayerClickListener(listener: (String, RosterPlayer?) -> Unit) {
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

    // Get the position coordinates for a given position name
    fun getPositionCoordinates(position: String): PointF? {
        return playerPositions[position]
    }

    // Get all position coordinates
    fun getAllPositionCoordinates(): Map<String, PointF> {
        return playerPositions.toMap()
    }
}