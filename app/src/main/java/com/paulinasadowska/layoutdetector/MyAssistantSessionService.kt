package com.paulinasadowska.layoutdetector

import android.os.Bundle

import android.service.voice.VoiceInteractionSession

import android.service.voice.VoiceInteractionSessionService


class MyAssistantSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(bundle: Bundle): VoiceInteractionSession {
        return MyAssistantSession(this)
    }
}