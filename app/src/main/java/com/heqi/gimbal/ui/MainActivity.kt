package com.heqi.gimbal.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.heqi.gimbal.R
import com.heqi.gimbal.model.AppSettings
import com.heqi.gimbal.protocol.*
import com.heqi.gimbal.video.VideoPlayerManager
import kotlinx.coroutines.*
import timber.log.Timber
import tv.danmaku.ijk.media.player.widget.IjkVideoView

/**
 * 主活动 - 吊舱控制器主界面
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var videoView: IjkVideoView
    private lateinit var statusBar: View
    private lateinit var leftControlPanel: View
    private lateinit var rightControlPanel: View
    private lateinit var bottomControlPanel: View
    
    // 状态显示
    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvGimbalStatus: TextView
    private lateinit var tvCameraStatus: TextView
    private lateinit var tvZoomLevel: TextView
    private lateinit var tvRecordTime: TextView
    private lateinit var ivRecordIndicator: ImageView
    
    // 控制按钮
    private lateinit var btnPitchUp: ImageButton
    private lateinit var btnPitchDown: ImageButton
    private lateinit var btnYawLeft: ImageButton
    private lateinit var btnYawRight: ImageButton
    private lateinit var btnCenter: Button
    private lateinit var btnLookDown: Button
    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton
    private lateinit var btnTakePhoto: ImageButton
    private lateinit var btnRecord: ImageButton
    private lateinit var btnImageMode: Button
    private lateinit var btnLaser: Button
    private lateinit var btnSettings: ImageButton
    private lateinit var btnConnect: Button
    
    private lateinit var protocolManager: ProtocolManager
    private lateinit var videoManager: VideoPlayerManager
    
    private var settings: AppSettings = AppSettings()
    private var isRecording = false
    private var recordStartTime: Long = 0
    private var recordTimeHandler: Handler? = null
    private var currentImageMode = HeQiProtocol.IMAGE_MODE_VISIBLE
    private var isConnected = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var gimbalControlRunnable: Runnable? = null
    
    // 当前控制状态
    private var pitchDirection = HeQiProtocol.DIRECTION_STOP
    private var yawDirection = HeQiProtocol.DIRECTION_STOP
    private var zoomDirection = HeQiProtocol.ZOOM_STOP
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 全屏设置
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_main)
        
        initManagers()
        initViews()
        setupListeners()
        setupCallbacks()
        
        loadSettings()
    }
    
    private fun initManagers() {
        protocolManager = ProtocolManager.getInstance(this)
        videoManager = VideoPlayerManager.getInstance(this)
    }
    
    private fun initViews() {
        videoView = findViewById(R.id.videoView)
        statusBar = findViewById(R.id.statusBar)
        leftControlPanel = findViewById(R.id.leftControlPanel)
        rightControlPanel = findViewById(R.id.rightControlPanel)
        bottomControlPanel = findViewById(R.id.bottomControlPanel)
        
        // 状态显示
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        tvGimbalStatus = findViewById(R.id.tvGimbalStatus)
        tvCameraStatus = findViewById(R.id.tvCameraStatus)
        tvZoomLevel = findViewById(R.id.tvZoomLevel)
        tvRecordTime = findViewById(R.id.tvRecordTime)
        ivRecordIndicator = findViewById(R.id.ivRecordIndicator)
        
        // 云台控制按钮
        btnPitchUp = findViewById(R.id.btnPitchUp)
        btnPitchDown = findViewById(R.id.btnPitchDown)
        btnYawLeft = findViewById(R.id.btnYawLeft)
        btnYawRight = findViewById(R.id.btnYawRight)
        btnCenter = findViewById(R.id.btnCenter)
        btnLookDown = findViewById(R.id.btnLookDown)
        
        // 相机控制按钮
        btnZoomIn = findViewById(R.id.btnZoomIn)
        btnZoomOut = findViewById(R.id.btnZoomOut)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnRecord = findViewById(R.id.btnRecord)
        btnImageMode = findViewById(R.id.btnImageMode)
        btnLaser = findViewById(R.id.btnLaser)
        btnSettings = findViewById(R.id.btnSettings)
        btnConnect = findViewById(R.id.btnConnect)
    }
    
    private fun setupListeners() {
        // 云台俯仰控制
        btnPitchUp.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pitchDirection = HeQiProtocol.DIRECTION_UP
                    startGimbalControl()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    pitchDirection = HeQiProtocol.DIRECTION_STOP
                    stopGimbalControl()
                    true
                }
                else -> false
            }
        }
        
        btnPitchDown.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pitchDirection = HeQiProtocol.DIRECTION_DOWN
                    startGimbalControl()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    pitchDirection = HeQiProtocol.DIRECTION_STOP
                    stopGimbalControl()
                    true
                }
                else -> false
            }
        }
        
        // 云台偏航控制
        btnYawLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    yawDirection = HeQiProtocol.DIRECTION_LEFT
                    startGimbalControl()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    yawDirection = HeQiProtocol.DIRECTION_STOP
                    stopGimbalControl()
                    true
                }
                else -> false
            }
        }
        
        btnYawRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    yawDirection = HeQiProtocol.DIRECTION_RIGHT
                    startGimbalControl()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    yawDirection = HeQiProtocol.DIRECTION_STOP
                    stopGimbalControl()
                    true
                }
                else -> false
            }
        }
        
        // 回中和下视
        btnCenter.setOnClickListener {
            sendGimbalCenter()
        }
        
        btnLookDown.setOnClickListener {
            sendGimbalLookDown()
        }
        
        // 变倍控制
        btnZoomIn.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendContinuousZoom(HeQiProtocol.ZOOM_IN_CONTINUOUS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    sendContinuousZoom(HeQiProtocol.ZOOM_STOP)
                    true
                }
                else -> false
            }
        }
        
        btnZoomOut.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendContinuousZoom(HeQiProtocol.ZOOM_OUT_CONTINUOUS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    sendContinuousZoom(HeQiProtocol.ZOOM_STOP)
                    true
                }
                else -> false
            }
        }
        
        // 拍照
        btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        
        // 录像
        btnRecord.setOnClickListener {
            toggleRecord()
        }
        
        // 图像模式切换
        btnImageMode.setOnClickListener {
            toggleImageMode()
        }
        
        // 激光测距
        btnLaser.setOnClickListener {
            toggleLaser()
        }
        
        // 设置
        btnSettings.setOnClickListener {
            openSettings()
        }
        
        // 连接按钮
        btnConnect.setOnClickListener {
            toggleConnection()
        }
    }
    
    private fun setupCallbacks() {
        // 协议管理器回调
        protocolManager.onGimbalStatusReceived = { status ->
            runOnUiThread {
                updateGimbalStatus(status)
            }
        }
        
        protocolManager.onGimbalAttitudeReceived = { attitude ->
            runOnUiThread {
                updateGimbalAttitude(attitude)
            }
        }
        
        protocolManager.onCameraStatusReceived = { status ->
            runOnUiThread {
                updateCameraStatus(status)
            }
        }
        
        protocolManager.onIRStatusReceived = { status ->
            runOnUiThread {
                updateIRStatus(status)
            }
        }
        
        protocolManager.onVisibleStatusReceived = { status ->
            runOnUiThread {
                updateVisibleStatus(status)
            }
        }
        
        protocolManager.onACKReceived = { msgId, ackCode ->
            Timber.d("ACK received: msgId=0x${msgId.toString(16)}, ackCode=0x${ackCode.toString(16)}")
            if (ackCode != HeQiProtocol.ACK_OK) {
                runOnUiThread {
                    showError("命令执行失败: 0x${ackCode.toString(16)}")
                }
            }
        }
        
        protocolManager.onLaserRangeReceived = { distance ->
            runOnUiThread {
                Toast.makeText(this, "测距: ${distance}m", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 视频播放器回调
        videoManager.onStateChanged = { state ->
            runOnUiThread {
                when (state) {
                    VideoPlayerManager.STATE_PLAYING -> {
                        tvConnectionStatus.text = "视频已连接"
                        tvConnectionStatus.setTextColor(getColor(R.color.status_green))
                    }
                    VideoPlayerManager.STATE_ERROR -> {
                        tvConnectionStatus.text = "视频连接失败"
                        tvConnectionStatus.setTextColor(getColor(R.color.recording_red))
                    }
                }
            }
        }
        
        videoManager.onError = { what, extra ->
            Timber.e("Video error: what=$what, extra=$extra")
        }
    }
    
    private fun loadSettings() {
        settings = AppSettings.load(this)
        protocolManager.updateSettings(settings.controlIp, settings.controlPort)
        videoManager.updateSettings(
            bufferTimeMs = settings.bufferTime,
            autoReconnect = settings.enableReconnect,
            reconnectDelayMs = settings.reconnectDelay
        )
    }
    
    private fun toggleConnection() {
        if (isConnected) {
            disconnect()
        } else {
            connect()
        }
    }
    
    private fun connect() {
        btnConnect.text = "连接中..."
        btnConnect.isEnabled = false
        
        lifecycleScope.launch {
            // 连接协议
            val protocolConnected = withContext(Dispatchers.IO) {
                protocolManager.connect()
            }
            
            if (protocolConnected) {
                isConnected = true
                btnConnect.text = "断开"
                btnConnect.isEnabled = true
                tvConnectionStatus.text = "已连接"
                tvConnectionStatus.setTextColor(getColor(R.color.status_green))
                
                // 启动视频播放
                startVideoPlayback()
            } else {
                btnConnect.text = "连接"
                btnConnect.isEnabled = true
                showError("连接失败")
            }
        }
    }
    
    private fun disconnect() {
        protocolManager.disconnect()
        videoManager.stop()
        isConnected = false
        btnConnect.text = "连接"
        tvConnectionStatus.text = "未连接"
        tvConnectionStatus.setTextColor(getColor(R.color.text_secondary))
    }
    
    private fun startVideoPlayback() {
        videoManager.setDisplay(videoView.holder)
        videoManager.start(settings.rtspUrl)
    }
    
    // ==================== 云台控制 ====================
    
    private fun startGimbalControl() {
        gimbalControlRunnable?.let { handler.removeCallbacks(it) }
        
        gimbalControlRunnable = object : Runnable {
            override fun run() {
                sendGimbalControl()
                handler.postDelayed(this, 100)  // 100ms发送一次
            }
        }
        handler.post(gimbalControlRunnable!!)
    }
    
    private fun stopGimbalControl() {
        gimbalControlRunnable?.let { handler.removeCallbacks(it) }
        gimbalControlRunnable = null
        
        // 发送停止指令
        val packet = HeQiProtocol.createGimbalControlPacket(
            mode = HeQiProtocol.GIMBAL_MODE_NONE,
            yawDirection = HeQiProtocol.DIRECTION_STOP,
            pitchDirection = HeQiProtocol.DIRECTION_STOP,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
    }
    
    private fun sendGimbalControl() {
        val packet = HeQiProtocol.createGimbalControlPacket(
            mode = HeQiProtocol.GIMBAL_MODE_NONE,
            yawDirection = yawDirection,
            pitchDirection = pitchDirection,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
    }
    
    private fun sendGimbalCenter() {
        val packet = HeQiProtocol.createGimbalCenterPacket(protocolManager.getNextSequence())
        protocolManager.sendCommand(packet)
        Toast.makeText(this, "云台回中", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendGimbalLookDown() {
        val packet = HeQiProtocol.createGimbalLookDownPacket(protocolManager.getNextSequence())
        protocolManager.sendCommand(packet)
        Toast.makeText(this, "云台下视90°", Toast.LENGTH_SHORT).show()
    }
    
    // ==================== 相机控制 ====================
    
    private fun sendContinuousZoom(control: Int) {
        val packet = HeQiProtocol.createContinuousZoomPacket(
            control = control,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
    }
    
    private fun takePhoto() {
        val packet = HeQiProtocol.createTakePhotoPacket(
            cameraMode = when (currentImageMode) {
                HeQiProtocol.IMAGE_MODE_IR -> 1
                HeQiProtocol.IMAGE_MODE_VISIBLE -> 2
                else -> 0
            },
            action = 0,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
        Toast.makeText(this, "拍照", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleRecord() {
        if (isRecording) {
            // 停止录像
            val packet = HeQiProtocol.createRecordPacket(
                cameraMode = when (currentImageMode) {
                    HeQiProtocol.IMAGE_MODE_IR -> 1
                    HeQiProtocol.IMAGE_MODE_VISIBLE -> 2
                    else -> 0
                },
                action = 2,
                seq = protocolManager.getNextSequence()
            )
            protocolManager.sendCommand(packet)
            stopRecordTimer()
            isRecording = false
            btnRecord.setImageResource(R.drawable.ic_videocam)
            ivRecordIndicator.visibility = View.GONE
            tvRecordTime.visibility = View.GONE
        } else {
            // 开始录像
            val packet = HeQiProtocol.createRecordPacket(
                cameraMode = when (currentImageMode) {
                    HeQiProtocol.IMAGE_MODE_IR -> 1
                    HeQiProtocol.IMAGE_MODE_VISIBLE -> 2
                    else -> 0
                },
                action = 1,
                seq = protocolManager.getNextSequence()
            )
            protocolManager.sendCommand(packet)
            startRecordTimer()
            isRecording = true
            btnRecord.setImageResource(R.drawable.ic_stop)
            ivRecordIndicator.visibility = View.VISIBLE
            tvRecordTime.visibility = View.VISIBLE
        }
    }
    
    private fun startRecordTimer() {
        recordStartTime = System.currentTimeMillis()
        recordTimeHandler = Handler(Looper.getMainLooper())
        recordTimeHandler?.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - recordStartTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / 1000) / 60
                tvRecordTime.text = String.format("%02d:%02d", minutes, seconds)
                recordTimeHandler?.postDelayed(this, 1000)
            }
        })
    }
    
    private fun stopRecordTimer() {
        recordTimeHandler?.removeCallbacksAndMessages(null)
        recordTimeHandler = null
    }
    
    private fun toggleImageMode() {
        currentImageMode = when (currentImageMode) {
            HeQiProtocol.IMAGE_MODE_VISIBLE -> HeQiProtocol.IMAGE_MODE_IR
            HeQiProtocol.IMAGE_MODE_IR -> HeQiProtocol.IMAGE_MODE_SPLIT
            else -> HeQiProtocol.IMAGE_MODE_VISIBLE
        }
        
        val packet = HeQiProtocol.createSetImageModePacket(
            mode = currentImageMode,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
        
        val modeName = when (currentImageMode) {
            HeQiProtocol.IMAGE_MODE_VISIBLE -> "可见光"
            HeQiProtocol.IMAGE_MODE_IR -> "红外"
            else -> "分屏"
        }
        btnImageMode.text = modeName
        Toast.makeText(this, "切换到$modeName", Toast.LENGTH_SHORT).show()
    }
    
    private var laserEnabled = false
    
    private fun toggleLaser() {
        laserEnabled = !laserEnabled
        val packet = HeQiProtocol.createLaserRangePacket(
            enable = laserEnabled,
            seq = protocolManager.getNextSequence()
        )
        protocolManager.sendCommand(packet)
        btnLaser.isSelected = laserEnabled
        Toast.makeText(this, if (laserEnabled) "激光测距开启" else "激光测距关闭", Toast.LENGTH_SHORT).show()
    }
    
    // ==================== 状态更新 ====================
    
    private fun updateGimbalStatus(status: GimbalStatus) {
        val statusText = buildString {
            append("云台:")
            append(if (status.gimbalConnected) "正常" else "异常")
            append(" | 相机:")
            append(if (status.cameraConnected) "正常" else "异常")
        }
        tvGimbalStatus.text = statusText
    }
    
    private fun updateGimbalAttitude(attitude: GimbalAttitude) {
        // 可以显示姿态角度
    }
    
    private fun updateCameraStatus(status: CameraStatus) {
        val modeText = if (status.mode == CameraMode.PHOTO) "拍照" else "录像"
        val sdText = when (status.sdStatus) {
            0 -> "SD卡正常"
            3 -> "无SD卡"
            4 -> "SD卡已满"
            else -> "SD卡异常"
        }
        tvCameraStatus.text = "$modeText | $sdText | 剩余:${status.sdFreeCapacity.toInt()}MB"
    }
    
    private fun updateIRStatus(status: IRStatus) {
        // 红外温度信息显示
    }
    
    private fun updateVisibleStatus(status: VisibleStatus) {
        tvZoomLevel.text = "${status.zoomRate}x"
    }
    
    // ==================== 其他 ====================
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onResume() {
        super.onResume()
        loadSettings()
        if (isConnected) {
            videoManager.resume()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (isConnected) {
            videoManager.pause()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        videoManager.release()
    }
}
