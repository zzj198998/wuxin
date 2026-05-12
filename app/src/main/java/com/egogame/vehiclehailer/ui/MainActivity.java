package com.egogame.vehiclehailer.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.egogame.vehiclehailer.R;
import com.egogame.vehiclehailer.VehicleHailerApp;
import com.egogame.vehiclehailer.engine.LogcatMonitor;
import com.egogame.vehiclehailer.engine.LogcatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    // 记录当前选中的Fragment
    private Fragment currentFragment;
    private VoiceListFragment voiceListFragment;
    private MonitorFragment monitorFragment;
    private SettingsFragment settingsFragment;

    // Logcat监听服务
    private LogcatMonitor logcatMonitor;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        bottomNavigation = findViewById(R.id.bottom_navigation);

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

        // 底部导航切换
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment target = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_voice) {
                target = voiceListFragment;
            } else if (itemId == R.id.nav_monitor) {
                target = monitorFragment;
            } else if (itemId == R.id.nav_settings) {
                target = settingsFragment;
            }
            if (target != null && target != currentFragment) {
                fragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(target)
                        .commit();
                currentFragment = target;
            }
            return true;
        });

        // 启动Logcat监听
        startLogcatMonitoring();
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
