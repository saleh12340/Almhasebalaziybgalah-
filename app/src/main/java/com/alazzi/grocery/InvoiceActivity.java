package com.alazzi.grocery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class InvoiceActivity extends androidx.appcompat.app.AppCompatActivity {
    private Repository repo;
    private String type="بيع", partyType="customer", partyName="";
    private long partyId=0;
    private final ArrayList<Map<String,Object>> cart=new ArrayList<>();
    private LinearLayout body,cartBox;
    private TextView totalView,partyView;
    private EditText qtyEdit,discountEdit,paidEdit;
    private AutoCompleteTextView productEdit;
    private final ArrayList<String> productNames=new ArrayList<>();
    private final HashMap<String,Map<String,Object>> byName=new HashMap<>();
    private boolean saving=false;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);repo=new Repository(this);
        type=getIntent().getStringExtra("type");
        if(type==null)type="بيع";
        build();
        loadProducts();
    }
    private void build(){
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);body.setBackgroundColor(Ui.BG);body.setPadding(Ui.dp(body,10),Ui.dp(body,8),Ui.dp(body,10),Ui.dp(body,16));
        body.addView(Ui.titleBar(this,"فاتورة "+type,v->finish(),v->{}),new LinearLayout.LayoutParams(-1,Ui.dp(body,64)));
        totalView=Ui.text(this,"الإجمالي 0",21,true,Ui.BLUE);totalView.setGravity(Gravity.CENTER);totalView.setBackground(Ui.round(Ui.WHITE,16,0xffdfe4ec));
        body.addView(totalView,new LinearLayout.LayoutParams(-1,Ui.dp(body,58)));

        LinearLayout typeRow=Ui.row(this);
        Button sale=Ui.button(this,"بيع",v->{type="بيع";partyType="customer";partyId=0;partyName="";partyView.setText("الحساب: نقدي");refreshTitle();});
        Button buy=Ui.button(this,"شراء",v->{type="شراء";partyType="supplier";partyId=0;partyName="";partyView.setText("الحساب: نقدي");refreshTitle();});
        typeRow.addView(sale,new LinearLayout.LayoutParams(0,Ui.dp(typeRow,50),1));typeRow.addView(buy,new LinearLayout.LayoutParams(0,Ui.dp(typeRow,50),1));body.addView(typeRow);

        partyView=Ui.text(this,"الحساب: نقدي",15,true,Ui.TEXT);partyView.setBackground(Ui.round(Ui.WHITE,14,0xffdfe4ec));partyView.setPadding(Ui.dp(partyView,12),0,Ui.dp(partyView,12),0);partyView.setOnClickListener(v->chooseParty());
        body.addView(partyView,new LinearLayout.LayoutParams(-1,Ui.dp(partyView,52)));
        body.addView(Ui.spacer(this,5));

        LinearLayout item=Ui.row(this);
        qtyEdit=Ui.edit(this,"الكمية",true);item.addView(qtyEdit,new LinearLayout.LayoutParams(0,Ui.dp(item,54),0.65f));
        productEdit=new AutoCompleteTextView(this);productEdit.setHint("اسم الصنف أو الباركود");productEdit.setTextSize(16);productEdit.setTextColor(Ui.TEXT);productEdit.setSingleLine(true);productEdit.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);productEdit.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);productEdit.setBackground(Ui.round(Ui.WHITE,14,0xffd9dee8));productEdit.setPadding(Ui.dp(productEdit,10),0,Ui.dp(productEdit,10),0);
        item.addView(productEdit,new LinearLayout.LayoutParams(0,Ui.dp(item,54),1.7f));
        Button add=Ui.button(this,"إضافة",v->addItem());item.addView(add,new LinearLayout.LayoutParams(0,Ui.dp(item,54),0.75f));body.addView(item);
        body.addView(Ui.text(this,"سعر الوحدة يُؤخذ تلقائيًا من بطاقة الصنف، ويمكن تعديله لاحقًا من إدارة الأسعار.",12,false,Ui.MUTED));
        cartBox=new LinearLayout(this);cartBox.setOrientation(LinearLayout.VERTICAL);body.addView(cartBox,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout sums=Ui.row(this);
        discountEdit=Ui.edit(this,"الخصم",true);paidEdit=Ui.edit(this,"المدفوع",true);
        sums.addView(discountEdit,new LinearLayout.LayoutParams(0,Ui.dp(sums,52),1));sums.addView(paidEdit,new LinearLayout.LayoutParams(0,Ui.dp(sums,52),1));body.addView(sums);
        RadioGroup pay=new RadioGroup(this);pay.setOrientation(RadioGroup.HORIZONTAL);pay.setGravity(Gravity.CENTER);pay.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        String[] ps={"نقد","آجل","شبكة"};for(String p:ps){RadioButton rb=new RadioButton(this);rb.setText(p);rb.setTextSize(14);rb.setId(View.generateViewId());pay.addView(rb,new RadioGroup.LayoutParams(0,52,1));if(p.equals("نقد"))rb.setChecked(true);}body.addView(pay);
        Button save=Ui.button(this,"حفظ الفاتورة",v->{String pm="نقد";for(int i=0;i<pay.getChildCount();i++){RadioButton rb=(RadioButton)pay.getChildAt(i);if(rb.isChecked())pm=rb.getText().toString();}save(pm);});
        body.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(save,56)));

        ScrollView sc=new ScrollView(this);sc.addView(body);setContentView(sc);
        discountEdit.setOnFocusChangeListener((v,f)->{if(!f)recalc();});paidEdit.setOnFocusChangeListener((v,f)->{if(!f)recalc();});
    }
    private void refreshTitle(){ }
    private void loadProducts(){
        repo.run(()->repo.products(""),new Repository.Callback<List<Map<String,Object>>>() {
            @Override public void ok(List<Map<String,Object>> rows){
                productNames.clear();byName.clear();
                for(Map<String,Object> r:rows){String n=String.valueOf(r.get("name"));productNames.add(n);byName.put(n,r);String b=String.valueOf(r.get("barcode"));if(b!=null&&!b.equals("null")&&!b.isEmpty()){productNames.add(b);byName.put(b,r);}}
                productEdit.setAdapter(new ArrayAdapter<>(InvoiceActivity.this,android.R.layout.simple_dropdown_item_1line,productNames));
            }
            @Override public void fail(Exception e){Toast.makeText(InvoiceActivity.this,"تعذر تحميل الأصناف",Toast.LENGTH_SHORT).show();}
        });
    }
    private void chooseParty(){
        final String table=type.equals("شراء")?"suppliers":"customers";final String label=type.equals("شراء")?"الموردون":"العملاء";
        repo.run(()->repo.parties(table,""),new Repository.Callback<List<Map<String,Object>>>() {
            @Override public void ok(List<Map<String,Object>> rows){
                String[] names=new String[rows.size()+1];names[0]="نقدي / بدون حساب";for(int i=0;i<rows.size();i++)names[i+1]=String.valueOf(rows.get(i).get("name"));
                new AlertDialog.Builder(InvoiceActivity.this).setTitle(label).setItems(names,(d,w)->{
                    if(w==0){partyId=0;partyName="";partyView.setText("الحساب: نقدي");}
                    else{Map<String,Object> r=rows.get(w-1);partyId=((Number)r.get("id")).longValue();partyName=String.valueOf(r.get("name"));partyView.setText("الحساب: "+partyName);}
                }).show();
            }
            @Override public void fail(Exception e){Toast.makeText(InvoiceActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();}
        });
    }
    private void addItem(){
        String key=productEdit.getText().toString().trim();Map<String,Object> p=byName.get(key);
        if(p==null){for(String n:productNames)if(n.equalsIgnoreCase(key)){p=byName.get(n);break;}}
        if(p==null){Toast.makeText(this,"اختر صنفًا من القائمة أو أدخل اسمًا صحيحًا",Toast.LENGTH_SHORT).show();return;}
        double qty=Ui.num(qtyEdit.getText().toString());if(qty<=0)qty=1;
        double price=((Number)p.get(type.equals("شراء")?"buy_price":"sell_price")).doubleValue();
        Map<String,Object> it=new HashMap<>();it.put("product_id",((Number)p.get("id")).longValue());it.put("name",p.get("name"));it.put("qty",qty);it.put("unit",p.get("unit"));it.put("price",price);it.put("total",qty*price);
        cart.add(it);productEdit.setText("");qtyEdit.setText("");renderCart();recalc();
    }
    private void renderCart(){
        cartBox.removeAllViews();int n=cart.size();
        for(int i=0;i<n;i++){final int index=i;Map<String,Object> it=cart.get(i);LinearLayout r=Ui.card(this);TextView t=Ui.text(this,(i+1)+". "+it.get("name")+"  ×  "+Ui.number(Ui.num(String.valueOf(it.get("qty")))),15,true,Ui.TEXT);TextView p=Ui.text(this,"سعر "+Ui.number(Ui.num(String.valueOf(it.get("price"))))+"  |  المجموع "+Ui.number(Ui.num(String.valueOf(it.get("total")))),14,false,Ui.MUTED);Button del=Ui.button(this,"حذف",v->{cart.remove(index);renderCart();recalc();});r.addView(t);r.addView(p);r.addView(del,new LinearLayout.LayoutParams(-1,Ui.dp(del,40)));cartBox.addView(r,new LinearLayout.LayoutParams(-1,Ui.dp(r,104)));}
    }
    private double subtotal(){double s=0;for(Map<String,Object> x:cart)s+=Ui.num(String.valueOf(x.get("total")));return s;}
    private double total(){return Math.max(0,subtotal()-Ui.num(discountEdit==null?"":discountEdit.getText().toString()));}
    private void recalc(){if(totalView!=null)totalView.setText("الإجمالي  "+Ui.number(total()));}
    private void save(String payment){
        if(saving)return;if(cart.isEmpty()){Toast.makeText(this,"أضف صنفًا واحدًا على الأقل",Toast.LENGTH_SHORT).show();return;}
        double total=total(),paid=Ui.num(paidEdit.getText().toString());if(payment.equals("آجل"))paid=0; if(paid>total)paid=total;
        saving=true;
        final double fpaid=paid,fd=Ui.num(discountEdit.getText().toString()),ft=total;
        repo.run(()->repo.saveInvoice(type,partyType,partyId,partyName,payment,ft,fd,fpaid,payment.equals("شبكة")?2:1,cart),new Repository.Callback<Long>(){
            @Override public void ok(Long id){saving=false;Map<String,Object> data=new HashMap<>();data.put("id",id);data.put("type",type);data.put("party",partyName);data.put("total",ft);data.put("paid",fpaid);data.put("remaining",Math.max(0,ft-fpaid));data.put("number",String.format(Locale.US,"%s-%06d",type.equals("شراء")?"P":"S",id));data.put("items",new ArrayList<>(cart));SharePrint.shareAndPrintPrompt(InvoiceActivity.this,data);cart.clear();renderCart();paidEdit.setText("");discountEdit.setText("");productEdit.setText("");recalc();}
            @Override public void fail(Exception e){saving=false;new AlertDialog.Builder(InvoiceActivity.this).setMessage(e.getMessage()==null?"تعذر حفظ الفاتورة":e.getMessage()).setPositiveButton("حسنًا",null).show();}
        });
    }
}
