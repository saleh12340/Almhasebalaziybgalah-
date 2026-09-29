package com.alazzi.grocery;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Ui {
    public static final int BLUE=Color.rgb(20,85,217), DARK=Color.rgb(11,45,115), GOLD=Color.rgb(212,155,22);
    public static final int BG=Color.rgb(246,248,252), TEXT=Color.rgb(23,32,51), MUTED=Color.rgb(102,112,133), WHITE=Color.WHITE;
    private Ui(){}

    public static int dp(View v,float n){return (int)(n*v.getResources().getDisplayMetrics().density+.5f);}
    public static TextView text(android.content.Context c,String s,float size,boolean bold,int color){
        TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        t.setTypeface(android.graphics.Typeface.DEFAULT,bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        t.setPadding(0,dp(t,4),0,dp(t,4));return t;
    }
    public static EditText edit(android.content.Context c,String hint,boolean numeric){
        EditText e=new EditText(c);e.setHint(hint);e.setTextSize(16);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setSingleLine(true);
        e.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);e.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);e.setPadding(dp(e,12),0,dp(e,12),0);e.setBackground(round(Color.WHITE,14,0xffd9dee8));
        if(numeric)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        else e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        e.setIncludeFontPadding(true);return e;
    }
    public static Button button(android.content.Context c,String label,View.OnClickListener l){
        Button b=new Button(c);b.setText(label);b.setTextSize(15);b.setTextColor(WHITE);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setOnClickListener(l);
        b.setBackground(round(BLUE,14,0));b.setPadding(dp(b,8),0,dp(b,8),0);return b;
    }
    public static View titleBar(android.content.Context c,String title,View.OnClickListener back,View.OnClickListener menu){
        LinearLayout bar=new LinearLayout(c);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(DARK);bar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);bar.setPadding(dp(bar,8),0,dp(bar,8),0);
        if(menu!=null){TextView m=text(c,"⋮",28,true,WHITE);m.setGravity(Gravity.CENTER);bar.addView(m,new LinearLayout.LayoutParams(dp(bar,45),-1));m.setOnClickListener(menu);}
        TextView tt=text(c,title,19,true,WHITE);tt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);bar.addView(tt,new LinearLayout.LayoutParams(0,-1,1));
        if(back!=null){TextView x=text(c,"‹",36,false,WHITE);x.setGravity(Gravity.CENTER);bar.addView(x,new LinearLayout.LayoutParams(dp(bar,45),-1));x.setOnClickListener(back);}
        return bar instanceof TextView ? (TextView)bar : titleViewHolder(bar);
    }
    private static TextView titleViewHolder(LinearLayout bar){
        TextView t=new TextView(bar.getContext());t.setVisibility(View.GONE);return t;
    }
    public static LinearLayout card(android.content.Context c){
        LinearLayout l=row(c);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(l,14),dp(l,10),dp(l,14),dp(l,10));l.setBackground(round(WHITE,16,0xffdfe4ec));return l;
    }
    public static LinearLayout row(android.content.Context c){LinearLayout l=new LinearLayout(c);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);l.setPadding(dp(l,6),dp(l,4),dp(l,6),dp(l,4));return l;}
    public static Space spacer(android.content.Context c,int dp){Space s=new Space(c);s.setLayoutParams(new LinearLayout.LayoutParams(1,(int)(dp*c.getResources().getDisplayMetrics().density)));return s;}
    public static GradientDrawable round(int fill,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(radius);if(stroke!=0)d.setStroke(1,stroke);return d;}
    public static String number(double n){if(Math.abs(n)<0.0000001)return "0";DecimalFormat f=new DecimalFormat("#,##0.###",DecimalFormatSymbols(Locale.US));return f.format(n);}
    public static double num(String s){try{return Double.parseDouble(s.replace(",","").trim());}catch(Exception e){return 0;}}
    public static String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date());}
}
