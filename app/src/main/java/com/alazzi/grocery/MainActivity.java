package com.alazzi.grocery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);AppDatabase.get(this).getWritableDatabase();render();}
    private void render(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Ui.BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView title=Ui.text(this,"بقالة العزي للمواد الغذائية",22,true,Ui.WHITE);title.setGravity(Gravity.CENTER);title.setBackgroundColor(Ui.DARK);root.addView(title,new LinearLayout.LayoutParams(-1,Ui.dp(title,72)));
        TextView summary=Ui.text(this,summaryText(),15,true,Ui.TEXT);summary.setGravity(Gravity.CENTER);summary.setBackground(Ui.round(Ui.WHITE,16,0xffdfe4ec));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,Ui.dp(summary,64));sp.setMargins(Ui.dp(summary,10),Ui.dp(summary,10),Ui.dp(summary,10),Ui.dp(summary,10));root.addView(summary,sp);
        ScrollView sc=new ScrollView(this);GridLayout grid=new GridLayout(this);grid.setColumnCount(2);grid.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        String[][] modules={{"الفواتير","بيع وشراء وحفظ وطباعة"},{"الحسابات","العملاء والموردون والكشوف"},{"الأصناف","الأصناف والأسعار والباركود"},{"المخزون","الجرد والمخازن والتحويل"},{"الصناديق","النقدية والشبكة والسندات"},{"الحوالات","إرسال واستلام ومشاركة"},{"التقارير","المبيعات والمشتريات والأرباح"},{"الملاحظات","حفظ ومشاركة وطباعة"},{"التنبيهات","مواعيد واستحقاقات"},{"العروض والطلبات","عروض أسعار وطلبيات"},{"العملات","العملات وأسعار الصرف"},{"النسخ الاحتياطي","محلي وGoogle Drive"},{"الإعدادات","الهوية والطباعة والرسائل"},{"أخرى","الحاسبة والأرشيف والأصول"}};
        for(String[] m:modules){
            LinearLayout c=Ui.card(this);c.setGravity(Gravity.CENTER);
            TextView a=Ui.text(this,m[0],18,true,Ui.BLUE);a.setGravity(Gravity.CENTER);
            TextView s=Ui.text(this,m[1],12,false,Ui.MUTED);s.setGravity(Gravity.CENTER);
            c.addView(a,new LinearLayout.LayoutParams(-1,Ui.dp(c,35)));c.addView(s,new LinearLayout.LayoutParams(-1,Ui.dp(c,45)));
            c.setOnClickListener(v->openModule(m[0]));
            GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=Ui.dp(c,124);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(Ui.dp(c,5),Ui.dp(c,5),Ui.dp(c,5),Ui.dp(c,5));grid.addView(c,p);
        }
        sc.addView(grid);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        Button add=Ui.button(this,"＋ إضافة سريعة",v->quickAdd());root.addView(add,new LinearLayout.LayoutParams(-1,Ui.dp(add,54)));
        setContentView(root);
    }
    private String summaryText(){
        android.database.Cursor c=AppDatabase.get(this).getReadableDatabase().rawQuery("SELECT IFNULL(SUM(CASE WHEN type='بيع' THEN total ELSE 0 END),0),IFNULL(SUM(CASE WHEN type='شراء' THEN total ELSE 0 END),0) FROM invoices WHERE date(created_at)=date('now','localtime')",null);
        try{c.moveToFirst();return "اليوم  •  مبيعات: "+Ui.number(c.getDouble(0))+"  •  مشتريات: "+Ui.number(c.getDouble(1));}finally{c.close();}
    }
    private void openModule(String m){
        Intent i;
        if(m.equals("الفواتير"))i=new Intent(this,InvoiceActivity.class);
        else if(m.equals("الحسابات"))i=new Intent(this,AccountsActivity.class);
        else if(m.equals("الأصناف")||m.equals("المخزون"))i=new Intent(this,ProductsActivity.class);
        else if(m.equals("الحوالات"))i=new Intent(this,TransferActivity.class);
        else if(m.equals("التقارير"))i=new Intent(this,ReportsActivity.class);
        else if(m.equals("الملاحظات"))i=new Intent(this,NotesActivity.class);
        else if(m.equals("النسخ الاحتياطي"))i=new Intent(this,BackupActivity.class);
        else if(m.equals("الإعدادات"))i=new Intent(this,SettingsActivity.class);
        else i=new Intent(this,ScreenActivity.class).putExtra("screen",m.equals("أخرى")?"__all__":m);
        startActivity(i);
    }
    private void quickAdd(){
        final String[] a={"فاتورة بيع","فاتورة شراء","حساب عميل","حساب مورد","صنف","عملية حساب","حوالة","ملاحظة"};
        new AlertDialog.Builder(this).setTitle("إضافة سريعة").setItems(a,(d,w)->{
            if(w<2)startActivity(new Intent(this,InvoiceActivity.class).putExtra("type",w==0?"بيع":"شراء"));
            else if(w==2||w==3)startActivity(new Intent(this,AccountsActivity.class).putExtra("addKind",w==2?"customer":"supplier"));
            else if(w==4)startActivity(new Intent(this,ProductsActivity.class).putExtra("add",true));
            else if(w==5)startActivity(new Intent(this,AccountsActivity.class).putExtra("operation",true));
            else if(w==6)startActivity(new Intent(this,TransferActivity.class).putExtra("add",true));
            else startActivity(new Intent(this,NotesActivity.class).putExtra("add",true));
        }).show();
    }
}
