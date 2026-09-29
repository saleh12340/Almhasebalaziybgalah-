package com.alazzi.grocery;

import android.Manifest;
import android.app.AlertDialog;
import android.widget.Toast;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import android.bluetooth.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public final class SharePrint {
    private SharePrint(){}

    public static Bitmap receipt(Context c, Map<String,Object> data){
        int w=384, margin=20, lineH=27;
        List<String> lines=new ArrayList<>();
        String name=s(data.get("business"),"بقالة العزي للمواد الغذائية");
        lines.add(name); lines.add("=".repeat(31));
        lines.add(s(data.get("type"),"فاتورة")); lines.add("رقم: "+s(data.get("number"),"")); lines.add(s(data.get("date"),Ui.now()));
        if(!s(data.get("party"),"").isEmpty())lines.add("الحساب: "+s(data.get("party"),""));
        Object items=data.get("items");
        if(items instanceof List){for(Object o:(List<?>)items)if(o instanceof Map){Map<?,?> m=(Map<?,?>)o;lines.add(s(m.get("name"),"")+" × "+Ui.number(Ui.num(s(m.get("qty"),"1")))+"  "+Ui.number(Ui.num(s(m.get("total"),"0"))));}}
        lines.add("=".repeat(31));lines.add("الإجمالي: "+Ui.number(Ui.num(s(data.get("total"),"0"))));
        lines.add("المدفوع: "+Ui.number(Ui.num(s(data.get("paid"),"0"))));
        lines.add("المتبقي: "+Ui.number(Ui.num(s(data.get("remaining"),"0"))));
        String cum=s(data.get("cumulative"),"");if(!cum.isEmpty())lines.add("الرصيد التراكمي: "+cum);
        lines.add("");lines.add("بقالة العزي");lines.add("شكراً لتعاملكم معنا");
        Bitmap b=Bitmap.createBitmap(w,Math.max(150,margin*2+lineH*lines.size()),Bitmap.Config.ARGB_8888);Canvas cn=new Canvas(b);cn.drawColor(Color.WHITE);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));p.setTextSize(18);p.setTextAlign(Paint.Align.RIGHT);
        float y=margin+18;for(String line:lines){p.setTypeface(line.equals(name)?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setTextSize(line.equals(name)?22:18);cn.drawText(line,w-margin,y,p);y+=lineH;}
        return b;
    }
    private static String s(Object o,String d){return o==null?d:String.valueOf(o);}

    public static void shareAndPrintPrompt(Context c,Map<String,Object> data){
        Bitmap b=receipt(c,data);
        File f=saveBitmap(c,b,"invoice_"+System.currentTimeMillis()+".png");
        new AlertDialog.Builder(c).setMessage("تم حفظ الفاتورة ✓").setPositiveButton("مشاركة", (d,w)->shareFile(c,f,"image/png"))
          .setNeutralButton("واتساب", (d,w)->shareToPackage(c,f,"com.whatsapp","image/png"))
          .setNegativeButton("طباعة 58mm", (d,w)->printSavedOrChoose(c,b)).show();
    }
    public static void shareText(Context c,String text){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);c.startActivity(Intent.createChooser(i,"مشاركة"));
    }
    public static void shareFile(Context c,File f,String type){
        Uri u=FileProvider.getUriForFile(c,c.getString(com.alazzi.grocery.R.string.share_authority),f);
        Intent i=new Intent(Intent.ACTION_SEND);i.setType(type);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);c.startActivity(Intent.createChooser(i,"مشاركة"));
    }
    public static void shareToPackage(Context c,File f,String pkg,String type){
        try{Uri u=FileProvider.getUriForFile(c,c.getString(com.alazzi.grocery.R.string.share_authority),f);Intent i=new Intent(Intent.ACTION_SEND);i.setPackage(pkg);i.setType(type);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);c.startActivity(i);}catch(Exception e){shareFile(c,f,type);}
    }
    private static File saveBitmap(Context c,Bitmap b,String name){File f=new File(c.getCacheDir(),name);try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);out.flush();}catch(Exception ignored){}return f;}
    public static void copyDb(Context c,File dest){try{File src=c.getDatabasePath("alazzi_grocery.db");try(FileInputStream in=new FileInputStream(src);FileOutputStream out=new FileOutputStream(dest)){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}}catch(Exception e){throw new RuntimeException(e);}}
    public static File backupFile(Context c){File f=new File(c.getCacheDir(),"alazzi_grocery_backup.db");copyDb(c,f);return f;}
    public static void printTest(Context c){Map<String,Object> d=new HashMap<>();d.put("business","بقالة العزي للمواد الغذائية");d.put("type","اختبار الطباعة");d.put("number","TEST");d.put("party","طباعة 58mm / 80mm");d.put("total",0);d.put("paid",0);d.put("remaining",0);printSavedOrChoose(c,receipt(c,d));}

    public static void printSavedOrChoose(Context c,Bitmap b){
        if(Build.VERSION.SDK_INT>=31 && c.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
            if(c instanceof android.app.Activity)((android.app.Activity)c).requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN},900);return;
        }
        BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter();
        if(a==null){new AlertDialog.Builder(c).setMessage("Bluetooth غير متاح").setPositiveButton("حسنًا",null).show();return;}
        Set<BluetoothDevice> dev=a.getBondedDevices();if(dev.isEmpty()){new AlertDialog.Builder(c).setMessage("لا توجد طابعة مقترنة").setPositiveButton("حسنًا",null).show();return;}
        android.database.Cursor pc=AppDatabase.get(c).getReadableDatabase().rawQuery("SELECT value FROM settings WHERE key='printer_address'",null);String pref;try{pref=pc.moveToFirst()?pc.getString(0):"";}finally{pc.close();}
        BluetoothDevice chosen=null;for(BluetoothDevice d:dev)if(d.getAddress().equals(pref))chosen=d;
        if(chosen!=null){doBt(c,chosen,b);return;}
        String[] names=new String[dev.size()];BluetoothDevice[] arr=dev.toArray(new BluetoothDevice[0]);for(int i=0;i<arr.length;i++)names[i]=(arr[i].getName()==null?"طابعة":arr[i].getName())+"\n"+arr[i].getAddress();
        new AlertDialog.Builder(c).setTitle("اختر الطابعة").setItems(names,(d,w)->{AppDatabase.get(c).getWritableDatabase().execSQL("UPDATE settings SET value=? WHERE key='printer_address'",new Object[]{arr[w].getAddress()});doBt(c,arr[w],b);}).show();
    }
    private static void doBt(Context c,BluetoothDevice d,Bitmap b){
        new Thread(()->{try{BluetoothSocket s=d.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));s.connect();OutputStream o=s.getOutputStream();o.write(new byte[]{0x1b,0x40});o.write(bitmapEscPos(b));o.write(new byte[]{0x0a,0x0a,0x0a});o.flush();o.close();s.close();((android.app.Activity)c).runOnUiThread(()->Toast.makeText(c,"تمت الطباعة ✓",Toast.LENGTH_SHORT).show());}catch(Exception e){((android.app.Activity)c).runOnUiThread(()->Toast.makeText(c,"تعذر الطباعة: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();
    }
    public static byte[] bitmapEscPos(Bitmap b){
        int widthBytes=(b.getWidth()+7)/8;ByteArrayOutputStream out=new ByteArrayOutputStream();
        out.write(0x1d);out.write(0x76);out.write(0x30);out.write(0x00);out.write(widthBytes&255);out.write((widthBytes>>8)&255);out.write(b.getHeight()&255);out.write((b.getHeight()>>8)&255);
        for(int y=0;y<b.getHeight();y++){for(int x=0;x<widthBytes*8;x+=8){int val=0;for(int bit=0;bit<8;bit++){int xx=x+bit;if(xx>=b.getWidth())continue;int px=b.getPixel(xx,y);int lum=(Color.red(px)*299+Color.green(px)*587+Color.blue(px)*114)/1000;if(lum<170)val|=1<<(7-bit);}out.write(val);}}return out.toByteArray();
    }

    public static File pdf(Context c,String title,String body){
        PdfDocument doc=new PdfDocument();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);p.setTextSize(14);p.setTextAlign(Paint.Align.RIGHT);
        String[] lines=body.split("\\n",-1);int per=48,index=0,pageNo=1;
        while(index<lines.length){PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo++).create());Canvas cn=page.getCanvas();float y=40;cn.drawText(title,555,y,p);y+=28;for(int k=0;k<per&&index<lines.length;k++,index++){cn.drawText(lines[index],555,y,p);y+=16;}doc.finishPage(page);}
        File f=new File(c.getCacheDir(),"document_"+System.currentTimeMillis()+".pdf");try(FileOutputStream o=new FileOutputStream(f)){doc.writeTo(o);}catch(Exception ignored){}doc.close();return f;
    }
    public static void sharePdf(Context c,String title,String body){shareFile(c,pdf(c,title,body),"application/pdf");}
}
