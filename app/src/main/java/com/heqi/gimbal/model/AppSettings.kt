package com.heqi.gimbal.model

import android.content.Context
import android.content.SharedPreferences

/**
 * 应用设置数据类
 */
data class AppSettings(
    val cameraIp: String = "192.168.144.64",
    val controlIp: String = "192.168.144.64",
    val controlPort: Int = 5000,
    val rtspUrl: String = "rtsp://192.168.144.64:558/live/single",
    val bufferTime: Int = 100,
    val enableReconnect: Boolean = true,
    val reconnectDelay: Long = 3000,
    val defaultPitchSpeed: Int = 50,
    val defaultYawSpeed: Int = 50
) {
    companion object {
        private const val PREFS_NAME = "gimbal_settings"
        private const val KEY_CAMERA_IP = "camera_ip"
        private const val KEY_CONTROL_IP = "control_ip"
        private const val KEY_CONTROL_PORT = "control_port"
        private const val KEY_RTSP_URL = "rtsp_url"
        private const val KEY_BUFFER_TIME = "buffer_time"
        private const val KEY_ENABLE_RECONNECT = "enable_reconnect"
        private const val KEY_RECONNECT_DELAY = "reconnect_delay"
        private const val KEY_PITCH_SPEED = "pitch_speed"
        private const val KEY_YAW_SPEED = "yaw_speed"
        
        fun load(context: Context): AppSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return AppSettings(
                cameraIp = prefs.getString(KEY_CAMERA_IP, "192.168.144.64") ?: "192.168.144.64",
                controlIp = prefs.getString(KEY_CONTROL_IP, "192.168.144.64") ?: "192.168.144.64",
                controlPort = prefs.getInt(KEY_CONTROL_PORT, 5000),
                rtspUrl = prefs.getString(KEY_RTSP_URL, "rtsp://192.168.144.64:558/live/single") 
                    ?: "rtsp://192.168.144.64:558/live/single",
                bufferTime = prefs.getInt(KEY_BUFFER_TIME, 100),
                enableReconnect = prefs.getBoolean(KEY_ENABLE_RECONNECT, true),
                reconnectDelay = prefs.getLong(KEY_RECONNECT_DELAY, 3000),
                defaultPitchSpeed = prefs.getInt(KEY_PITCH_SPEED, 50),
                defaultYawSpeed = prefs.getInt(KEY_YAW_SPEED, 50)
            )
        }
        
        fun save(context: Context, settings: AppSettings) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putString(KEY_CAMERA_IP, settings.cameraIp)
                putString(KEY_CONTROL_IP, settings.controlIp)
                putInt(KEY_CONTROL_PORT, settings.controlPort)
                putString(KEY_RTSP_URL, settings.rtspUrl)
                putInt(KEY_BUFFER_TIME, settings.bufferTime)
                putBoolean(KEY_ENABLE_RECONNECT, settings.enableReconnect)
                putLong(KEY_RECONNECT_DELAY, settings.reconnectDelay)
                putInt(KEY_PITCH_SPEED, settings.defaultPitchSpeed)
                putInt(KEY_YAW_SPEED, settings.defaultYawSpeed)
                apply()
            }
        }
    }
}
