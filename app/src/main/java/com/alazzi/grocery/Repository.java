package com.alazzi.grocery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;
import java.util.concurrent.*;

public final class Repository {
    private final AppDatabase db;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    public Repository(Context c){db=AppDatabase.get(c);db.getWritableDatabase();}

    public interface Callback<T>{void ok(T value);void fail(Exception e);}
    public <T> void run(Callable<T> job,Callback<T> cb){
        io.execute(()->{try{T v=job.call();android.os.Handler h=new android.os.Handler(android.os.Looper.getMainLooper());h.post(()->cb.ok(v));}catch(Exception e){new android.os.Handler(android.os.Looper.getMainLooper()).post(()->cb.fail(e));}});
    }

    public List<Map<String,Object>> rows(String sql,String... args){
        List<Map<String,Object>> out=new ArrayList<>();
        try(Cursor c=db.getReadableDatabase().rawQuery(sql,args)){String[] names=c.getColumnNames();while(c.moveToNext()){Map<String,Object> r=new LinkedHashMap<>();for(int i=0;i<names.length;i++)r.put(names[i],c.isNull(i)?null:read(c,i));out.add(r);}}return out;
    }
    private Object read(Cursor c,int i){switch(c.getType(i)){case Cursor.FIELD_TYPE_INTEGER:return c.getLong(i);case Cursor.FIELD_TYPE_FLOAT:return c.getDouble(i);case Cursor.FIELD_TYPE_BLOB:return c.getBlob(i);default:return c.getString(i);}}
    public long addCustomer(String n,String phone,double limit){return addParty("customers",n,phone,limit);}
    public long addSupplier(String n,String phone,double limit){return addParty("suppliers",n,phone,limit);}
    private long addParty(String table,String n,String phone,double limit){
        if(n==null||n.trim().isEmpty())throw new IllegalArgumentException("الاسم مطلوب");
        ContentValues v=new ContentValues();v.put("name",n.trim());v.put("phone",phone==null?"":phone.trim());v.put("credit_limit",limit);v.put("opening_balance",0);v.put("created_at",Ui.now());
        return db.getWritableDatabase().insertOrThrow(table,null,v);
    }
    public long addProduct(String name,String barcode,String unit,double buy,double sell,double stock,long warehouse){
        if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("اسم الصنف مطلوب");
        ContentValues v=new ContentValues();v.put("name",name.trim());v.put("barcode",barcode==null?null:barcode.trim());v.put("unit",unit==null||unit.trim().isEmpty()?"حبة":unit.trim());v.put("buy_price",buy);v.put("sell_price",sell);v.put("stock",stock);v.put("warehouse_id",warehouse);v.put("created_at",Ui.now());
        long id=db.getWritableDatabase().insertOrThrow("products",null,v);
        db.getWritableDatabase().insertOrThrow("product_prices",null,AppDatabase.values("product_id",String.valueOf(id),"price_type","شراء","price",String.valueOf(buy)));
        db.getWritableDatabase().insertOrThrow("product_prices",AppDatabase.values("product_id",String.valueOf(id),"price_type","بيع","price",String.valueOf(sell)));
        return id;
    }
    public List<Map<String,Object>> parties(String table,String q){
        return rows("SELECT p.id,p.name,p.phone,p.credit_limit,p.opening_balance+IFNULL((SELECT SUM(o.debit-o.credit) FROM operations o WHERE o.party_type=? AND o.party_id=p.id),0) balance FROM "+table+" p WHERE p.name LIKE ? ORDER BY p.id DESC",table.equals("customers")?"customer":"supplier","%"+(q==null?"":q)+"%");
    }
    public List<Map<String,Object>> products(String q){return rows("SELECT id,name,barcode,unit,buy_price,sell_price,stock,warehouse_id FROM products WHERE active=1 AND (name LIKE ? OR IFNULL(barcode,'') LIKE ?) ORDER BY id DESC","%"+(q==null?"":q)+"%","%"+(q==null?"":q)+"%");}

    public long saveInvoice(final String type,final String partyType,final long partyId,final String partyName,final String payment,final double total,final double discount,final double paid,final long boxId,final List<Map<String,Object>> items){
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            long id=DatabaseNext.next(d,"invoices");String number=String.format(Locale.US,"%s-%06d",type.equals("شراء")?"P":"S",id);
            ContentValues iv=new ContentValues();iv.put("number",number);iv.put("type",type);iv.put("party_type",partyType);iv.put("party_id",partyId);iv.put("party_name",partyName);iv.put("cash_box_id",boxId);iv.put("total",total);iv.put("discount",discount);iv.put("paid",paid);iv.put("remaining",Math.max(0,total-paid));iv.put("payment_method",payment);iv.put("created_at",Ui.now());
            d.insertOrThrow("invoices",null,iv);
            for(Map<String,Object> item:items){
                long pid=item.get("product_id") instanceof Number?((Number)item.get("product_id")).longValue():0;double qty=Ui.num(String.valueOf(item.get("qty")));double price=Ui.num(String.valueOf(item.get("price")));double line=Ui.num(String.valueOf(item.get("total")));
                ContentValues x=new ContentValues();x.put("invoice_id",id);if(pid>0)x.put("product_id",pid);x.put("product_name",String.valueOf(item.get("name")));x.put("qty",qty);x.put("unit",String.valueOf(item.get("unit")));x.put("unit_price",price);x.put("buy_price",0);x.put("total",line);d.insertOrThrow("invoice_items",null,x);
                if(pid>0){double delta=type.contains("بيع")?-qty:qty;d.execSQL("UPDATE products SET stock=stock+? WHERE id=?",new Object[]{delta,pid});ContentValues sm=new ContentValues();sm.put("product_id",pid);sm.put("warehouse_id",1);sm.put("qty",delta);sm.put("move_type",type);sm.put("ref_id",id);sm.put("details","الفاتورة "+number);sm.put("created_at",Ui.now());d.insertOrThrow("stock_moves",null,sm);}
            }
            double rem=Math.max(0,total-paid);
            if(partyId>0){
                ContentValues op=new ContentValues();op.put("party_type",partyType);op.put("party_id",partyId);op.put("party_name",partyName);op.put("type",type);op.put("details","مقابل فاتورة "+number);op.put("debit",type.contains("بيع")?rem:0);op.put("credit",type.contains("شراء")?rem:0);op.put("amount",rem);op.put("cash_box_id",boxId);op.put("ref_id",id);op.put("created_at",Ui.now());d.insertOrThrow("operations",null,op);
            }
            if(paid>0)d.execSQL("UPDATE cash_boxes SET balance=balance+? WHERE id=?",new Object[]{type.contains("شراء")?-paid:paid,boxId});
            d.setTransactionSuccessful();return id;
        }finally{d.endTransaction();}
    }

    public long addOperation(String partyType,long partyId,String partyName,String type,String details,double debit,double credit,double amount,long boxId){
        SQLiteDatabase d=db.getWritableDatabase();ContentValues v=new ContentValues();v.put("party_type",partyType);v.put("party_id",partyId);v.put("party_name",partyName);v.put("type",type);v.put("details",details);v.put("debit",debit);v.put("credit",credit);v.put("amount",amount);v.put("cash_box_id",boxId);v.put("created_at",Ui.now());long id=d.insertOrThrow("operations",null,v);
        d.execSQL("UPDATE cash_boxes SET balance=balance+? WHERE id=?",new Object[]{debit-credit,boxId});return id;
    }

    static final class DatabaseNext{static long next(SQLiteDatabase d,String table){Cursor c=d.rawQuery("SELECT IFNULL(MAX(id),0)+1 FROM "+table,null);try{c.moveToFirst();return c.getLong(0);}finally{c.close();}}}
}
