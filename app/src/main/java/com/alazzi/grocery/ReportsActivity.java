package com.alazzi.grocery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.*;
import java.util.*;

public class ReportsActivity extends androidx.appcompat.app.AppCompatActivity {
    private Repository repo; private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new Repository(this);build();}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Ui.BG);
        String party=getIntent().getStringExtra("party_name");long partyId=getIntent().getLongExtra("party_id",0);root.addView(Ui.titleBar(this,party==null?"التقارير":"كشف حساب: "+party,v->finish(),v->{}),new LinearLayout.LayoutParams(-1,64));
        ScrollView sc=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(10,10,10,20);sc.addView(list);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        if(partyId>0)partyReport(partyId,getIntent().getStringExtra("party_type"),party);else overview();
    }
    private void overview(){
        addMetric("مبيعات نقدًا","SELECT IFNULL(SUM(paid),0) FROM invoices WHERE type='بيع' AND payment_method='نقد'");
        addMetric("مبيعات آجل","SELECT IFNULL(SUM(remaining),0) FROM invoices WHERE type='بيع' AND remaining>0");
        addMetric("مشتريات","SELECT IFNULL(SUM(total),0) FROM invoices WHERE type='شراء'");
        addMetric("إجمالي المخزون","SELECT IFNULL(SUM(stock*buy_price),0) FROM products");
        addMetric("عدد الفواتير","SELECT COUNT(*) FROM invoices");
        Button daily=Ui.button(this,"تفاصيل الحركة اليومية",v->showDaily());list.addView(daily,new LinearLayout.LayoutParams(-1,54));
    }
    private void addMetric(String title,String sql){double v=((Number)repo.rows(sql).get(0).values().iterator().next()).doubleValue();LinearLayout c=Ui.card(this);c.addView(Ui.text(this,title,15,false,Ui.MUTED));c.addView(Ui.text(this,Ui.number(v),22,true,Ui.BLUE));list.addView(c,new LinearLayout.LayoutParams(-1,86));}
    private void partyReport(long id,String type,String name){
        String[] args={type,id+"",""+id};List<Map<String,Object>> rows=repo.rows("SELECT type,details,debit,credit,created_at FROM operations WHERE party_type=? AND party_id=? ORDER BY id DESC",type,id+"");
        double running=0;for(Map<String,Object> r:rows){running+=((Number)r.get("debit")).doubleValue()-((Number)r.get("credit")).doubleValue();}
        TextView bal=Ui.text(this,"الرصيد الحالي: "+Ui.number(Math.abs(running))+(running>=0?" عليك":" له"),18,true,running>=0?0xffb42318:0xff027a48);bal.setGravity(Gravity.CENTER);list.addView(bal,new LinearLayout.LayoutParams(-1,58));
        double cumulative=0;for(Map<String,Object> r:rows){cumulative+=((Number)r.get("debit")).doubleValue()-((Number)r.get("credit")).doubleValue();LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(r.get("type")),15,true,Ui.BLUE));c.addView(Ui.text(this,String.valueOf(r.get("details")),15,false,Ui.TEXT));c.addView(Ui.text(this,"المبلغ: "+Ui.number(Math.abs(((Number)r.get("debit")).doubleValue()-((Number)r.get("credit")).doubleValue()))+"  |  الرصيد: "+Ui.number(Math.abs(cumulative)),13,true,Ui.MUTED));c.addView(Ui.text(this,Ui.formatDate(String.valueOf(r.get("created_at"))),11,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,110));}
        Button share=Ui.button(this,"مشاركة كشف الحساب",v->{Map<String,Object> data=new HashMap<>();data.put("title","كشف حساب "+name);data.put("body",buildStatement(rows,name));SharePrint.shareText(this,String.valueOf(data.get("title"))+"\n"+data.get("body"));});list.addView(share,new LinearLayout.LayoutParams(-1,54));
    }
    private String buildStatement(List<Map<String,Object>> rows,String name){StringBuilder s=new StringBuilder("بقالة العزي للمواد الغذائية\nكشف حساب: "+name+"\n");for(Map<String,Object> r:rows)s.append(r.get("type")).append("  ").append(Ui.number(Math.abs(((Number)r.get("debit")).doubleValue()-((Number)r.get("credit")).doubleValue()))).append("\n");return s.toString();}
    private void showDaily(){List<Map<String,Object>> rows=repo.rows("SELECT number,type,party_name,total,paid,remaining,created_at FROM invoices ORDER BY id DESC LIMIT 100");list.removeAllViews();for(Map<String,Object> r:rows){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,String.valueOf(r.get("number"))+"  |  "+r.get("type"),16,true,Ui.BLUE));c.addView(Ui.text(this,String.valueOf(r.get("party_name")),14,true,Ui.TEXT));c.addView(Ui.text(this,"الإجمالي "+Ui.number(((Number)r.get("total")).doubleValue())+"  •  المدفوع "+Ui.number(((Number)r.get("paid")).doubleValue())+"  •  المتبقي "+Ui.number(((Number)r.get("remaining")).doubleValue()),13,false,Ui.MUTED));list.addView(c,new LinearLayout.LayoutParams(-1,96));}}
}
