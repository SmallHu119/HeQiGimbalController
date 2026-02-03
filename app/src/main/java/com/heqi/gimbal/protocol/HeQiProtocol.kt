package com.heqi.gimbal.protocol

/**
 * 禾启K40T-MINI四光云台协议实现
 * 协议版本: V1.4.13
 */
object HeQiProtocol {
    
    // 协议常量
    const val STX: Byte = 0xFD.toByte()  // 数据包启动标记
    const val APP_SYSTEM_ID: Byte = 0x01  // APP系统ID
    const val APP_COMPONENT_ID: Byte = 0x01  // APP组件ID
    const val GIMBAL_SYSTEM_ID: Byte = 0x04  // 四光吊舱系统ID
    const val GIMBAL_COMPONENT_ID: Byte = 0x01  // 四光吊舱组件ID
    
    // 消息ID定义 - 云台控制
    const val MSG_GIMBAL_STATUS: Int = 0x000001  // 云台状态消息
    const val MSG_GIMBAL_ATTITUDE: Int = 0x000002  // 云台姿态信息
    const val MSG_GIMBAL_CONTROL: Int = 0x000010  // 云台控制指令
    const val MSG_GIMBAL_SET_ANGLE: Int = 0x000012  // 指定云台角度控制指令
    const val MSG_GIMBAL_CALIBRATE: Int = 0x000013  // 云台一键校飘指令
    const val MSG_GIMBAL_SET_SPEED: Int = 0x000017  // 云台波轮速度设置指令
    const val MSG_GIMBAL_GET_VERSION: Int = 0x000018  // 获取云台版本号
    const val MSG_GIMBAL_POINTING: Int = 0x00002C  // 云台指点对准指令
    const val MSG_GIMBAL_CLOSE_SERVO: Int = 0x00002D  // 关闭云台伺服指令
    const val MSG_GIMBAL_LINEAR_CALIB: Int = 0x00002E  // 云台线性校准指令
    const val MSG_GIMBAL_SOFT_REBOOT: Int = 0x00002F  // 云台软重启指令
    const val MSG_GIMBAL_USE_FC_ATTITUDE: Int = 0x000030  // 云台使用飞控假姿态指令
    const val MSG_GIMBAL_CALIB_ACCEL: Int = 0x000031  // 云台校准运动加速度偏置指令
    const val MSG_GIMBAL_STABILIZE: Int = 0x000033  // 云台稳像指令
    
    // 消息ID定义 - 相机控制
    const val MSG_CAMERA_STATUS: Int = 0x000003  // 相机系统状态反馈
    const val MSG_IR_STATUS: Int = 0x000004  // 红外相机状态反馈
    const val MSG_VISIBLE_STATUS: Int = 0x000005  // 可见光相机状态反馈
    
    // 消息ID定义 - 红外相机设置
    const val MSG_IR_GET_PARAMS: Int = 0x000100  // 红外相机所有设置参数读取指令
    const val MSG_IR_ZOOM: Int = 0x000105  // 红外电子放大设置指令
    const val MSG_IR_PALETTE: Int = 0x000106  // 红外伪彩设置指令
    const val MSG_IR_TEMP_SWITCH: Int = 0x000108  // 红外测温开关指令
    const val MSG_IR_POINT_TEMP: Int = 0x00010F  // 红外点测温设置指令
    const val MSG_IR_AREA_TEMP: Int = 0x000110  // 红外区域测温设置指令
    const val MSG_IR_TEMP_OVERLAY: Int = 0x000125  // 红外相机测温度信息叠加开关设置指令
    const val MSG_IR_SUPER_RES: Int = 0x000180  // 红外超分开关指令
    
    // 消息ID定义 - 可见光相机设置
    const val MSG_VISIBLE_GET_PARAMS: Int = 0x000200  // 可见光相机所有设置参数读取指令
    const val MSG_VISIBLE_VIDEO_RES: Int = 0x000201  // 可见光录像分辨率设置指令
    const val MSG_VISIBLE_PHOTO_RES: Int = 0x000202  // 可见光拍照分辨率设置指令
    
    // 消息ID定义 - 通用控制
    const val MSG_SET_MODE: Int = 0x000300  // 拍照、录像模式设置指令
    const val MSG_SET_PHOTO_PARAMS: Int = 0x000301  // 拍照参数设置指令
    const val MSG_TAKE_PHOTO: Int = 0x000302  // 拍照指令
    const val MSG_RECORD: Int = 0x000303  // 录像指令
    const val MSG_SET_ZOOM: Int = 0x000304  // 指定混合变倍指令
    const val MSG_CONTINUOUS_ZOOM: Int = 0x000306  // 连续混合变倍指令
    const val MSG_PRECISE_CAPTURE: Int = 0x000307  // 指定相机精准复拍指令
    const val MSG_SET_BITRATE: Int = 0x000308  // 视频输出码流设置指令
    const val MSG_SET_RESOLUTION: Int = 0x00030A  // 视频输出分辨率设置指令
    const val MSG_SET_CODEC: Int = 0x00030B  // 视频编码格式设置指令
    const val MSG_SET_TIME: Int = 0x00030E  // 云台相机授时指令
    const val MSG_GET_IP: Int = 0x000312  // 相机IP地址获取指令
    const val MSG_OSD_SWITCH: Int = 0x000314  // 相机OSD水印开关
    const val MSG_SHUTDOWN: Int = 0x000316  // 相机关机指令
    const val MSG_GET_CAMERA_VERSION: Int = 0x000317  // 获取相机版本号
    const val MSG_SET_IMAGE_MODE: Int = 0x000318  // 图像模式设置指令
    const val MSG_AI_DETECT: Int = 0x000319  // 智能识别指令
    const val MSG_TRACK_TARGET: Int = 0x000324  // 框选目标追踪指令
    
    // 消息ID定义 - 激光
    const val MSG_LASER_RANGE: Int = 0x000400  // 激光测距设置指令
    const val MSG_LASER_PERIODIC_RANGE: Int = 0x000406  // 激光周期测距设置指令
    
    // ACK响应码
    const val ACK_OK: Int = 0x0000  // 成功
    const val ACK_FAIL: Int = 0x0001  // 失败
    const val ACK_UNKNOWN_ERROR: Int = 0x0002  // 未知错误
    const val ACK_CRC_FAIL: Int = 0x0003  // 校验失败
    const val ACK_TIMEOUT: Int = 0x0004  // 超时
    
    // 云台工作模式
    const val GIMBAL_MODE_NONE: Int = 0x00  // 无操作
    const val GIMBAL_MODE_CENTER: Int = 0x10  // 云台回中
    const val GIMBAL_MODE_LOOK_DOWN: Int = 0x20  // 云台下视90°
    
    // 运动方向
    const val DIRECTION_LEFT: Int = 0  // 向左
    const val DIRECTION_RIGHT: Int = 1  // 向右
    const val DIRECTION_UP: Int = 0  // 向上
    const val DIRECTION_DOWN: Int = 1  // 向下
    const val DIRECTION_STOP: Int = 2  // 停止
    
    // 相机模式
    const val MODE_PHOTO: Int = 0  // 拍照模式
    const val MODE_RECORD: Int = 1  // 录像模式
    
    // 拍照模式
    const val PHOTO_MODE_SINGLE: Int = 0x00  // 单拍
    const val PHOTO_MODE_BURST: Int = 0x01  // 连拍
    const val PHOTO_MODE_TIMELAPSE: Int = 0x02  // 延时拍
    
    // 图像模式
    const val IMAGE_MODE_IR: Int = 0x00  // 红外图像
    const val IMAGE_MODE_VISIBLE: Int = 0x05  // 可见光
    const val IMAGE_MODE_SPLIT: Int = 0x07  // 分屏
    
    // 变倍控制
    const val ZOOM_IN_CONTINUOUS: Int = 0x00  // 连续放大
    const val ZOOM_OUT_CONTINUOUS: Int = 0x01  // 连续缩小
    const val ZOOM_STOP: Int = 0x02  // 停止变倍
    const val ZOOM_IN_STEP: Int = 0x03  // 放大
    const val ZOOM_OUT_STEP: Int = 0x04  // 缩小
    
    // 红外伪彩
    val IR_PALETTES = listOf(
        "白热", "黑体", "彩虹", "高度对比彩虹", "铁红", "岩浆",
        "天空", "中灰", "灰红", "紫橙", "特殊1", "警示红",
        "冰火", "青红", "特殊2", "渐变红", "渐变绿", "渐变黄",
        "警示绿", "警示蓝"
    )
    
    /**
     * CRC校验计算
     * X.25 CRC算法
     */
    fun calculateCRC(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
        var crc = 0xFFFF
        for (i in offset until offset + length) {
            var tmp = (data[i].toInt() and 0xFF) xor (crc and 0xFF)
            tmp = tmp xor (tmp shl 4)
            crc = (crc shr 8) xor (tmp shl 8) xor (tmp shl 3) xor (tmp shr 4)
            crc = crc and 0xFFFF
        }
        return crc
    }
    
    /**
     * 构建协议数据包
     * @param msgId 消息ID
     * @param payload 有效载荷
     * @param seq 序列号
     * @return 完整的数据包
     */
    fun buildPacket(msgId: Int, payload: ByteArray, seq: Int = 0): ByteArray {
        val payloadLength = payload.size
        val packetLength = 10 + payloadLength + 2  // 头部10字节 + 载荷 + CRC2字节
        val packet = ByteArray(packetLength)
        
        var index = 0
        
        // STX - 启动标记
        packet[index++] = STX
        
        // 载荷长度
        packet[index++] = payloadLength.toByte()
        
        // 接收者系统ID和组件ID (吊舱)
        packet[index++] = GIMBAL_SYSTEM_ID
        packet[index++] = GIMBAL_COMPONENT_ID
        
        // 序列号
        packet[index++] = (seq and 0xFF).toByte()
        
        // 发送者系统ID和组件ID (APP)
        packet[index++] = APP_SYSTEM_ID
        packet[index++] = APP_COMPONENT_ID
        
        // 消息ID (低、中、高字节)
        packet[index++] = (msgId and 0xFF).toByte()
        packet[index++] = ((msgId shr 8) and 0xFF).toByte()
        packet[index++] = ((msgId shr 16) and 0xFF).toByte()
        
        // 有效载荷
        System.arraycopy(payload, 0, packet, index, payloadLength)
        index += payloadLength
        
        // CRC校验 (从STX之后开始计算)
        val crc = calculateCRC(packet, 1, packetLength - 3)
        packet[index++] = (crc and 0xFF).toByte()
        packet[index] = ((crc shr 8) and 0xFF).toByte()
        
        return packet
    }
    
    /**
     * 解析协议数据包
     */
    fun parsePacket(data: ByteArray): HeQiPacket? {
        if (data.size < 12) return null
        if (data[0] != STX) return null
        
        val payloadLength = data[1].toInt() and 0xFF
        if (data.size != 10 + payloadLength + 2) return null
        
        // 验证CRC
        val receivedCrc = (data[data.size - 2].toInt() and 0xFF) or 
                         ((data[data.size - 1].toInt() and 0xFF) shl 8)
        val calculatedCrc = calculateCRC(data, 1, data.size - 3)
        if (receivedCrc != calculatedCrc) return null
        
        val targetSystem = data[2]
        val targetComponent = data[3]
        val sequence = data[4].toInt() and 0xFF
        val sourceSystem = data[5]
        val sourceComponent = data[6]
        
        val msgId = (data[7].toInt() and 0xFF) or 
                   ((data[8].toInt() and 0xFF) shl 8) or 
                   ((data[9].toInt() and 0xFF) shl 16)
        
        val payload = ByteArray(payloadLength)
        System.arraycopy(data, 10, payload, 0, payloadLength)
        
        return HeQiPacket(
            msgId = msgId,
            payload = payload,
            sequence = sequence,
            targetSystem = targetSystem,
            targetComponent = targetComponent,
            sourceSystem = sourceSystem,
            sourceComponent = sourceComponent
        )
    }
    
    // ==================== 云台控制指令 ====================
    
    /**
     * 云台控制指令 (0x000010)
     * @param mode 工作模式 (高4位)
     * @param yawDirection 方位运动方向 (0:左, 1:右, 2:停止)
     * @param pitchDirection 俯仰运动方向 (0:上, 1:下, 2:停止)
     */
    fun createGimbalControlPacket(
        mode: Int = GIMBAL_MODE_NONE,
        yawDirection: Int = DIRECTION_STOP,
        pitchDirection: Int = DIRECTION_STOP,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            mode.toByte(),
            yawDirection.toByte(),
            pitchDirection.toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_GIMBAL_CONTROL, payload, seq)
    }
    
    /**
     * 云台回中指令
     */
    fun createGimbalCenterPacket(seq: Int = 0): ByteArray {
        return createGimbalControlPacket(mode = GIMBAL_MODE_CENTER, seq = seq)
    }
    
    /**
     * 云台下视90°指令
     */
    fun createGimbalLookDownPacket(seq: Int = 0): ByteArray {
        return createGimbalControlPacket(mode = GIMBAL_MODE_LOOK_DOWN, seq = seq)
    }
    
    /**
     * 指定云台角度控制指令 (0x000012)
     * @param pitchDirection 俯仰方向 (0:向上, 1:向下, 2:无运动)
     * @param pitchAngle 俯仰角度 (0-90度)
     * @param yawDirection 偏航方向 (0:向左, 1:向右, 2:无运动)
     * @param yawAngle 偏航角度 (0-180度)
     */
    fun createGimbalSetAnglePacket(
        pitchDirection: Int = DIRECTION_STOP,
        pitchAngle: Int = 0,
        yawDirection: Int = DIRECTION_STOP,
        yawAngle: Int = 0,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            pitchDirection.toByte(),
            (pitchAngle and 0xFF).toByte(),
            ((pitchAngle shr 8) and 0xFF).toByte(),
            yawDirection.toByte(),
            (yawAngle and 0xFF).toByte(),
            ((yawAngle shr 8) and 0xFF).toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_GIMBAL_SET_ANGLE, payload, seq)
    }
    
    /**
     * 云台波轮速度设置指令 (0x000017)
     * @param pitchSpeed 俯仰波轮速度 (5-150, 对应1-30度/秒)
     * @param yawSpeed 方位波轮速度 (5-150, 对应1-30度/秒)
     */
    fun createGimbalSetSpeedPacket(
        pitchSpeed: Int = 50,
        yawSpeed: Int = 50,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            pitchSpeed.coerceIn(5, 150).toByte(),
            yawSpeed.coerceIn(5, 150).toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_GIMBAL_SET_SPEED, payload, seq)
    }
    
    /**
     * 云台稳像指令 (0x000033)
     * @param enable 是否开启稳像
     */
    fun createGimbalStabilizePacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00  // 预留
        )
        return buildPacket(MSG_GIMBAL_STABILIZE, payload, seq)
    }
    
    // ==================== 相机控制指令 ====================
    
    /**
     * 拍照、录像模式设置指令 (0x000300)
     * @param mode 0:拍照模式, 1:录像模式
     */
    fun createSetModePacket(mode: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            mode.toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_SET_MODE, payload, seq)
    }
    
    /**
     * 拍照参数设置指令 (0x000301)
     * @param photoMode 0:单拍, 1:连拍, 2:延时拍
     * @param timelapseInterval 延时拍间隔 (5/7/30/60)
     * @param burstCount 连拍张数 (3/5)
     */
    fun createSetPhotoParamsPacket(
        photoMode: Int = PHOTO_MODE_SINGLE,
        timelapseInterval: Int = 5,
        burstCount: Int = 3,
        seq: Int = 0
    ): ByteArray {
        val payload = byteArrayOf(
            photoMode.toByte(),
            timelapseInterval.toByte(),
            burstCount.toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_SET_PHOTO_PARAMS, payload, seq)
    }
    
    /**
     * 拍照指令 (0x000302)
     * @param cameraMode 0:默认, 1:红外, 2:可见光, 3:红外+可见光
     * @param action 0:单拍/开始, 1:停止 (仅连拍模式)
     * @param folderName 文件夹名 (可选)
     * @param fileName 文件名 (可选)
     */
    fun createTakePhotoPacket(
        cameraMode: Int = 0,
        action: Int = 0,
        folderName: String = "",
        fileName: String = "",
        seq: Int = 0
    ): ByteArray {
        val payload = ByteArray(54)
        payload[0] = cameraMode.toByte()
        payload[1] = action.toByte()
        
        // 文件夹名 (20字节)
        val folderBytes = folderName.toByteArray(Charsets.UTF_8)
        System.arraycopy(folderBytes, 0, payload, 2, minOf(folderBytes.size, 20))
        
        // 文件名 (32字节)
        val fileBytes = fileName.toByteArray(Charsets.UTF_8)
        System.arraycopy(fileBytes, 0, payload, 22, minOf(fileBytes.size, 32))
        
        return buildPacket(MSG_TAKE_PHOTO, payload, seq)
    }
    
    /**
     * 录像指令 (0x000303)
     * @param cameraMode 0:默认, 1:红外, 2:可见光, 3:红外+可见光, 4:视频流录屏
     * @param action 1:开始, 2:停止
     */
    fun createRecordPacket(
        cameraMode: Int = 0,
        action: Int = 1,
        folderName: String = "",
        fileName: String = "",
        seq: Int = 0
    ): ByteArray {
        val payload = ByteArray(54)
        payload[0] = cameraMode.toByte()
        payload[1] = action.toByte()
        
        val folderBytes = folderName.toByteArray(Charsets.UTF_8)
        System.arraycopy(folderBytes, 0, payload, 2, minOf(folderBytes.size, 20))
        
        val fileBytes = fileName.toByteArray(Charsets.UTF_8)
        System.arraycopy(fileBytes, 0, payload, 22, minOf(fileBytes.size, 32))
        
        return buildPacket(MSG_RECORD, payload, seq)
    }
    
    /**
     * 指定混合变倍指令 (0x000304)
     * @param zoomRate 混合变倍倍率 (1-160, 单位0.1倍)
     */
    fun createSetZoomPacket(zoomRate: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            0x00,  // 开始设置
            (zoomRate and 0xFF).toByte(),
            ((zoomRate shr 8) and 0xFF).toByte()
        )
        return buildPacket(MSG_SET_ZOOM, payload, seq)
    }
    
    /**
     * 连续混合变倍指令 (0x000306)
     * @param control 0:连续放大, 1:连续缩小, 2:停止, 3:放大, 4:缩小
     */
    fun createContinuousZoomPacket(control: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            control.toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_CONTINUOUS_ZOOM, payload, seq)
    }
    
    /**
     * 图像模式设置指令 (0x000318)
     * @param mode 0:红外, 5/6:可见光, 7:分屏
     */
    fun createSetImageModePacket(mode: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            mode.toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_SET_IMAGE_MODE, payload, seq)
    }
    
    // ==================== 红外相机设置指令 ====================
    
    /**
     * 红外伪彩设置指令 (0x000106)
     * @param palette 伪彩类型 (1-20)
     */
    fun createIRPalettePacket(palette: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            palette.coerceIn(1, 20).toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_IR_PALETTE, payload, seq)
    }
    
    /**
     * 红外测温开关指令 (0x000108)
     * @param enable 是否开启测温
     */
    fun createIRTempSwitchPacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x00 else 0x01,  // 0:开启, 1:关闭
            0x00  // 预留
        )
        return buildPacket(MSG_IR_TEMP_SWITCH, payload, seq)
    }
    
    /**
     * 红外点测温设置指令 (0x00010F)
     * @param x X坐标 (0-1920)
     * @param y Y坐标 (0-1088)
     */
    fun createIRPointTempPacket(x: Int, y: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            (x and 0xFF).toByte(),
            ((x shr 8) and 0xFF).toByte(),
            (y and 0xFF).toByte(),
            ((y shr 8) and 0xFF).toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_IR_POINT_TEMP, payload, seq)
    }
    
    /**
     * 红外电子放大设置指令 (0x000105)
     * @param zoom 电子放大倍数 (0x01:无放大, 0x02-0x08:2-8倍)
     */
    fun createIRZoomPacket(zoom: Int, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            zoom.coerceIn(1, 8).toByte(),
            0x00  // 预留
        )
        return buildPacket(MSG_IR_ZOOM, payload, seq)
    }
    
    /**
     * 红外超分开关指令 (0x000180)
     * @param enable 是否开启超分
     */
    fun createIRSuperResPacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00  // 预留
        )
        return buildPacket(MSG_IR_SUPER_RES, payload, seq)
    }
    
    // ==================== 激光测距指令 ====================
    
    /**
     * 激光测距设置指令 (0x000400)
     * @param enable 是否开启单次测距
     */
    fun createLaserRangePacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00  // 预留
        )
        return buildPacket(MSG_LASER_RANGE, payload, seq)
    }
    
    /**
     * 激光周期测距设置指令 (0x000406)
     * @param enable 是否开启1秒周期测距
     */
    fun createLaserPeriodicRangePacket(enable: Boolean, seq: Int = 0): ByteArray {
        val payload = byteArrayOf(
            if (enable) 0x01 else 0x00,
            0x00  // 预留
        )
        return buildPacket(MSG_LASER_PERIODIC_RANGE, payload, seq)
    }
}

/**
 * 禾启协议数据包
 */
data class HeQiPacket(
    val msgId: Int,
    val payload: ByteArray,
    val sequence: Int,
    val targetSystem: Byte,
    val targetComponent: Byte,
    val sourceSystem: Byte,
    val sourceComponent: Byte
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HeQiPacket
        return msgId == other.msgId && 
               sequence == other.sequence &&
               payload.contentEquals(other.payload)
    }
    
    override fun hashCode(): Int {
        var result = msgId
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + sequence
        return result
    }
}
