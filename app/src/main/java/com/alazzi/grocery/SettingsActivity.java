package com.alazzi.grocery;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
public class SettingsActivity extends AppCompatActivity {
 @Override protected void onCreate(Bundle b){super.onCreate(b); LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(16,16,16,16);
 TextView h=new TextView(this);h.setText("الإعدادات");h.setTextSize(26);h.setGravity(Gravity.CENTER);r.addView(h,new LinearLayout.LayoutParams(-1,90));
 String[] s={"إعدادات الفواتير","إعدادات الطباعة","إعدادات الرسائل","إعدادات النسخ والاستعادة","المعلومات عن التطبيق","الهوية: بقالة العزي","حجم الخط واتجاه RTL"};
 for(String x:s){Button v=new Button(this);v.setText(x);v.setTextSize(16);r.addView(v,new LinearLayout.LayoutParams(-1,70));} setContentView(r);}
}
