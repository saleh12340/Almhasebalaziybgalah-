package com.alazzi.grocery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SettingsActivity extends androidx.appcompat.app.AppCompatActivity {
    private LinearLayout root;private EditText name,phone,width,font,address;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Ui.BG);root.setPadding(10,8,10,14);root.addView(Ui.titleBar(this,"الإعدادات",v->finish(),v->{}),new LinearLayout.LayoutParams(-1,64));
        ScrollView s=new ScrollView(this);LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);name=Ui.edit(this,"اسم النشاط",false);phone=Ui.edit(this,"رقم الهاتف",true);width=Ui.edit(this,"عرض الطباعة 58 أو 80",true);font=Ui.edit(this,"حجم خط الإيصال",true);address=Ui.edit(this,"عنوان الطابعة Bluetooth",false);for(EditText e:new EditText[]{name,phone,width,font,address})b.addView(e,new LinearLayout.LayoutParams(-1,54));name.setText(get("business_name","بقالة العزي للمواد الغذائية"));phone.setText(get("business_phone",""));width.setText(get("print_width","58"));font.setText(get("receipt_font","13"));address.setText(get("printer_address",""));
        Switch auto=new Switch(this);auto.setText("النسخ الاحتياطي التلقائي");auto.setTextSize(16);auto.setChecked("1".equals(get("auto_backup","0")));b.addView(auto,new LinearLayout.LayoutParams(-1,54));
        Switch rtl=new Switch(this);rtl.setText("واجهة RTL العربية");rtl.setChecked(true);b.addView(rtl,new LinearLayout.LayoutParams(-1,54));
        b.addView(Ui.button(this,"حفظ إعدادات الهوية والطباعة",v->{put("business_name",name.getText().toString());put("business_phone",phone.getText().toString());put("print_width",width.getText().toString());put("receipt_font",font.getText().toString());put("printer_address",address.getText().toString());put("auto_backup",auto.isChecked()?"1":"0");Toast.makeText(this,"تم حفظ الإعدادات ✓",Toast.LENGTH_SHORT).show();}),new LinearLayout.LayoutParams(-1,56));
        b.addView(Ui.button(this,"فتح النسخ والاستعادة",v->startActivity(new android.content.Intent(this,BackupActivity.class))),new LinearLayout.LayoutParams(-1,56));b.addView(Ui.button(this,"اختيار طابعة Bluetooth",v->SharePrint.printTest(this)),new LinearLayout.LayoutParams(-1,56));s.addView(b);root.addView(s,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);}
    private String get(String k,String d){android.database.Cursor c=AppDatabase.get(this).getReadableDatabase().rawQuery("SELECT value FROM settings WHERE key=?",new String[]{k});try{return c.moveToFirst()?c.getString(0):d;}finally{c.close();}}
    private void put(String k,String v){AppDatabase.get(this).getWritableDatabase().execSQL("INSERT OR REPLACE INTO settings(key,value)VALUES(?,?)",new Object[]{k,v});}
}
