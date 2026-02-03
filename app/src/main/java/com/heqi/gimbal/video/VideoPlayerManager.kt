package com.heqi.gimbal.video

import android.content.Context
import android.content.SharedPreferences
import android.view.Surface
import android.view.SurfaceHolder
import kotlinx.coroutines.*
import timber.log.Timber
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * 视频播放器管理器 - 基于IJKPlayer实现RTSP低延迟播放
 */
class VideoPlayerManager private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var instance: VideoPlayerManager? = null
        
        fun getInstance(context: Context): VideoPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: VideoPlayerManager(context.applicationContext).also { instance = it }
            }
        }
        
        // 播放状态
        const val STATE_IDLE = 0
        const val STATE_PREPARING = 1
        const val STATE_PREPARED = 2
        const val STATE_PLAYING = 3
        const val STATE_PAUSED = 4
        const val STATE_ERROR = 5
        const val STATE_COMPLETED = 6
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences("video_settings", Context.MODE_PRIVATE)
    
    private var mediaPlayer: IjkMediaPlayer? = null
    private var currentUrl: String = ""
    private var currentState: Int = STATE_IDLE
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var reconnectJob: Job? = null
    
    // 配置参数
    private var bufferTime: Int = 100      // 缓冲区时间 (ms)
    private var minFrames: Int = 3         // 最小帧数
    private var maxBufferSize: Int = 1024 * 1024  // 最大缓冲区大小 (1MB)
    private var reconnectDelay: Long = 3000  // 重连延迟 (ms)
    private var enableReconnect: Boolean = true
    
    // 回调
    var onStateChanged: ((Int) -> Unit)? = null
    var onError: ((Int, Int) -> Unit)? = null  // what, extra
    var onInfo: ((Int, Int) -> Unit)? = null   // what, extra
    var onVideoSizeChanged: ((Int, Int) -> Unit)? = null  // width, height
    var onBufferingUpdate: ((Int) -> Unit)? = null  // percent
    
    init {
        loadSettings()
        initPlayer()
    }
    
    private fun loadSettings() {
        bufferTime = prefs.getInt("buffer_time", 100)
        minFrames = prefs.getInt("min_frames", 3)
        maxBufferSize = prefs.getInt("max_buffer_size", 1024 * 1024)
        reconnectDelay = prefs.getLong("reconnect_delay", 3000)
        enableReconnect = prefs.getBoolean("enable_reconnect", true)
    }
    
    fun updateSettings(
        bufferTimeMs: Int = this.bufferTime,
        minFramesCount: Int = this.minFrames,
        maxBuffer: Int = this.maxBufferSize,
        reconnectDelayMs: Long = this.reconnectDelay,
        autoReconnect: Boolean = this.enableReconnect
    ) {
        prefs.edit().apply {
            putInt("buffer_time", bufferTimeMs)
            putInt("min_frames", minFramesCount)
            putInt("max_buffer_size", maxBuffer)
            putLong("reconnect_delay", reconnectDelayMs)
            putBoolean("enable_reconnect", autoReconnect)
            apply()
        }
        
        this.bufferTime = bufferTimeMs
        this.minFrames = minFramesCount
        this.maxBufferSize = maxBuffer
        this.reconnectDelay = reconnectDelayMs
        this.enableReconnect = autoReconnect
    }
    
    private fun initPlayer() {
        releasePlayer()
        
        try {
            mediaPlayer = IjkMediaPlayer().apply {
                // 设置日志级别
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO)
                
                // 启用硬解码
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L)
                
                // RTSP低延迟优化设置
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp")
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp")
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L * 32)
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 500000L)
                
                // 缓冲区优化
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", maxBufferSize.toLong())
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", minFrames.toLong())
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 1000L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "low_latency", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1L)
                
                // 禁用音频 (如果不需要)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "an", 1L)
                
                // 设置监听器
                setOnPreparedListener { mp ->
                    Timber.d("Player prepared")
                    currentState = STATE_PREPARED
                    onStateChanged?.invoke(STATE_PREPARED)
                    mp.start()
                    currentState = STATE_PLAYING
                    onStateChanged?.invoke(STATE_PLAYING)
                }
                
                setOnCompletionListener {
                    Timber.d("Playback completed")
                    currentState = STATE_COMPLETED
                    onStateChanged?.invoke(STATE_COMPLETED)
                    if (enableReconnect) {
                        scheduleReconnect()
                    }
                }
                
                setOnErrorListener { _, what, extra ->
                    Timber.e("Player error: what=$what, extra=$extra")
                    currentState = STATE_ERROR
                    onStateChanged?.invoke(STATE_ERROR)
                    onError?.invoke(what, extra)
                    if (enableReconnect) {
                        scheduleReconnect()
                    }
                    true
                }
                
                setOnInfoListener { _, what, extra ->
                    Timber.d("Player info: what=$what, extra=$extra")
                    onInfo?.invoke(what, extra)
                    when (what) {
                        IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                            Timber.d("Buffering start")
                        }
                        IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                            Timber.d("Buffering end")
                        }
                        IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                            Timber.d("First frame rendered")
                        }
                    }
                    true
                }
                
                setOnBufferingUpdateListener { _, percent ->
                    onBufferingUpdate?.invoke(percent)
                }
                
                setOnVideoSizeChangedListener { _, width, height, _, _ ->
                    Timber.d("Video size changed: ${width}x$height")
                    onVideoSizeChanged?.invoke(width, height)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize player")
            currentState = STATE_ERROR
            onStateChanged?.invoke(STATE_ERROR)
        }
    }
    
    fun setDisplay(surface: Surface?) {
        try {
            mediaPlayer?.setSurface(surface)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set display")
        }
    }
    
    fun setDisplay(holder: SurfaceHolder?) {
        try {
            mediaPlayer?.setDisplay(holder)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set display holder")
        }
    }
    
    fun start(url: String) {
        if (url.isEmpty()) {
            Timber.w("Empty URL, cannot start playback")
            return
        }
        
        if (currentUrl == url && currentState == STATE_PLAYING) {
            Timber.d("Already playing this URL")
            return
        }
        
        currentUrl = url
        
        try {
            cancelReconnect()
            
            if (currentState == STATE_ERROR) {
                initPlayer()
            }
            
            mediaPlayer?.apply {
                reset()
                setDataSource(url)
                currentState = STATE_PREPARING
                onStateChanged?.invoke(STATE_PREPARING)
                prepareAsync()
                Timber.d("Starting playback: $url")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to start playback")
            currentState = STATE_ERROR
            onStateChanged?.invoke(STATE_ERROR)
            if (enableReconnect) {
                scheduleReconnect()
            }
        }
    }
    
    fun pause() {
        try {
            if (currentState == STATE_PLAYING) {
                mediaPlayer?.pause()
                currentState = STATE_PAUSED
                onStateChanged?.invoke(STATE_PAUSED)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to pause playback")
        }
    }
    
    fun resume() {
        try {
            if (currentState == STATE_PAUSED) {
                mediaPlayer?.start()
                currentState = STATE_PLAYING
                onStateChanged?.invoke(STATE_PLAYING)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to resume playback")
        }
    }
    
    fun stop() {
        try {
            cancelReconnect()
            mediaPlayer?.stop()
            currentState = STATE_IDLE
            onStateChanged?.invoke(STATE_IDLE)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop playback")
        }
    }
    
    fun release() {
        cancelReconnect()
        releasePlayer()
        scope.cancel()
    }
    
    private fun releasePlayer() {
        try {
            mediaPlayer?.apply {
                stop()
                setDisplay(null)
                setSurface(null)
                release()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error releasing player")
        } finally {
            mediaPlayer = null
            currentState = STATE_IDLE
        }
    }
    
    private fun scheduleReconnect() {
        cancelReconnect()
        reconnectJob = scope.launch {
            delay(reconnectDelay)
            Timber.d("Attempting to reconnect...")
            if (currentUrl.isNotEmpty()) {
                initPlayer()
                start(currentUrl)
            }
        }
    }
    
    private fun cancelReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
    }
    
    fun getCurrentState(): Int = currentState
    
    fun getCurrentUrl(): String = currentUrl
    
    fun isPlaying(): Boolean = currentState == STATE_PLAYING
    
    /**
     * 截图功能
     */
    fun takeSnapshot(): Boolean {
        // IJKPlayer不直接支持截图，需要在Surface层面实现
        // 可以通过TextureView的getBitmap()或SurfaceView的截图方式实现
        Timber.d("Snapshot requested")
        return false
    }
    
    /**
     * 获取当前播放时间
     */
    fun getCurrentPosition(): Long {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取视频总时长
     */
    fun getDuration(): Long {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
