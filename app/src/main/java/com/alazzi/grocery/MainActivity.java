package com.alazzi.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    static final String[] MODULES = {
        "الفواتير","الحسابات والعملاء","الموردون","المنتجات","المخزون والمخازن",
        "العملات","العروض والطلبات","الحوالات","التنبيهات والرسائل","التقارير",
        "النسخ الاحتياطي والاستعادة","الطباعة","الإعدادات"
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        AppDatabase.get(this).getWritableDatabase();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,18,18,18);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("بقالة العزي\nللمواد الغذائية");
        title.setTextSize(26); title.setGravity(Gravity.CENTER); title.setTextColor(0xFFFFFFFF);
        title.setBackgroundColor(0xFF111827);
        root.addView(title,new LinearLayout.LayoutParams(-1,130));
        GridLayout grid = new GridLayout(this); grid.setColumnCount(2);
        for(String m: MODULES) {
            Button x = new Button(this); x.setText(m); x.setTextSize(17);
            x.setOnClickListener(v -> {
                if (m.equals("الإعدادات")) startActivity(new Intent(this, SettingsActivity.class));
                else { Intent i=new Intent(this,ModuleActivity.class); i.putExtra("module",m); startActivity(i); }
            });
            GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.height=150; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); p.setMargins(6,6,6,6); grid.addView(x,p);
        }
        ScrollView scroll=new ScrollView(this); scroll.addView(grid);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
}
