// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.
/**
 * 包声明，指定此源文件属于哪个包。
 */
package com.tencent.yolov8ncnn;

/**
 * 导入必要的Android权限、组件和工具类。
 * 这些导入语句允许使用Android SDK中提供的各种功能，如权限检查、用户界面组件、数据适配器等。
 */
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class MainActivity extends Activity implements SurfaceHolder.Callback
{
    // 请求相机权限的请求码
    public static final int REQUEST_CAMERA = 100;

    // Yolov8Ncnn 实例
    private Yolov8Ncnn yolov8ncnn = new Yolov8Ncnn();
    // 当前使用的摄像头方向（0为后置，1为前置）
    private int facing = 0;

    // 模型选择下拉框
    private Spinner spinnerModel;
    // CPU/GPU选择下拉框
    private Spinner spinnerCPUGPU;
    // 当前选择的模型索引
    private int current_model = 0;
    // 当前选择的CPU/GPU索引
    private int current_cpugpu = 0;

    // 摄像头预览视图
    private SurfaceView cameraView;

    /** 当Activity首次创建时调用 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 初始化摄像头预览视图
        cameraView = (SurfaceView) findViewById(R.id.cameraview);
        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        // 初始化切换摄像头按钮
        Button buttonSwitchCamera = (Button) findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 切换摄像头方向
                int new_facing = 1 - facing;
                yolov8ncnn.closeCamera();
                yolov8ncnn.openCamera(new_facing);
                facing = new_facing;
            }
        });

        // 初始化模型选择下拉框
        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // 模型选择改变时重新加载模型
                if (position != current_model) {
                    current_model = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // 未选择任何项时的处理
            }
        });

        // 初始化CPU/GPU选择下拉框
        spinnerCPUGPU = (Spinner) findViewById(R.id.spinnerCPUGPU);
        spinnerCPUGPU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // CPU/GPU选择改变时重新加载模型
                if (position != current_cpugpu) {
                    current_cpugpu = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // 未选择任何项时的处理
            }
        });

        // 加载初始模型
        reload();
    }

    // 重新加载模型
    private void reload()
    {
        boolean ret_init = yolov8ncnn.loadModel(getAssets(), current_model, current_cpugpu);
        if (!ret_init) {
            Log.e("MainActivity", "yolov8ncnn loadModel failed");
        }
    }

    // SurfaceHolder的尺寸或格式发生变化时调用
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        yolov8ncnn.setOutputWindow(holder.getSurface());
    }

    // SurfaceHolder创建时调用
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    // SurfaceHolder销毁时调用
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    // Activity恢复时调用
    @Override
    public void onResume()
    {
        super.onResume();

        // 检查相机权限，如果没有则请求权限
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        // 打开摄像头
        yolov8ncnn.openCamera(facing);
    }

    // Activity暂停时调用
    @Override
    public void onPause()
    {
        super.onPause();

        // 关闭摄像头
        yolov8ncnn.closeCamera();
    }
}

