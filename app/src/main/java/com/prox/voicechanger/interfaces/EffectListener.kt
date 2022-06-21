package com.prox.voicechanger.interfaces

import com.prox.voicechanger.model.Effect

interface EffectListener {
    fun addEffectListener(effect: Effect?)
}