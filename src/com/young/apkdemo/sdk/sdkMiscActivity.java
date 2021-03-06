/*
 * Copyright (C) 2015 Mu Weiyang <young.mu@aliyun.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranties of
 * MERCHANTABILITY, SATISFACTORY QUALITY, or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.young.apkdemo.sdk;

import com.young.apkdemo.R;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;
import android.os.Environment;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.telephony.TelephonyManager;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import dalvik.system.DexClassLoader;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.widget.Toast;

public class sdkMiscActivity extends Activity implements OnClickListener {
    private static final String TAG = "apkdemo";
    private TextView miscTxt;
    private Button abiBtn;
    private Button envDirBtn;
    private Button appDirBtn;
    private Button classLoaderBtn;
    private Button systemServiceBtn;
    private Button gpsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk_misc);
        Log.i(TAG, "enter SDK Misc Activity");
        // get textview
        miscTxt = (TextView)findViewById(R.id.sdk_misc_text);
        // get buttons and set listeners
        abiBtn = (Button)findViewById(R.id.abi_button);
        envDirBtn = (Button)findViewById(R.id.env_directory_button);
        appDirBtn = (Button)findViewById(R.id.app_directory_button);
        classLoaderBtn = (Button)findViewById(R.id.classloader_button);
        systemServiceBtn = (Button)findViewById(R.id.systemservice_button);
        gpsBtn = (Button)findViewById(R.id.gps_button);
        abiBtn.setOnClickListener(this);
        envDirBtn.setOnClickListener(this);
        appDirBtn.setOnClickListener(this);
        classLoaderBtn.setOnClickListener(this);
        systemServiceBtn.setOnClickListener(this);
        gpsBtn.setOnClickListener(this);
    }

    public String getSystemABI() {
        // get ABI from Java level
        Log.i(TAG, "CPU_ABI(java): " + Build.CPU_ABI);
        String ret = "CPU_ABI(java): [build] " + Build.CPU_ABI + ", [support]";
        String[] abis = Build.SUPPORTED_ABIS;
        for (int i = 0; i < abis.length; i ++) {
            Log.i(TAG, "abis[" + i + "](java): " + abis[i]);
            ret = ret + abis[i];
        }
        ret = ret + "\n";
        // get ABI from Native level
        try {
            Process process = Runtime.getRuntime().exec("getprop ro.product.cpu.abi");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String na = "CPU_ABI(standalone): " + bufferedReader.readLine();
            Log.i(TAG, na);
            ret = ret + na;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    public String getEnvironmentDirectories() {
        String[] dirs = {
            "getRootDirectory(): " + Environment.getRootDirectory().toString(),
            "getDataDirectory(): " + Environment.getDataDirectory().toString(),
            "getDownloadCacheDirectory(): " + Environment.getDownloadCacheDirectory().toString(),
            "getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory().toString(),
            "getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES): " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(),
            "isExternalStorageEmulated(): " + Environment.isExternalStorageEmulated(),
            "isExternalStorageRemovable(): " + Environment.isExternalStorageRemovable()
        };
        String ret = "";
        for (String d : dirs) {
            ret = ret + d + "\n";
            Log.i(TAG, d);
        }
        return ret;
    }

    public String getApplicationDirectories(Context context) {
        String[] dirs = {
            "getFilesDir(): " + context.getFilesDir().toString(),
            "getCacheDir(): " + context.getCacheDir().toString(),
            "getExternalFilesDir(null): " + context.getExternalFilesDir(null).toString(),
            "getExternalCacheDir(): " + context.getExternalCacheDir().toString()
        };
        String ret = "";
        for (String d : dirs) {
            ret = ret + d + "\n";
            Log.i(TAG, d);
        }
        return ret;
    }

    public void useDexClassLoader() {
        Intent intent = new Intent("young.android.plugin");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL); // MATCH_ALL is available after API23
        if (resolveInfos.size() == 0) {
            Log.e(TAG, "resolveInfos length is 0, CHECK if the plugin is installed");
            return;
        } else {
            for (ResolveInfo resolveInfo : resolveInfos) {
                Log.i(TAG, "resolveInfo: " + resolveInfo);
            }
        }
        ActivityInfo actInfo = resolveInfos.get(0).activityInfo;
        String packageName = actInfo.packageName;
        Log.i(TAG, "packageName: " + packageName);

        String apkPath = actInfo.applicationInfo.sourceDir;
        String dexOutputPath = getApplicationInfo().dataDir;
        String libPath = actInfo.applicationInfo.nativeLibraryDir;
        Log.i(TAG, "apkPath: " + apkPath);
        Log.i(TAG, "dexOutputPath: " + dexOutputPath);
        Log.i(TAG, "libPath: " + libPath);
        Log.i(TAG, "classLoader: " + this.getClass().getClassLoader().toString());
        DexClassLoader classLoader = new DexClassLoader(apkPath, dexOutputPath, libPath, this.getClass().getClassLoader());

        try {
            Class clazz = classLoader.loadClass(packageName + ".Plugin");
            Object obj = clazz.newInstance();
            Class[] params = new Class[2];
            params[0] = Integer.TYPE;
            params[1] = Integer.TYPE;
            Method mtd = clazz.getMethod("function", params);
            Integer result = (Integer)mtd.invoke(obj, 100, 200);
            Log.i(TAG, "result is: " + result.toString());
        } catch (ClassNotFoundException e) { // loadClass
            e.printStackTrace();
        } catch (IllegalAccessException e) { // newInstance/invoke
            e.printStackTrace();
        } catch (InstantiationException e) { // newInstance/invoke
            e.printStackTrace();
        } catch (NoSuchMethodException e) { // getMethod
            e.printStackTrace();
        } catch (NullPointerException e) { // inovke
            e.printStackTrace();
        } catch (InvocationTargetException e) { // invoke
            e.printStackTrace();
        }
    }

    public String getSystemServiceInfo() {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String str = null;
        try {
            str = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, "IMEI: " + str);
            return "IMEI: " + str;
        }
    }

    public String getGpsStatus() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String ret = "all location providers: " + lm.getAllProviders().toString();
        Log.i(TAG, ret);
        if (lm.getAllProviders().contains(android.location.LocationManager.GPS_PROVIDER)) {
            String b = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ? "YES" : "NO";
            Log.i(TAG, "device has GPS, enabled? - " + b);
            while (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "please enable GPS, and grant app's access to it manually!");
                Toast.makeText(this, "device has GPS, please enable and grant the location access", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                startActivityForResult(intent, 0);
            }
            try {
                b = (lm.getProvider(android.location.LocationManager.GPS_PROVIDER) != null) ? "YES" : "NO";
                b = "app got the GPS controller? - " + b;
            } catch (Exception e) {
                b = "Exception when get GPS provider!";
                e.printStackTrace();
                Toast.makeText(this, "device has GPS, please enable and grant the location access", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                startActivityForResult(intent, 0);
            } finally {
                Log.i(TAG, b);
                ret = ret + "\n" + b;
            }
        } else {
            String t = "device does NOT have GPS!";
            Log.i(TAG, t);
            ret = ret + "\n" + t;
        }
        return ret;
    }

    @Override
    public void onClick(View view) {
        String retString = "nothing";
        switch (view.getId()) {
        case R.id.abi_button:
            retString = getSystemABI();
            break;
        case R.id.env_directory_button:
            retString = getEnvironmentDirectories();
            break;
        case R.id.app_directory_button:
            retString = getApplicationDirectories(this);
            break;
        case R.id.classloader_button:
            useDexClassLoader();
            retString = "DexClassLoader is complex, check log";
            break;
        case R.id.systemservice_button:
            retString = getSystemServiceInfo();
            break;
        case R.id.gps_button:
            retString = getGpsStatus();
            break;
        default:
            break;
        }
        Log.i(TAG, retString);
        miscTxt.setText(retString);
    }
}
