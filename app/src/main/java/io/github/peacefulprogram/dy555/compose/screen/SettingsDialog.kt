package io.github.peacefulprogram.dy555.compose.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.peacefulprogram.dy555.Constants
import io.github.peacefulprogram.dy555.util.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    context: Context
) {
    var apiServerUrl: String by remember {
        mutableStateOf(PreferenceManager.getM3u8ApiServer(context))
    }
    
    // 添加网站地址状态
    var baseUrl: String by remember {
        mutableStateOf(PreferenceManager.getBaseUrl(context) ?: Constants.BASE_URL)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column {
                // M3U8解析服务器地址
                Text("M3U8解析服务器地址:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiServerUrl,
                    onValueChange = { apiServerUrl = it },
                    label = { Text("解析地址") },
                    placeholder = { Text("http://hualsylf.eicp.net ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 网站地址输入框
                Text("网站地址:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("网站地址") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 保存 M3U8 解析服务器地址
                    PreferenceManager.saveM3u8ApiServer(context, apiServerUrl)
                    Constants.M3U8_EXTRACT_API_SERVER = apiServerUrl
                    
                    // 保存网站地址
                    PreferenceManager.saveBaseUrl(context, baseUrl)
                    Constants.BASE_URL = baseUrl
                    
                    android.widget.Toast.makeText(context, "保存成功", android.widget.Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}