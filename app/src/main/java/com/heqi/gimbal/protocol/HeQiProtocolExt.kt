package com.heqi.gimbal.protocol

/**
 * 禾启协议扩展功能
 * 包含更多高级控制指令
 */
object HeQiProtocolExt {
    
    // ==================== 可见光相机高级设置 ====================
    
    /**
     * 可见光录像分辨率设置指令 (0x000201)
     * @param resolution 0x08:1080P, 0x26:4K, 0x36:4000x3000
     */
    fun createVisibleVideoResPacket(resolution: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x00,  // 开始设置
            resolution.toByte()
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_VISIBLE_VIDEO_RES, payload, seq)
    }
    
    /**
     * 可见光拍照分辨率设置指令 (0x000202)
     * @param resolution 0x14:8000x6000, 0x15:4000x3000, 0x16:5160x3870, 0x17:5664x4248
     */
    fun createVisiblePhotoResPacket(resolution: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x00,  // 开始设置
            resolution.toByte()
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_VISIBLE_PHOTO_RES, payload, seq)
    }
    
    // ==================== 视频输出设置 ====================
    
    /**
     * 视频输出码流设置指令 (0x000308)
     * @param bitrate 1:1M, 2:1.5M, 3:2M, 4:4M, 5:8M, 6:12M
     */
    fun createSetBitratePacket(bitrate: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            bitrate.coerceIn(1, 6).toByte(),
            0x00  // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_SET_BITRATE, payload, seq)
    }
    
    /**
     * 视频输出分辨率设置指令 (0x00030A)
     * @param resolution 1:1080P30fps, 2:720P30fps
     */
    fun createSetResolutionPacket(resolution: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            resolution.toByte(),
            0x00  // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_SET_RESOLUTION, payload, seq)
    }
    
    /**
     * 视频编码格式设置指令 (0x00030B)
     * @param codec 0:H264, 1:H265
     */
    fun createSetCodecPacket(codec: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            codec.toByte(),
            0x00  // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_SET_CODEC, payload, seq)
    }
    
    // ==================== 红外相机高级设置 ====================
    
    /**
     * 红外区域测温设置指令 (0x000110)
     * @param width 区域框宽度
     * @param height 区域框高度
     * @param centerX 区域框中心X坐标
     * @param centerY 区域框中心Y坐标
     */
    fun createIRAreaTempPacket(
        width: Int,
        height: Int,
        centerX: Int,
        centerY: Int,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            (width and 0xFF).toByte(),
            ((width shr 8) and 0xFF).toByte(),
            (height and 0xFF).toByte(),
            ((height shr 8) and 0xFF).toByte(),
            (centerX and 0xFF).toByte(),
            ((centerX shr 8) and 0xFF).toByte(),
            (centerY and 0xFF).toByte(),
            ((centerY shr 8) and 0xFF).toByte(),
            0x00  // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_IR_AREA_TEMP, payload, seq)
    }
    
    /**
     * 红外温度信息叠加开关设置指令 (0x000125)
     * @param enable 是否开启温度信息叠加
     */
    fun createIRTempOverlayPacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00  // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_IR_TEMP_OVERLAY, payload, seq)
    }
    
    // ==================== 精准复拍 ====================
    
    /**
     * 指定相机精准复拍指令 (0x000307)
     * @param cameraMode 0:默认, 1:红外, 2:可见光, 3:红外+可见光
     * @param resolution 可见光拍照分辨率
     * @param zoomRate 可见光倍数 (单位0.1倍)
     * @param focalLength 精准复拍焦距
     */
    fun createPreciseCapturePacket(
        cameraMode: Int = 0,
        resolution: Int = 0x15,
        zoomRate: Int = 10,
        focalLength: Int = 0,
        folderName: String = "",
        fileName: String = "",
        seq: Int = 0
    ): ByteArray {
        val payload = ByteArray(58)
        payload[0] = cameraMode.toByte()
        payload[1] = resolution.toByte()
        payload[2] = (zoomRate and 0xFF).toByte()
        payload[3] = ((zoomRate shr 8) and 0xFF).toByte()
        payload[4] = (focalLength and 0xFF).toByte()
        payload[5] = ((focalLength shr 8) and 0xFF).toByte()
        
        // 文件夹名 (20字节)
        val folderBytes = folderName.toByteArray(Charsets.UTF_8)
        System.arraycopy(folderBytes, 0, payload, 6, minOf(folderBytes.size, 20))
        
        // 文件名 (32字节)
        val fileBytes = fileName.toByteArray(Charsets.UTF_8)
        System.arraycopy(fileBytes, 0, payload, 26, minOf(fileBytes.size, 32))
        
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_PRECISE_CAPTURE, payload, seq)
    }
    
    // ==================== 云台指点对准 ====================
    
    /**
     * 云台指点对准指令 (0x00002C)
     * @param lensType 0:长焦镜头, 1:广角镜头, 2:红外镜头
     * @param zoomRate 混合变倍倍率 (单位0.1倍)
     * @param pointX 指点对准X坐标 (1-1920)
     * @param pointY 指点对准Y坐标 (1-1080)
     */
    fun createPointingPacket(
        lensType: Int = 0,
        zoomRate: Int = 10,
        pointX: Int = 960,
        pointY: Int = 540,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            lensType.toByte(),
            (zoomRate and 0xFF).toByte(),
            ((zoomRate shr 8) and 0xFF).toByte(),
            (pointX and 0xFF).toByte(),
            ((pointX shr 8) and 0xFF).toByte(),
            (pointY and 0xFF).toByte(),
            ((pointY shr 8) and 0xFF).toByte()
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_POINTING, payload, seq)
    }
    
    // ==================== 云台调试指令 ====================
    
    /**
     * 云台一键校飘指令 (0x000013)
     */
    fun createGimbalCalibratePacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 开始校漂
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_CALIBRATE, payload, seq)
    }
    
    /**
     * 关闭云台伺服指令 (0x00002D)
     */
    fun createCloseServoPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x00,  // 关闭伺服
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_CLOSE_SERVO, payload, seq)
    }
    
    /**
     * 云台线性校准指令 (0x00002E)
     */
    fun createLinearCalibPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 线性校准
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_LINEAR_CALIB, payload, seq)
    }
    
    /**
     * 云台软重启指令 (0x00002F)
     */
    fun createSoftRebootPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 软重启
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_SOFT_REBOOT, payload, seq)
    }
    
    // ==================== 参数读取指令 ====================
    
    /**
     * 红外相机所有设置参数读取指令 (0x000100)
     */
    fun createIRGetParamsPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 读取参数
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_IR_GET_PARAMS, payload, seq)
    }
    
    /**
     * 可见光相机所有设置参数读取指令 (0x000200)
     */
    fun createVisibleGetParamsPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 读取参数
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_VISIBLE_GET_PARAMS, payload, seq)
    }
    
    /**
     * 获取云台版本号 (0x000018)
     */
    fun createGetGimbalVersionPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 获取版本
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GIMBAL_GET_VERSION, payload, seq)
    }
    
    /**
     * 获取相机版本号 (0x000317)
     */
    fun createGetCameraVersionPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 获取版本
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_GET_CAMERA_VERSION, payload, seq)
    }
    
    // ==================== 相机关机指令 ====================
    
    /**
     * 相机关机指令 (0x000316)
     */
    fun createShutdownPacket(seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x01,  // 即将关机
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_SHUTDOWN, payload, seq)
    }
    
    // ==================== OSD水印设置 ====================
    
    /**
     * 相机OSD水印开关 (0x000314)
     * @param enable 是否开启水印
     */
    fun createOSDSwitchPacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00,  // 预留
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_OSD_SWITCH, payload, seq)
    }
    
    // ==================== 激光周期测距 ====================
    
    /**
     * 激光周期测距设置指令 (0x000406)
     * @param enable 是否开启1秒周期测距
     */
    fun createLaserPeriodicRangePacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00   // 预留
        )
        return HeQiProtocol.buildPacket(HeQiProtocol.MSG_LASER_PERIODIC_RANGE, payload, seq)
    }
}

/**
 * 分辨率常量
 */
object VideoResolution {
    const val RES_1080P = 0x08
    const val RES_4K = 0x26
    const val RES_4000x3000 = 0x36
}

object PhotoResolution {
    const val RES_8000x6000 = 0x14
    const val RES_4000x3000 = 0x15
    const val RES_5160x3870 = 0x16
    const val RES_5664x4248 = 0x17
}

object Bitrate {
    const val B_1M = 1
    const val B_1_5M = 2
    const val B_2M = 3
    const val B_4M = 4
    const val B_8M = 5
    const val B_12M = 6
}

object LensType {
    const val TELEPHOTO = 0  // 长焦镜头
    const val WIDE = 1       // 广角镜头
    const val IR = 2         // 红外镜头
}
