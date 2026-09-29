package com.alazzi.grocery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class ScreenActivity extends androidx.appcompat.app.AppCompatActivity {
    private Repository repo; private LinearLayout list; private String screen;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new Repository(this);screen=getIntent().getStringExtra("screen");if(screen==null)screen="__all__";render();}
    private void render(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Ui.BG);
        root.addView(Ui.titleBar(this,screen.equals("__all__")?"وظائف بقالة العزي":screen,v->finish(),v->{}),new LinearLayout.LayoutParams(-1,64));
        ScrollView sc=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(10,10,10,18);sc.addView(list);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        if(screen.equals("__all__"))allScreens();else dispatch();
    }
    private void allScreens(){
        for(String n:ScreenCatalog.ALL.keySet()){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,n,17,true,Ui.TEXT));c.setOnClickListener(v->startActivityForScreen(n));list.addView(c,new LinearLayout.LayoutParams(-1,58));}
    }
    private void startActivityForScreen(String n){
        if(n.equals("الفواتير")||n.contains("فاتورة"))startActivity(new android.content.Intent(this,InvoiceActivity.class).putExtra("type",n.contains("شراء")?"شراء":"بيع"));
        else if(n.equals("الحسابات")||n.equals("العملاء")||n.equals("الموردون")||n.contains("حساب"))startActivity(new android.content.Intent(this,AccountsActivity.class));
        else if(n.equals("الأصناف")||n.equals("إضافة صنف")||n.contains("باركود")||n.contains("أسعار الأصناف"))startActivity(new android.content.Intent(this,ProductsActivity.class).putExtra("add",n.equals("إضافة صنف"))); else if(n.equals("المخزون")||n.equals("المخازن")||n.equals("تحويل بين المخازن")||n.equals("جرد المخزون"))startActivity(new android.content.Intent(this,WarehouseActivity.class)); else if(n.equals("الرسائل")||n.equals("إرسال واتساب"))startActivity(new android.content.Intent(this,MessagesActivity.class));
        else if(n.equals("الحوالات")||n.equals("إضافة حوالة")||n.equals("تفاصيل الحوالة")||n.equals("إرسال واتساب"))startActivity(new android.content.Intent(this,TransferActivity.class).putExtra("add",n.equals("إضافة حوالة")));
        else if(n.equals("الملاحظات")||n.equals("إضافة ملاحظة"))startActivity(new android.content.Intent(this,NotesActivity.class).putExtra("add",n.equals("إضافة ملاحظة")));
        else if(n.equals("التقارير")||n.startsWith("تقرير")||n.startsWith("تقارير")||n.contains("الأرباح")||n.contains("الديون"))startActivity(new android.content.Intent(this,ReportsActivity.class));
        else if(n.equals("النسخ الاحتياطي")||n.equals("الاستعادة")||n.equals("Google Drive"))startActivity(new android.content.Intent(this,BackupActivity.class));
        else if(n.startsWith("إعدادات")||n.equals("بيانات بقالة العزي")||n.equals("التوقيع والختم"))startActivity(new android.content.Intent(this,SettingsActivity.class));
        else startActivity(new android.content.Intent(this,ScreenActivity.class).putExtra("screen",n));
    }
    private void dispatch(){
        if(screen.equals("العملات")||screen.equals("أسعار الصرف"))currencies();
        else if(screen.equals("الصناديق")||screen.equals("حركة الصندوق"))boxes();
        else if(screen.equals("التنبيهات")||screen.equals("إضافة تنبيه"))alerts();
        else if(screen.equals("العروض")||screen.equals("إضافة عرض سعر")||screen.equals("الطلبات")||screen.equals("إضافة طلبية"))offers();
        else if(screen.equals("الأرشيف")||screen.equals("سلة المحذوفات"))archive();
        else if(screen.equals("الحاسبة"))calculator();
        else if(screen.equals("الأصول"))assets();
        else if(screen.equals("التقرير اليومي")||screen.equals("التقرير النهائي")||screen.startsWith("تقارير"))report();
        else if(screen.equals("سند قبض")||screen.equals("سند صرف")||screen.equals("عملية حساب")||screen.equals("الحسابات المدينة"))startActivity(new android.content.Intent(this,AccountsActivity.class).putExtra("operation",true));
        else {actionPanel();}
    }
    private void currencies(){
        Button add=Ui.button(this,"＋ إضافة عملة",v->currencyForm());list.addView(add,new LinearLayout.LayoutParams(-1,54));
        List<Map<String,Object>> rows=repo.rows("SELECT id,name,symbol,rate FROM currencies ORDER BY id DESC");
        for(Map<String,Object>x:rows){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("name")),18,true,Ui.TEXT));c.addView(Ui.text(this,"الرمز: "+x.get("symbol")+"  |  سعر الصرف: "+Ui.number(((Number)x.get("rate")).doubleValue()),14,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,86));}
    }
    private void currencyForm(){LinearLayout f=form();EditText n=Ui.edit(this,"اسم العملة",false),s=Ui.edit(this,"الرمز",false),r=Ui.edit(this,"سعر الصرف",true);f.addView(n);f.addView(s);f.addView(r);new AlertDialog.Builder(this).setTitle("إضافة عملة").setView(f).setPositiveButton("حفظ",(d,w)->{AppDatabase.get(this).getWritableDatabase().execSQL("INSERT INTO currencies(name,symbol,rate) VALUES(?,?,?)",new Object[]{n.getText().toString(),s.getText().toString(),Ui.num(r.getText().toString())});render();}).show();}
    private void boxes(){
        Button add=Ui.button(this,"＋ إضافة صندوق",v->boxForm());list.addView(add,new LinearLayout.LayoutParams(-1,54));
        for(Map<String,Object>x:repo.rows("SELECT id,name,kind,balance,currency FROM cash_boxes ORDER BY id DESC")){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("name")),18,true,Ui.TEXT));c.addView(Ui.text(this,"الرصيد: "+Ui.number(((Number)x.get("balance")).doubleValue())+" "+x.get("currency"),15,true,Ui.BLUE));list.addView(c,new LinearLayout.LayoutParams(-1,84));}
        Button mv=Ui.button(this,"حركة الصندوق",v->{List<Map<String,Object>> os=repo.rows("SELECT party_name,type,amount,created_at FROM operations ORDER BY id DESC LIMIT 100");list.removeAllViews();for(Map<String,Object>x:os){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("type"))+"  "+String.valueOf(x.get("party_name")),16,true,Ui.TEXT));c.addView(Ui.text(this,Ui.number(((Number)x.get("amount")).doubleValue())+"  |  "+x.get("created_at"),13,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,80));}});list.addView(mv,new LinearLayout.LayoutParams(-1,54));
    }
    private void boxForm(){EditText n=Ui.edit(this,"اسم الصندوق",false);new AlertDialog.Builder(this).setTitle("إضافة صندوق").setView(n).setPositiveButton("حفظ",(d,w)->{android.content.ContentValues v=new android.content.ContentValues();v.put("name",n.getText().toString());v.put("kind","cash");v.put("balance",0);v.put("currency","ريال");AppDatabase.get(this).getWritableDatabase().insertOrThrow("cash_boxes",null,v);render();}).show();}
    private void alerts(){
        Button add=Ui.button(this,"＋ تنبيه",v->alertForm());list.addView(add,new LinearLayout.LayoutParams(-1,54));
        for(Map<String,Object>x:repo.rows("SELECT id,due_date,details,done FROM alerts ORDER BY id DESC")){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("due_date")),16,true,Ui.BLUE));c.addView(Ui.text(this,String.valueOf(x.get("details")),14,false,Ui.TEXT));c.addView(Ui.text(this,"الحالة: "+(((Number)x.get("done")).intValue()==1?"منجز":"قادم"),12,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,96));}
    }
    private void alertForm(){LinearLayout f=form();EditText d=Ui.edit(this,"التاريخ YYYY-MM-DD",false),t=Ui.edit(this,"التفاصيل",false);f.addView(d);f.addView(t);new AlertDialog.Builder(this).setTitle("إضافة تنبيه").setView(f).setPositiveButton("حفظ",(x,w)->{android.content.ContentValues v=new android.content.ContentValues();v.put("due_date",d.getText().toString());v.put("details",t.getText().toString());AppDatabase.get(this).getWritableDatabase().insert("alerts",null,v);render();}).show();}
    private void offers(){Button add=Ui.button(this,screen.contains("طلب")?"＋ طلبية":"＋ عرض سعر",v->offerForm(screen.contains("طلب")));list.addView(add,new LinearLayout.LayoutParams(-1,54));String table=screen.contains("طلب")?"orders":"offers";for(Map<String,Object>x:repo.rows("SELECT id,number,party_name,total,status,created_at FROM "+table+" ORDER BY id DESC")){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("number")),17,true,Ui.BLUE));c.addView(Ui.text(this,String.valueOf(x.get("party_name"))+"  |  "+Ui.number(((Number)x.get("total")).doubleValue()),14,true,Ui.TEXT));c.addView(Ui.text(this,String.valueOf(x.get("status"))+"  |  "+x.get("created_at"),12,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,90));}}
    private void offerForm(boolean order){LinearLayout f=form();EditText n=Ui.edit(this,"رقم العرض/الطلبية",false),p=Ui.edit(this,"اسم العميل/الجهة",false),t=Ui.edit(this,"الإجمالي",true),note=Ui.edit(this,"ملاحظات",false);f.addView(n);f.addView(p);f.addView(t);f.addView(note);new AlertDialog.Builder(this).setTitle(order?"إضافة طلبية":"إضافة عرض سعر").setView(f).setPositiveButton("حفظ",(d,w)->{ContentValuesHolder.save(this,order,n.getText().toString(),p.getText().toString(),Ui.num(t.getText().toString()),note.getText().toString());render();}).show();}
    private void archive(){for(Map<String,Object>x:repo.rows("SELECT id,table_name,record_id,deleted_at FROM deleted_records ORDER BY id DESC")){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,"حذف: "+x.get("table_name")+" #"+x.get("record_id"),16,true,Ui.TEXT));c.addView(Ui.text(this,String.valueOf(x.get("deleted_at")),12,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,76));}if(list.getChildCount()==0)list.addView(Ui.text(this,"لا توجد سجلات محذوفة",16,true,Ui.MUTED));}
    private void calculator(){EditText a=Ui.edit(this,"الرقم الأول",true),b=Ui.edit(this,"الرقم الثاني",true);list.addView(a,new LinearLayout.LayoutParams(-1,54));list.addView(b,new LinearLayout.LayoutParams(-1,54));TextView out=Ui.text(this,"النتيجة: 0",22,true,Ui.BLUE);list.addView(out,new LinearLayout.LayoutParams(-1,64));String[] ops={"+","−","×","÷"};for(String op:ops){list.addView(Ui.button(this,op,v->{double x=Ui.num(a.getText().toString()),y=Ui.num(b.getText().toString());double z=op.equals("+")?x+y:op.equals("−")?x-y:op.equals("×")?x*y:(y==0?0:x/y);out.setText("النتيجة: "+Ui.number(z));}),new LinearLayout.LayoutParams(-1,48));}}
    private void assets(){addMetric("قيمة المخزون بالشراء","SELECT IFNULL(SUM(stock*buy_price),0) FROM products");addMetric("قيمة المخزون بالبيع","SELECT IFNULL(SUM(stock*sell_price),0) FROM products");addMetric("عدد المنتجات","SELECT COUNT(*) FROM products WHERE active=1");}
    private void report(){addMetric("مبيعات نقدية","SELECT IFNULL(SUM(total),0) FROM invoices WHERE type='بيع' AND payment_method='نقد'");addMetric("مبيعات آجلة","SELECT IFNULL(SUM(total),0) FROM invoices WHERE type='بيع' AND remaining>0");addMetric("مشتريات","SELECT IFNULL(SUM(total),0) FROM invoices WHERE type='شراء'");addMetric("ربح تقريبي","SELECT IFNULL(SUM((i.unit_price-i.buy_price)*i.qty),0) FROM invoice_items i JOIN invoices h ON h.id=i.invoice_id WHERE h.type='بيع'");}
    private void addMetric(String title,String sql){List<Map<String,Object>> rows=repo.rows(sql);double v=0;if(!rows.isEmpty()&&!rows.get(0).isEmpty()){Object o=rows.get(0).values().iterator().next();if(o instanceof Number)v=((Number)o).doubleValue();}LinearLayout c=Ui.card(this);c.addView(Ui.text(this,title,15,false,Ui.MUTED));c.addView(Ui.text(this,Ui.number(v),22,true,Ui.BLUE));list.addView(c,new LinearLayout.LayoutParams(-1,86));}
    private void actionPanel(){String[] actions={"فتح الفواتير","فتح الحسابات","فتح الأصناف","فتح التقارير","فتح الملاحظات","فتح النسخ الاحتياطي","فتح الإعدادات"};for(String a:actions)list.addView(Ui.button(this,a,v->startActivity(new android.content.Intent(this,a.contains("الفواتير")?InvoiceActivity.class:a.contains("الحسابات")?AccountsActivity.class:a.contains("الأصناف")?ProductsActivity.class:a.contains("التقارير")?ReportsActivity.class:a.contains("الملاحظات")?NotesActivity.class:a.contains("النسخ")?BackupActivity.class:SettingsActivity.class))),new LinearLayout.LayoutParams(-1,54));}
    private LinearLayout form(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(24,8,24,8);return f;}
    static final class ContentValuesHolder{static void save(android.content.Context c,boolean order,String number,String party,double total,String note){android.content.ContentValues v=new android.content.ContentValues();v.put("number",number);v.put(order?"party_name":"party_name",party);v.put("total",total);v.put("note",note);v.put("created_at",Ui.now());AppDatabase.get(c).getWritableDatabase().insertOrThrow(order?"orders":"offers",null,v);}}
}
