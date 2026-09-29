package com.alazzi.grocery;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ModuleActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        String m=getIntent().getStringExtra("module");
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(16,16,16,16);
        TextView h=new TextView(this); h.setText(m); h.setTextSize(24); h.setGravity(Gravity.CENTER); root.addView(h,new LinearLayout.LayoutParams(-1,90));
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.VERTICAL);
        String[] a=actionsFor(m);
        for(String s:a){ Button x=new Button(this); x.setText(s); x.setTextSize(16); actions.addView(x,new LinearLayout.LayoutParams(-1,64)); }
        ScrollView sv=new ScrollView(this); sv.addView(actions); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private String[] actionsFor(String m){
        if(m.contains("فواتير")) return new String[]{"فاتورة بيع","فاتورة شراء","مرتجع بيع","مرتجع شراء","قائمة الفواتير","تفاصيل الفاتورة","خيارات الفاتورة"};
        if(m.contains("حسابات")) return new String[]{"العملاء","حساب العميل","عملية عميل","كشف الحساب","البحث والتصفية"};
        if(m.contains("المورد")) return new String[]{"الموردون","حساب المورد","عملية مورد","كشف الحساب"};
        if(m.contains("منتجات")) return new String[]{"إضافة صنف","الأصناف","تعديل الصنف","الباركود","الأسعار"};
        if(m.contains("المخزون")) return new String[]{"المخازن","حركة المخزون","تحويل بين المخازن","الجرد"};
        if(m.contains("العملات")) return new String[]{"العملات","إضافة عملة","أسعار الصرف"};
        if(m.contains("العروض")) return new String[]{"العروض","إضافة عرض","الطلبات","تفاصيل الطلب"};
        if(m.contains("الحوالات")) return new String[]{"حوالة جديدة","الحوالات","تفاصيل الحوالة","مشاركة وواتساب"};
        if(m.contains("التنبيهات")) return new String[]{"التنبيهات","الرسائل","إضافة تنبيه"};
        if(m.contains("التقارير")) return new String[]{"التقرير النهائي","تقارير المبيعات","تقارير المنتجات","تقارير المخزون","تقارير الضرائب","تقارير المتجر","تقارير الحسابات"};
        if(m.contains("النسخ")) return new String[]{"نسخ احتياطي","استعادة","Google Drive","الأرشيف"};
        if(m.contains("الطباعة")) return new String[]{"طابعة Bluetooth","إعداد الطباعة","طباعة 58mm","معاينة"};
        return new String[]{"فتح","إضافة","بحث","تصفية","مشاركة","طباعة"};
    }
}
