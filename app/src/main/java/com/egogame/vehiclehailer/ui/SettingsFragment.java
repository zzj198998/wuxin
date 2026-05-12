package com.egogame.vehiclehailer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.egogame.vehiclehailer.R;
import com.egogame.vehiclehailer.VehicleHailerApp;
import com.egogame.vehiclehailer.engine.ConfigLoader;
import com.egogame.vehiclehailer.engine.VoicePlayer;
import com.egogame.vehiclehailer.model.CarModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private Spinner carModelSpinner;
    private MaterialSwitch channelSwitch;
    private MaterialSwitch ttsEnabledSwitch;
    private TextInputEditText ttsUrlInput;
    private MaterialSwitch autoPlaySwitch;

    private ConfigLoader configLoader;
    private VoicePlayer voicePlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            configLoader = VehicleHailerApp.getInstance().getConfigLoader();
            voicePlayer = VehicleHailerApp.getInstance().getVoicePlayer();

            carModelSpinner = view.findViewById(R.id.car_model_spinner);
            channelSwitch = view.findViewById(R.id.channel_switch);
            ttsEnabledSwitch = view.findViewById(R.id.tts_enabled_switch);
            ttsUrlInput = view.findViewById(R.id.tts_url_input);
            autoPlaySwitch = view.findViewById(R.id.auto_play_switch);

            // 车型选择（空安全）
            List<CarModel> carModels = configLoader != null ? configLoader.getCarModels() : new ArrayList<>();
            String[] modelNames = new String[carModels.size()];
            for (int i = 0; i < carModels.size(); i++) {
                modelNames[i] = carModels.get(i).getDisplayName();
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        getContext(), android.R.layout.simple_spinner_item, modelNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            carModelSpinner.setAdapter(spinnerAdapter);

            // 默认选中启源Q05 (id=5)
            int defaultModelIndex = 0;
            for (int i = 0; i < carModels.size(); i++) {
                if (carModels.get(i).getId() == 5) { // 启源Q05
                    defaultModelIndex = i;
                    break;
                }
            }
            carModelSpinner.setSelection(defaultModelIndex);

            // 车型切换
            carModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (configLoader != null) {
                            CarModel selected = configLoader.getCarModels().get(position);
                            configLoader.setCurrentCarModelId(selected.getId());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SettingsFragment", "车型选择失败", e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // 声道切换（车内/车外）
            if (channelSwitch != null) {
                channelSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    try {
                        if (voicePlayer != null) {
                            if (isChecked) {
                                voicePlayer.setChannel(true);
                                channelSwitch.setText(R.string.setting_channel_inside);
                            } else {
                                voicePlayer.setChannel(false);
                                channelSwitch.setText(R.string.setting_channel_outside);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SettingsFragment", "声道切换失败", e);
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("SettingsFragment", "设置页面初始化失败", e);
        }
    }

}
