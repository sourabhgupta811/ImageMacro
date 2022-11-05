package com.assignment.imagemacro.data

sealed class Action
data class OverlayAddAction(val overlay: Overlay): Action()
data class MoveAction(val overlay: Overlay, val startPos:Pair<Float,Float>, val endPos:Pair<Float,Float>):
    Action()