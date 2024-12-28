package cool.cmg.swi_fispeed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.net.wifi.WifiInfo;

import android.widget.TextView;


import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"com.android.settings".equals(lpparam.packageName)) {
            return;
        }
        findAndHookMethod(
                findClass("com.samsung.android.settings.wifi.ConnectedListAdapter", lpparam.classLoader),
                "onBindViewHolder",
                findClass("androidx.recyclerview.widget.RecyclerView.ViewHolder", lpparam.classLoader),
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        TextView mSummary = (TextView) XposedHelpers.getObjectField(param.args[0], "mSummary");
                        String result = mSummary.getText().toString() + " " + getSpeed(param);
                        mSummary.setText(result);
                    }

                    private String getSpeed(MethodHookParam param) {
                        List<?> wifiEntries = (List<?>) XposedHelpers.getObjectField(param.thisObject, "mWifiEntries");
                        Object wifiEntry = wifiEntries.get((int) param.args[1]);

                        WifiInfo wifiInfo = (WifiInfo) XposedHelpers.getObjectField(wifiEntry, "mWifiInfo");
                        int tx = wifiInfo.getTxLinkSpeedMbps();
                        int rx = wifiInfo.getRxLinkSpeedMbps();

                        return tx + "," + rx;
                    }
                });
    }
}