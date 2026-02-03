package com.heqi.gimbal.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heqi.gimbal.R
import com.heqi.gimbal.model.AppSettings

/**
 * 设置页面
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var etCameraIp: EditText
    private lateinit var etControlIp: EditText
    private lateinit var etControlPort: EditText
    private lateinit var etRtspUrl: EditText
    private lateinit var etBufferTime: EditText
    private lateinit var switchReconnect: Switch
    private lateinit var etReconnectDelay: EditText
    private lateinit var etPitchSpeed: EditText
    private lateinit var etYawSpeed: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnReset: Button
    
    private var settings: AppSettings = AppSettings()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_settings)
        
        initViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initViews() {
        etCameraIp = findViewById(R.id.etCameraIp)
        etControlIp = findViewById(R.id.etControlIp)
        etControlPort = findViewById(R.id.etControlPort)
        etRtspUrl = findViewById(R.id.etRtspUrl)
        etBufferTime = findViewById(R.id.etBufferTime)
        switchReconnect = findViewById(R.id.switchReconnect)
        etReconnectDelay = findViewById(R.id.etReconnectDelay)
        etPitchSpeed = findViewById(R.id.etPitchSpeed)
        etYawSpeed = findViewById(R.id.etYawSpeed)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnReset = findViewById(R.id.btnReset)
    }
    
    private fun loadSettings() {
        settings = AppSettings.load(this)
        
        etCameraIp.setText(settings.cameraIp)
        etControlIp.setText(settings.controlIp)
        etControlPort.setText(settings.controlPort.toString())
        etRtspUrl.setText(settings.rtspUrl)
        etBufferTime.setText(settings.bufferTime.toString())
        switchReconnect.isChecked = settings.enableReconnect
        etReconnectDelay.setText(settings.reconnectDelay.toString())
        etPitchSpeed.setText(settings.defaultPitchSpeed.toString())
        etYawSpeed.setText(settings.defaultYawSpeed.toString())
    }
    
    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveSettings()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
        
        btnReset.setOnClickListener {
            resetToDefaults()
        }
    }
    
    private fun saveSettings() {
        try {
            val newSettings = AppSettings(
                cameraIp = etCameraIp.text.toString().trim(),
                controlIp = etControlIp.text.toString().trim(),
                controlPort = etControlPort.text.toString().toInt(),
                rtspUrl = etRtspUrl.text.toString().trim(),
                bufferTime = etBufferTime.text.toString().toInt(),
                enableReconnect = switchReconnect.isChecked,
                reconnectDelay = etReconnectDelay.text.toString().toLong(),
                defaultPitchSpeed = etPitchSpeed.text.toString().toInt(),
                defaultYawSpeed = etYawSpeed.text.toString().toInt()
            )
            
            // 验证输入
            if (newSettings.cameraIp.isEmpty() || newSettings.controlIp.isEmpty()) {
                Toast.makeText(this, "IP地址不能为空", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (newSettings.controlPort <= 0 || newSettings.controlPort > 65535) {
                Toast.makeText(this, "端口号必须在1-65535之间", Toast.LENGTH_SHORT).show()
                return
            }
            
            AppSettings.save(this, newSettings)
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun resetToDefaults() {
        settings = AppSettings()
        
        etCameraIp.setText(settings.cameraIp)
        etControlIp.setText(settings.controlIp)
        etControlPort.setText(settings.controlPort.toString())
        etRtspUrl.setText(settings.rtspUrl)
        etBufferTime.setText(settings.bufferTime.toString())
        switchReconnect.isChecked = settings.enableReconnect
        etReconnectDelay.setText(settings.reconnectDelay.toString())
        etPitchSpeed.setText(settings.defaultPitchSpeed.toString())
        etYawSpeed.setText(settings.defaultYawSpeed.toString())
        
        Toast.makeText(this, "已恢复默认设置", Toast.LENGTH_SHORT).show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
