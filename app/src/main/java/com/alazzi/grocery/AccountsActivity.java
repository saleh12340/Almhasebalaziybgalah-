package com.alazzi.grocery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;

public class AccountsActivity extends androidx.appcompat.app.AppCompatActivity {
    private Repository repo; private LinearLayout body; private boolean operationMode=false;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new Repository(this);operationMode=getIntent().getBooleanExtra("operation",false);build();}
    private void build(){
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);body.setBackgroundColor(Ui.BG);
        body.setPadding(Ui.dp(body,10),Ui.dp(body,8),Ui.dp(body,10),Ui.dp(body,12));
        body.addView(Ui.titleBar(this,"الحسابات والعملاء والموردون",v->finish(),v->{}),new LinearLayout.LayoutParams(-1,Ui.dp(body,64)));
        LinearLayout tabs=Ui.row(this);
        Button c=Ui.button(this,"العملاء",v->load("customers"));Button s=Ui.button(this,"الموردون",v->load("suppliers"));tabs.addView(c,new LinearLayout.LayoutParams(0,52,1));tabs.addView(s,new LinearLayout.LayoutParams(0,52,1));body.addView(tabs);
        Button add=Ui.button(this,"＋ حساب جديد",v->chooseAdd());body.addView(add,new LinearLayout.LayoutParams(-1,52));
        ScrollView sc=new ScrollView(this);LinearLayout list=Ui.card(this);list.setTag("list");sc.addView(list);body.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(body);
        if(operationMode)showOperationDialog("customer"); else load("customers");
        if(getIntent().getStringExtra("addKind")!=null)chooseAdd(getIntent().getStringExtra("addKind"));
    }
    private LinearLayout list(){return (LinearLayout)body.findViewWithTag("list");}
    private void load(String table){final String label=table.equals("customers")?"العميل":"المورد";repo.run(()->repo.parties(table,""),new Repository.Callback<List<Map<String,Object>>>(){public void ok(List<Map<String,Object>> rows){list().removeAllViews();for(Map<String,Object> r:rows){long id=((Number)r.get("id")).longValue();String name=String.valueOf(r.get("name"));double bal=((Number)r.get("balance")).doubleValue();String txt=(bal>0?"عليك ":"له ")+Ui.number(Math.abs(bal));LinearLayout card=Ui.card(AccountsActivity.this);card.addView(Ui.text(AccountsActivity.this,name,18,true,Ui.TEXT));TextView b=Ui.text(AccountsActivity.this,txt,15,true,bal>0?0xffb42318:0xff027a48);card.addView(b);if(String.valueOf(r.get("phone")).trim().length()>0)card.addView(Ui.text(AccountsActivity.this,String.valueOf(r.get("phone")),13,false,Ui.MUTED));card.setOnClickListener(v->startActivity(new android.content.Intent(AccountsActivity.this,ReportsActivity.class).putExtra("party_type",table.equals("customers")?"customer":"supplier").putExtra("party_id",id).putExtra("party_name",name)));list().addView(card,new LinearLayout.LayoutParams(-1,Ui.dp(card,96)));}}public void fail(Exception e){Toast.makeText(AccountsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();}});}
    private void chooseAdd(){new AlertDialog.Builder(this).setItems(new String[]{"إضافة عميل","إضافة مورد","عملية حساب قبض","عملية حساب صرف"},(d,w)->{if(w==0)chooseAdd("customer");else if(w==1)chooseAdd("supplier");else showOperationDialog(w==2?"customer":"customer");}).show();}
    private void chooseAdd(String kind){
        LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(32,10,32,5);
        EditText n=Ui.edit(this,"الاسم",false),p=Ui.edit(this,"رقم الهاتف",true),limit=Ui.edit(this,"حد الائتمان",true);f.addView(n);f.addView(p);f.addView(limit);
        new AlertDialog.Builder(this).setTitle(kind.equals("customer")?"إضافة عميل":"إضافة مورد").setView(f).setPositiveButton("حفظ",(d,w)->repo.run(()->kind.equals("customer")?repo.addCustomer(n.getText().toString(),p.getText().toString(),Ui.num(limit.getText().toString())):repo.addSupplier(n.getText().toString(),p.getText().toString(),Ui.num(limit.getText().toString())),new Repository.Callback<Long>(){public void ok(Long x){load(kind.equals("customer")?"customers":"suppliers");Toast.makeText(AccountsActivity.this,"تم الحفظ ✓",Toast.LENGTH_SHORT).show();}public void fail(Exception e){Toast.makeText(AccountsActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}})).setNegativeButton("إلغاء",null).show();
    }
    private void showOperationDialog(String type){
        repo.run(()->repo.parties("customers",""),new Repository.Callback<List<Map<String,Object>>>(){public void ok(List<Map<String,Object>> rows){if(rows.isEmpty()){Toast.makeText(AccountsActivity.this,"أضف حسابًا أولًا",Toast.LENGTH_SHORT).show();return;}String[] names=new String[rows.size()];for(int i=0;i<rows.size();i++)names[i]=String.valueOf(rows.get(i).get("name"));new AlertDialog.Builder(AccountsActivity.this).setTitle("اختر الحساب").setItems(names,(d,w)->operationForm(rows.get(w))).show();}public void fail(Exception e){}}); }
    private void operationForm(Map<String,Object> r){
        LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);EditText amount=Ui.edit(this,"المبلغ",true),details=Ui.edit(this,"التفاصيل",false);f.addView(amount);f.addView(details);
        new AlertDialog.Builder(this).setTitle("عملية حساب").setView(f).setPositiveButton("حفظ",(d,w)->{double a=Ui.num(amount.getText().toString());repo.run(()->repo.addOperation("customer",((Number)r.get("id")).longValue(),String.valueOf(r.get("name")),"سند حساب",details.getText().toString(),a,0,a,1),new Repository.Callback<Long>(){public void ok(Long x){Toast.makeText(AccountsActivity.this,"تم تحديث عملياته ✓",Toast.LENGTH_SHORT).show();load("customers");}public void fail(Exception e){Toast.makeText(AccountsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();}});}).setNegativeButton("إلغاء",null).show();
    }
}
