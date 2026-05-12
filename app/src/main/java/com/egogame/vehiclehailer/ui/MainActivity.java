package com.egogame.vehiclehailer.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.egogame.vehiclehailer.R;
import com.egogame.vehiclehailer.VehicleHailerApp;
import com.egogame.vehiclehailer.engine.LogcatMonitor;
import com.egogame.vehiclehailer.engine.LogcatService;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    // 记录当前选中的Fragment
    private Fragment currentFragment;
    private VoiceListFragment voiceListFragment;
    private MonitorFragment monitorFragment;
    private SettingsFragment settingsFragment;

    // 左侧导航按钮
    private ImageButton navVoice, navMonitor, navSettings;
    private TextView navVoiceLabel, navMonitorLabel, navSettingsLabel;

    // Logcat监听服务
    private LogcatMonitor logcatMonitor;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        // 初始化左侧导航按钮
        navVoice = findViewById(R.id.nav_voice);
        navMonitor = findViewById(R.id.nav_monitor);
        navSettings = findViewById(R.id.nav_settings);
        navVoiceLabel = findViewById(R.id.nav_voice_label);
        navMonitorLabel = findViewById(R.id.nav_monitor_label);
        navSettingsLabel = findViewById(R.id.nav_settings_label);

        // 初始化Fragment
        voiceListFragment = new VoiceListFragment();
        monitorFragment = new MonitorFragment();
        settingsFragment = new SettingsFragment();

        // 默认显示语音列表
        currentFragment = voiceListFragment;
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, voiceListFragment, "voice")
                .add(R.id.fragment_container, monitorFragment, "monitor")
                .add(R.id.fragment_container, settingsFragment, "settings")
                .hide(monitorFragment)
                .hide(settingsFragment)
                .commit();

        // 更新导航按钮高亮状态
        updateNavHighlight(true, false, false);

        // 左侧导航按钮点击事件
        navVoice.setOnClickListener(v -> switchFragment(voiceListFragment));
        navMonitor.setOnClickListener(v -> {
            switchFragment(monitorFragment);
            // 切换到监控时刷新数据
            if (monitorFragment != null) {
                monitorFragment.onVehicleStateChanged();
            }
        });
        navSettings.setOnClickListener(v -> switchFragment(settingsFragment));

        // 启动Logcat监听
        startLogcatMonitoring();
    }

    private void switchFragment(Fragment target) {
        if (target == null || target == currentFragment) return;
        fragmentManager.beginTransaction()
                .hide(currentFragment)
                .show(target)
                .commit();
        currentFragment = target;

        // 更新导航高亮
        updateNavHighlight(
                target == voiceListFragment,
                target == monitorFragment,
                target == settingsFragment
        );
    }

    private void updateNavHighlight(boolean voice, boolean monitor, boolean settings) {
        navVoice.setBackgroundResource(voice ?
                R.drawable.nav_button_bg_active : R.drawable.nav_button_bg);
        navMonitor.setBackgroundResource(monitor ?
                R.drawable.nav_button_bg_active : R.drawable.nav_button_bg);
        navSettings.setBackgroundResource(settings ?
                R.drawable.nav_button_bg_active : R.drawable.nav_button_bg);

        if (navVoiceLabel != null) navVoiceLabel.setTextColor(
                getColor(voice ? R.color.white : R.color.gray_400));
        if (navMonitorLabel != null) navMonitorLabel.setTextColor(
                getColor(monitor ? R.color.white : R.color.gray_400));
        if (navSettingsLabel != null) navSettingsLabel.setTextColor(
                getColor(settings ? R.color.white : R.color.gray_400));
    }

    private void startLogcatMonitoring() {
        VehicleHailerApp app = VehicleHailerApp.getInstance();
        logcatMonitor = new LogcatMonitor(app.getVehicleStateManager(), matchedLine -> {
            runOnUiThread(() -> {
                if (!isMonitoring) {
                    isMonitoring = true;
                    Toast.makeText(this, "车辆信号已连接", Toast.LENGTH_SHORT).show();
                }
                // 通知MonitorFragment更新UI
                if (monitorFragment != null && monitorFragment.isVisible()) {
                    monitorFragment.onVehicleStateChanged();
                }
            });
        });
        logcatMonitor.start();

        // 同时启动前台Service保持监听
        LogcatService.start(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logcatMonitor != null) {
            logcatMonitor.stop();
        }
        // 停止Service
        LogcatService.stop(this);
    }
}
