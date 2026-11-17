package kr.napjaki.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("napjaki", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
        }
        layout.addView(TextView(this).apply {
            text = "납작이"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "홈 화면에서만 움직이는 라이브 배경화면"
            textSize = 15f
            setPadding(0, 12, 0, 22)
        })
        val label = TextView(this).apply { textSize = 17f }
        val seek = SeekBar(this).apply {
            max = 100
            progress = prefs.getInt("size", 65) - 50
        }
        fun refresh() { label.text = "캐릭터 크기: ${seek.progress + 50}%" }
        refresh()
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                prefs.edit().putInt("size", value + 50).apply()
                refresh()
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        layout.addView(label)
        layout.addView(seek)
        layout.addView(TextView(this).apply {
            text = "납작이를 화면에서 손가락으로 끌어 옮길 수 있어요. 혼잣말은 15~45초 사이 무작위로 나타나고 2초 뒤 사라져요."
            textSize = 14f
            setPadding(0, 16, 0, 24)
        })
        layout.addView(Button(this).apply {
            text = "배경화면 적용하기"
            setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, NapjakiWallpaperService::class.java))
                }
                startActivity(intent)
            }
        })
        setContentView(layout)
    }
}
