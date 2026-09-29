package com.alazzi.grocery;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;

public class TransferActivity extends androidx.appcompat.app.AppCompatActivity {
    private Repository repo;private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new Repository(this);build();if(getIntent().getBooleanExtra("add",false))showForm();}
    private void build(){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);r.setBackgroundColor(Ui.BG);
        r.addView(Ui.titleBar(this,"الحوالات",v->finish(),v->{}),new LinearLayout.LayoutParams(-1,64));
        Button add=Ui.button(this,"＋ حوالة جديدة",v->showForm());r.addView(add,new LinearLayout.LayoutParams(-1,54));
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);ScrollView s=new ScrollView(this);s.addView(list);r.addView(s,new LinearLayout.LayoutParams(-1,0,1));setContentView(r);load();
    }
    private void load(){List<Map<String,Object>> rows=repo.rows("SELECT id,sender_name,sender_phone,receiver_name,receiver_phone,amount,commission,status,created_at FROM transfers ORDER BY id DESC");list.removeAllViews();for(Map<String,Object> x:rows){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(x.get("receiver_name"))+" ← "+String.valueOf(x.get("sender_name")),17,true,Ui.TEXT));c.addView(Ui.text(this,"المبلغ: "+Ui.number(((Number)x.get("amount")).doubleValue())+"  •  عمولة: "+Ui.number(((Number)x.get("commission")).doubleValue()),14,true,Ui.BLUE));c.addView(Ui.text(this,String.valueOf(x.get("receiver_phone"))+"  |  "+String.valueOf(x.get("created_at")),12,false,Ui.MUTED));c.setOnClickListener(v->{Map<String,Object>d=new HashMap<>();d.put("business","بقالة العزي للمواد الغذائية");d.put("type","حوالة");d.put("number","TR-"+x.get("id"));d.put("party",String.valueOf(x.get("receiver_name")));d.put("total",x.get("amount"));d.put("paid",x.get("amount"));d.put("remaining",0);FileShare(c,d);});list.addView(c,new LinearLayout.LayoutParams(-1,106));}}
    private void FileShare(View view,Map<String,Object>d){String text="بقالة العزي للمواد الغذائية\nحوالة رقم "+d.get("number")+"\nالمستلم: "+d.get("party")+"\nالمبلغ: "+Ui.number(((Number)d.get("total")).doubleValue());SharePrint.shareText(this,text);}
    private void showForm(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);EditText sn=Ui.edit(this,"اسم المرسل",false),sp=Ui.edit(this,"رقم المرسل",true),rn=Ui.edit(this,"اسم المستلم",false),rp=Ui.edit(this,"رقم المستلم",true),amount=Ui.edit(this,"المبلغ",true),fee=Ui.edit(this,"العمولة",true),note=Ui.edit(this,"التفاصيل",false);for(EditText e:new EditText[]{amount,sn,sp,rn,rp,fee,note})f.addView(e);new android.app.AlertDialog.Builder(this).setTitle("حوالة جديدة").setView(f).setPositiveButton("حفظ",(d,w)->{ContentValuesHolder v=new ContentValuesHolder();v.sn=sn.getText().toString();v.sp=sp.getText().toString();v.rn=rn.getText().toString();v.rp=rp.getText().toString();v.amount=Ui.num(amount.getText().toString());v.fee=Ui.num(fee.getText().toString());v.note=note.getText().toString();repo.run(()->{android.content.ContentValues x=new android.content.ContentValues();x.put("sender_name",v.sn);x.put("sender_phone",v.sp);x.put("receiver_name",v.rn);x.put("receiver_phone",v.rp);x.put("amount",v.amount);x.put("commission",v.fee);x.put("note",v.note);x.put("created_at",Ui.now());return AppDatabase.get(this).getWritableDatabase().insertOrThrow("transfers",null,x);},new Repository.Callback<Long>(){public void ok(Long id){load();Toast.makeText(TransferActivity.this,"تم حفظ الحوالة ✓",Toast.LENGTH_SHORT).show();}public void fail(Exception e){Toast.makeText(TransferActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}});}).setNegativeButton("إلغاء",null).show();}
    static class ContentValuesHolder{String sn,sp,rn,rp,note;double amount,fee;}
}
