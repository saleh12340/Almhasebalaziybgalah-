package com.alazzi.grocery;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class AppDatabase extends SQLiteOpenHelper {
    private static final String NAME = "alazzi_grocery.db";
    private static final int VERSION = 2;
    private static AppDatabase instance;

    public static synchronized AppDatabase get(Context c) {
        if (instance == null) instance = new AppDatabase(c.getApplicationContext());
        return instance;
    }
    public static synchronized void reset(Context c) { if (instance != null) { instance.close(); instance=null; } get(c); }
    private AppDatabase(Context c) { super(c, NAME, null, VERSION); }

    @Override public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        db.enableWriteAheadLogging();
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,phone TEXT DEFAULT '',credit_limit REAL DEFAULT 0,opening_balance REAL DEFAULT 0,note TEXT DEFAULT '',created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,phone TEXT DEFAULT '',credit_limit REAL DEFAULT 0,opening_balance REAL DEFAULT 0,note TEXT DEFAULT '',created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,barcode TEXT UNIQUE,unit TEXT DEFAULT 'حبة',buy_price REAL DEFAULT 0,sell_price REAL DEFAULT 0,stock REAL DEFAULT 0,warehouse_id INTEGER DEFAULT 1,active INTEGER DEFAULT 1,created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE product_prices(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER NOT NULL,price_type TEXT NOT NULL,price REAL NOT NULL,UNIQUE(product_id,price_type),FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE warehouses(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,note TEXT DEFAULT '')");
        db.execSQL("CREATE TABLE stock_moves(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER NOT NULL,warehouse_id INTEGER NOT NULL,qty REAL NOT NULL,move_type TEXT NOT NULL,ref_id INTEGER,details TEXT DEFAULT '',created_at TEXT NOT NULL,FOREIGN KEY(product_id) REFERENCES products(id),FOREIGN KEY(warehouse_id) REFERENCES warehouses(id))");
        db.execSQL("CREATE TABLE cash_boxes(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,kind TEXT DEFAULT 'cash',balance REAL DEFAULT 0,currency TEXT DEFAULT 'ريال')");
        db.execSQL("CREATE TABLE currencies(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE,symbol TEXT DEFAULT '',rate REAL DEFAULT 1)");
        db.execSQL("CREATE TABLE invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,number TEXT NOT NULL UNIQUE,type TEXT NOT NULL,party_type TEXT,party_id INTEGER,party_name TEXT DEFAULT '',cash_box_id INTEGER,total REAL DEFAULT 0,discount REAL DEFAULT 0,tax REAL DEFAULT 0,paid REAL DEFAULT 0,remaining REAL DEFAULT 0,payment_method TEXT DEFAULT 'نقد',note TEXT DEFAULT '',created_at TEXT NOT NULL,voided INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE invoice_items(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER NOT NULL,product_id INTEGER,product_name TEXT NOT NULL,qty REAL DEFAULT 0,unit TEXT DEFAULT 'حبة',unit_price REAL DEFAULT 0,buy_price REAL DEFAULT 0,discount REAL DEFAULT 0,total REAL DEFAULT 0,FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,FOREIGN KEY(product_id) REFERENCES products(id))");
        db.execSQL("CREATE TABLE operations(id INTEGER PRIMARY KEY AUTOINCREMENT,party_type TEXT NOT NULL,party_id INTEGER,party_name TEXT DEFAULT '',type TEXT NOT NULL,details TEXT DEFAULT '',debit REAL DEFAULT 0,credit REAL DEFAULT 0,amount REAL DEFAULT 0,cash_box_id INTEGER,ref_id INTEGER,created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE transfers(id INTEGER PRIMARY KEY AUTOINCREMENT,sender_name TEXT DEFAULT '',sender_phone TEXT DEFAULT '',receiver_name TEXT DEFAULT '',receiver_phone TEXT DEFAULT '',amount REAL DEFAULT 0,commission REAL DEFAULT 0,note TEXT DEFAULT '',status TEXT DEFAULT 'معلق',created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE alerts(id INTEGER PRIMARY KEY AUTOINCREMENT,party_type TEXT,party_id INTEGER,due_date TEXT NOT NULL,details TEXT DEFAULT '',done INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT DEFAULT '',body TEXT DEFAULT '',created_at TEXT NOT NULL,updated_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE offers(id INTEGER PRIMARY KEY AUTOINCREMENT,number TEXT UNIQUE,type TEXT DEFAULT 'عرض سعر',party_name TEXT DEFAULT '',total REAL DEFAULT 0,note TEXT DEFAULT '',created_at TEXT NOT NULL,status TEXT DEFAULT 'مفتوح')");
        db.execSQL("CREATE TABLE orders(id INTEGER PRIMARY KEY AUTOINCREMENT,number TEXT UNIQUE,party_name TEXT DEFAULT '',total REAL DEFAULT 0,note TEXT DEFAULT '',created_at TEXT NOT NULL,status TEXT DEFAULT 'جديد')");
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,party_name TEXT DEFAULT '',phone TEXT DEFAULT '',message TEXT DEFAULT '',kind TEXT DEFAULT 'عام',created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY,value TEXT DEFAULT '')");
        db.execSQL("CREATE TABLE deleted_records(id INTEGER PRIMARY KEY AUTOINCREMENT,table_name TEXT NOT NULL,record_id INTEGER NOT NULL,payload TEXT DEFAULT '',deleted_at TEXT NOT NULL)");
        db.execSQL("CREATE INDEX idx_invoice_date ON invoices(created_at)");
        db.execSQL("CREATE INDEX idx_operations_party ON operations(party_type,party_id,created_at)");
        db.execSQL("CREATE INDEX idx_stock_moves_product ON stock_moves(product_id,created_at)");

        db.execSQL("INSERT INTO warehouses(name,note) VALUES('المخزن الرئيسي','المخزن الافتراضي')");
        db.execSQL("INSERT INTO cash_boxes(name,kind,balance,currency) VALUES('الصندوق الرئيسي','cash',0,'ريال')");
        db.execSQL("INSERT INTO cash_boxes(name,kind,balance,currency) VALUES('شبكة','network',0,'ريال')");
        db.execSQL("INSERT INTO currencies(name,symbol,rate) VALUES('الريال اليمني','ريال',1)");
        db.execSQL("INSERT INTO settings(key,value) VALUES('business_name','بقالة العزي للمواد الغذائية')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('business_phone','')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('print_width','58')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('receipt_font','13')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('printer_address','')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('tax_rate','0')");
        db.execSQL("INSERT INTO settings(key,value) VALUES('rtl','1')");
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE invoices ADD COLUMN payment_method TEXT DEFAULT 'نقد'");
            db.execSQL("ALTER TABLE invoices ADD COLUMN note TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE invoices ADD COLUMN voided INTEGER DEFAULT 0");
        }
    }

    public static ContentValues values(String... pairs) {
        ContentValues v=new ContentValues();
        for(int i=0;i+1<pairs.length;i+=2)v.put(pairs[i],pairs[i+1]);
        return v;
    }
}
