package com.heqi.gimbal.protocol

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

/**
 * 协议管理器 - 负责与吊舱的UDP通信
 */
class ProtocolManager private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var instance: ProtocolManager? = null
        
        fun getInstance(context: Context): ProtocolManager {
            return instance ?: synchronized(this) {
                instance ?: ProtocolManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences("protocol_settings", Context.MODE_PRIVATE)
    private val sequenceCounter = AtomicInteger(0)
    
    private var socket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private var serverPort: Int = 5000
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var receiveJob: Job? = null
    
    // 状态回调
    var onGimbalStatusReceived: ((GimbalStatus) -> Unit)? = null
    var onGimbalAttitudeReceived: ((GimbalAttitude) -> Unit)? = null
    var onCameraStatusReceived: ((CameraStatus) -> Unit)? = null
    var onIRStatusReceived: ((IRStatus) -> Unit)? = null
    var onVisibleStatusReceived: ((VisibleStatus) -> Unit)? = null
    var onACKReceived: ((Int, Int) -> Unit)? = null  // msgId, ackCode
    var onLaserRangeReceived: ((Float) -> Unit)? = null
    
    // 连接状态
    var isConnected: Boolean = false
        private set
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        serverPort = prefs.getInt("control_port", 5000)
        val ip = prefs.getString("control_ip", "192.168.144.64") ?: "192.168.144.64"
        try {
            serverAddress = InetAddress.getByName(ip)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse IP address")
        }
    }
    
    fun updateSettings(ip: String, port: Int) {
        prefs.edit().apply {
            putString("control_ip", ip)
            putInt("control_port", port)
            apply()
        }
        serverPort = port
        try {
            serverAddress = InetAddress.getByName(ip)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse IP address")
        }
    }
    
    fun connect(): Boolean {
        if (isConnected) return true
        
        return try {
            socket = DatagramSocket().apply {
                soTimeout = 5000
            }
            startReceiving()
            isConnected = true
            Timber.d("Protocol manager connected to ${serverAddress?.hostAddress}:$serverPort")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect protocol manager")
            false
        }
    }
    
    fun disconnect() {
        isConnected = false
        receiveJob?.cancel()
        receiveJob = null
        try {
            socket?.close()
        } catch (e: Exception) {
            Timber.e(e, "Error closing socket")
        }
        socket = null
        Timber.d("Protocol manager disconnected")
    }
    
    private fun startReceiving() {
        receiveJob = scope.launch {
            val buffer = ByteArray(1024)
            while (isActive && isConnected) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    
                    val data = ByteArray(packet.length)
                    System.arraycopy(buffer, 0, data, 0, packet.length)
                    
                    parseReceivedData(data)
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        Timber.e(e, "Error receiving data")
                    }
                }
            }
        }
    }
    
    private fun parseReceivedData(data: ByteArray) {
        val packet = HeQiProtocol.parsePacket(data) ?: return
        
        when (packet.msgId) {
            // ACK消息 (高字节为0x01)
            in 0x010000..0x01FFFF -> {
                val originalMsgId = packet.msgId and 0x00FFFF
                val ackCode = if (packet.payload.size >= 2) {
                    (packet.payload[0].toInt() and 0xFF) or 
                    ((packet.payload[1].toInt() and 0xFF) shl 8)
                } else 0x0001
                onACKReceived?.invoke(originalMsgId, ackCode)
            }
            
            // 状态帧消息 (高字节为0x02)
            in 0x020000..0x02FFFF -> {
                val originalMsgId = packet.msgId and 0x00FFFF
                handleStatusFrame(originalMsgId, packet.payload)
            }
            
            // 周期性上报消息
            HeQiProtocol.MSG_GIMBAL_STATUS -> {
                parseGimbalStatus(packet.payload)
            }
            HeQiProtocol.MSG_GIMBAL_ATTITUDE -> {
                parseGimbalAttitude(packet.payload)
            }
            HeQiProtocol.MSG_CAMERA_STATUS -> {
                parseCameraStatus(packet.payload)
            }
            HeQiProtocol.MSG_IR_STATUS -> {
                parseIRStatus(packet.payload)
            }
            HeQiProtocol.MSG_VISIBLE_STATUS -> {
                parseVisibleStatus(packet.payload)
            }
        }
    }
    
    private fun handleStatusFrame(msgId: Int, payload: ByteArray) {
        when (msgId) {
            HeQiProtocol.MSG_RECORD -> {
                // 录像状态帧
                if (payload.size >= 4) {
                    val isRecording = payload[1].toInt() == 1
                    val recordTime = (payload[2].toInt() and 0xFF) or 
                                    ((payload[3].toInt() and 0xFF) shl 8)
                    Timber.d("Recording status: $isRecording, time: $recordTime")
                }
            }
            HeQiProtocol.MSG_TAKE_PHOTO -> {
                // 拍照状态帧
                if (payload.size >= 4) {
                    val photoMode = payload[0].toInt()
                    val photoStatus = payload[1].toInt()
                    val burstCount = (payload[2].toInt() and 0xFF) or 
                                    ((payload[3].toInt() and 0xFF) shl 8)
                    Timber.d("Photo status: mode=$photoMode, status=$photoStatus, burst=$burstCount")
                }
            }
            HeQiProtocol.MSG_SET_ZOOM -> {
                // 变倍状态帧
                if (payload.size >= 1) {
                    val zoomStatus = payload[0].toInt()
                    Timber.d("Zoom status: $zoomStatus")
                }
            }
            HeQiProtocol.MSG_LASER_RANGE, HeQiProtocol.MSG_LASER_PERIODIC_RANGE -> {
                // 激光测距状态帧
                if (payload.size >= 4) {
                    val distance = (payload[2].toInt() and 0xFF) or 
                                  ((payload[3].toInt() and 0xFF) shl 8)
                    val distanceMeters = distance / 10.0f
                    onLaserRangeReceived?.invoke(distanceMeters)
                }
            }
        }
    }
    
    private fun parseGimbalStatus(payload: ByteArray) {
        if (payload.size < 6) return
        
        val status = GimbalStatus(
            gimbalConnected = (payload[0].toInt() and 0x0F) == 0,
            cameraConnected = (payload[0].toInt() and 0xF0) shr 4 == 0,
            upgradeStatus = payload[1].toInt(),
            irNormal = (payload[2].toInt() and 0x01) != 0,
            telephotoNormal = (payload[2].toInt() and 0x02) != 0,
            wideNormal = (payload[2].toInt() and 0x04) != 0,
            laserNormal = (payload[2].toInt() and 0x08) != 0,
            stabilizeStatus = payload[3].toInt()
        )
        onGimbalStatusReceived?.invoke(status)
    }
    
    private fun parseGimbalAttitude(payload: ByteArray) {
        if (payload.size < 20) return
        
        fun readInt16(data: ByteArray, offset: Int): Int {
            return (data[offset].toInt() and 0xFF) or 
                   ((data[offset + 1].toInt() and 0xFF) shl 8)
        }
        
        val attitude = GimbalAttitude(
            yawJoint = readInt16(payload, 0) / 100.0f,
            rollJoint = readInt16(payload, 2) / 100.0f,
            pitchJoint = readInt16(payload, 4) / 100.0f,
            yawAttitude = readInt16(payload, 6) / 100.0f,
            rollAttitude = readInt16(payload, 8) / 100.0f,
            pitchAttitude = readInt16(payload, 10) / 100.0f,
            yawSpeed = readInt16(payload, 12) / 100.0f,
            pitchSpeed = readInt16(payload, 14) / 100.0f,
            rollSpeed = readInt16(payload, 18) / 100.0f
        )
        onGimbalAttitudeReceived?.invoke(attitude)
    }
    
    private fun parseCameraStatus(payload: ByteArray) {
        if (payload.size < 16) return
        
        fun readUInt16(data: ByteArray, offset: Int): Int {
            return (data[offset].toInt() and 0xFF) or 
                   ((data[offset + 1].toInt() and 0xFF) shl 8)
        }
        
        val status = CameraStatus(
            mode = if (payload[0].toInt() == 0) CameraMode.PHOTO else CameraMode.RECORD,
            resolution = payload[1].toInt(),
            codec = payload[2].toInt(),
            streamMode = payload[3].toInt(),
            bitrate = payload[4].toInt(),
            photoMode = payload[5].toInt(),
            timelapseInterval = payload[6].toInt(),
            burstCount = payload[7].toInt(),
            sdStatus = payload[8].toInt(),
            sdTotalCapacity = readUInt16(payload, 9) / 10.0f,
            sdFreeCapacity = readUInt16(payload, 11) / 10.0f,
            sdUsedCapacity = readUInt16(payload, 13) / 10.0f
        )
        onCameraStatusReceived?.invoke(status)
    }
    
    private fun parseIRStatus(payload: ByteArray) {
        if (payload.size < 30) return
        
        fun readInt16(data: ByteArray, offset: Int): Int {
            return (data[offset].toInt() and 0xFF) or 
                   ((data[offset + 1].toInt() and 0xFF) shl 8)
        }
        
        fun readUInt16(data: ByteArray, offset: Int): Int {
            return readInt16(data, offset)
        }
        
        val status = IRStatus(
            maxTemp = readInt16(payload, 0) / 10.0f,
            minTemp = readInt16(payload, 2) / 10.0f,
            centerTemp = readInt16(payload, 4) / 10.0f,
            pointTemp = readInt16(payload, 6) / 10.0f,
            avgTemp = readInt16(payload, 8) / 10.0f,
            maxTempX = readUInt16(payload, 10),
            maxTempY = readUInt16(payload, 12),
            minTempX = readUInt16(payload, 14),
            minTempY = readUInt16(payload, 16),
            centerTempX = readUInt16(payload, 18),
            centerTempY = readUInt16(payload, 20),
            pointTempX = readUInt16(payload, 22),
            pointTempY = readUInt16(payload, 24),
            highTempWarning = payload[26].toInt() == 1,
            lowTempWarning = payload[27].toInt() == 1,
            tempDiffWarning = payload[28].toInt() == 1,
            thresholdWarning = payload[29].toInt() == 1
        )
        onIRStatusReceived?.invoke(status)
    }
    
    private fun parseVisibleStatus(payload: ByteArray) {
        if (payload.size < 15) return
        
        fun readUInt16(data: ByteArray, offset: Int): Int {
            return (data[offset].toInt() and 0xFF) or 
                   ((data[offset + 1].toInt() and 0xFF) shl 8)
        }
        
        val status = VisibleStatus(
            zoomStatus = payload[0].toInt(),
            focalLength = readUInt16(payload, 1) / 100.0f,
            zoomRate = readUInt16(payload, 3) / 10.0f,
            evValue = payload[5].toInt(),
            isoValue = readUInt16(payload, 6) / 10.0f,
            shutter = readUInt16(payload, 8),
            aeLock = payload[10].toInt() == 1,
            focusStatus = payload[11].toInt(),
            preciseCaptureFocal = readUInt16(payload, 12)
        )
        onVisibleStatusReceived?.invoke(status)
    }
    
    fun sendCommand(packet: ByteArray) {
        if (!isConnected) return
        
        scope.launch {
            try {
                val address = serverAddress ?: return@launch
                val datagramPacket = DatagramPacket(packet, packet.size, address, serverPort)
                socket?.send(datagramPacket)
                Timber.d("Sent command: ${packet.toHexString()}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send command")
            }
        }
    }
    
    fun getNextSequence(): Int {
        return sequenceCounter.incrementAndGet() and 0xFF
    }
    
    private fun ByteArray.toHexString(): String {
        return joinToString(" ") { String.format("%02X", it) }
    }
}

// 数据类定义

data class GimbalStatus(
    val gimbalConnected: Boolean,
    val cameraConnected: Boolean,
    val upgradeStatus: Int,
    val irNormal: Boolean,
    val telephotoNormal: Boolean,
    val wideNormal: Boolean,
    val laserNormal: Boolean,
    val stabilizeStatus: Int
)

data class GimbalAttitude(
    val yawJoint: Float,
    val rollJoint: Float,
    val pitchJoint: Float,
    val yawAttitude: Float,
    val rollAttitude: Float,
    val pitchAttitude: Float,
    val yawSpeed: Float,
    val pitchSpeed: Float,
    val rollSpeed: Float
)

enum class CameraMode {
    PHOTO, RECORD
}

data class CameraStatus(
    val mode: CameraMode,
    val resolution: Int,
    val codec: Int,
    val streamMode: Int,
    val bitrate: Int,
    val photoMode: Int,
    val timelapseInterval: Int,
    val burstCount: Int,
    val sdStatus: Int,
    val sdTotalCapacity: Float,  // MB
    val sdFreeCapacity: Float,   // MB
    val sdUsedCapacity: Float    // MB
)

data class IRStatus(
    val maxTemp: Float,      // °C
    val minTemp: Float,      // °C
    val centerTemp: Float,   // °C
    val pointTemp: Float,    // °C
    val avgTemp: Float,      // °C
    val maxTempX: Int,
    val maxTempY: Int,
    val minTempX: Int,
    val minTempY: Int,
    val centerTempX: Int,
    val centerTempY: Int,
    val pointTempX: Int,
    val pointTempY: Int,
    val highTempWarning: Boolean,
    val lowTempWarning: Boolean,
    val tempDiffWarning: Boolean,
    val thresholdWarning: Boolean
)

data class VisibleStatus(
    val zoomStatus: Int,
    val focalLength: Float,      // mm
    val zoomRate: Float,         // x
    val evValue: Int,
    val isoValue: Float,
    val shutter: Int,            // μs
    val aeLock: Boolean,
    val focusStatus: Int,
    val preciseCaptureFocal: Int
)
