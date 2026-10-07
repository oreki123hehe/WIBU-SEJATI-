package com.wibusejati.app;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final String API="https://api.jikan.moe/v4";
    final ExecutorService exec=Executors.newFixedThreadPool(3);
    final int BG=Color.rgb(11,10,16), SUR=Color.rgb(23,21,31), SUR2=Color.rgb(33,30,44);
    final int TXT=Color.rgb(247,243,255), MUT=Color.rgb(170,164,184), ACC=Color.rgb(182,109,255);
    FrameLayout root; LinearLayout body;
    final LinkedHashSet<Integer> saved=new LinkedHashSet<>();
    final ArrayList<Integer> history=new ArrayList<>();
    android.content.SharedPreferences prefs;

    static class Anime {
        int id,episodes; String title,image,synopsis,year,type,status; double score;
    }
    static class Episode { int number; String title; }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("wibu",0); loadLocal();
        root=new FrameLayout(this); root.setBackgroundColor(BG); setContentView(root);
        showHome();
        handleDeepLink(getIntent());
    }
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleDeepLink(i);}
    void handleDeepLink(Intent i){
        Uri u=i==null?null:i.getData();
        if(u!=null && "wibusejati".equals(u.getScheme()) && "episode".equals(u.getHost())){
            int a=num(u.getQueryParameter("anime")), e=num(u.getQueryParameter("episode"));
            if(a>0&&e>0) showPlayer(a,e,null);
        }
    }
    int num(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}
    void loadLocal(){
        for(String s:prefs.getStringSet("saved",Collections.emptySet()))try{saved.add(Integer.parseInt(s));}catch(Exception ignored){}
        String h=prefs.getString("history","");
        if(!h.isEmpty())for(String s:h.split(","))try{history.add(Integer.parseInt(s));}catch(Exception ignored){}
    }
    void saveLocal(){
        HashSet<String> s=new HashSet<>(); for(int i:saved)s.add(""+i);
        prefs.edit().putStringSet("saved",s).putString("history",join(history)).apply();
    }
    String join(List<Integer> a){StringBuilder b=new StringBuilder();for(int i:a){if(b.length()>0)b.append(",");b.append(i);}return b.toString();}

    void shell(String title,int tab){
        root.removeAllViews();
        LinearLayout outer=new LinearLayout(this); outer.setOrientation(LinearLayout.VERTICAL); outer.setBackgroundColor(BG);
        ScrollView sv=new ScrollView(this); body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        sv.addView(body,new ScrollView.LayoutParams(-1,-1)); outer.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(8),dp(6),dp(8),dp(6)); nav.setBackgroundColor(SUR);
        String[] labels={"Home","Explore","Watchlist","History"};
        for(int i=0;i<4;i++){TextView t=tx(labels[i],12,i==tab?TXT:MUT);t.setGravity(17);t.setPadding(4,8,4,8);if(i==tab)t.setBackground(round(SUR2,18));
            final int x=i;t.setOnClickListener(v->{if(x==0)showHome();else if(x==1)showExplore();else if(x==2)showSaved();else showHistory();});
            nav.addView(t,new LinearLayout.LayoutParams(0,dp(54),1));
        }
        outer.addView(nav);root.addView(outer);
        if(title!=null){TextView h=tx(title,28,TXT);h.setTypeface(null,1);h.setPadding(dp(18),dp(18),dp(18),dp(8));body.addView(h);}
    }
    void showHome(){
        shell("Wibu Sejati",0); msg("Memuat katalog...");
        api("/top/anime?limit=12",j->{clearMessages();section("Popular sekarang");cards(animes(j));api("/seasons/now?limit=10",x->{section("Musim berjalan");cards(animes(x));},m->msg("Musim berjalan belum tersedia."));},m->{clearMessages();msg("Katalog gagal dimuat: "+m);action("Coba lagi",v->showHome());});
    }
    void showExplore(){
        shell("Explore",1);
        LinearLayout row=new LinearLayout(this);row.setPadding(dp(16),dp(4),dp(16),dp(12));
        EditText q=new EditText(this);q.setHint("Cari judul anime...");q.setHintTextColor(MUT);q.setTextColor(TXT);q.setSingleLine();
        q.setBackground(round(SUR2,16));q.setPadding(dp(14),0,dp(14),0);row.addView(q,new LinearLayout.LayoutParams(0,dp(52),1));
        Button b=button("CARI");LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(92),dp(52));bp.setMargins(dp(8),0,0,0);row.addView(b,bp);body.addView(row);
        b.setOnClickListener(v->{String s=q.getText().toString().trim();if(s.isEmpty())return;while(body.getChildCount()>1)body.removeViewAt(1);msg("Mencari...");api("/anime?q="+url(s)+"&limit=20&sfw=true",j->{clearMessages();section("Hasil: "+s);List<Anime>a=animes(j);cards(a);if(a.isEmpty())msg("Tidak ditemukan.");},m->{clearMessages();msg("Pencarian gagal: "+m);});});
    }
    void showSaved(){shell("Watchlist",2);if(saved.isEmpty()){msg("Watchlist masih kosong.");action("Jelajahi",v->showExplore());return;}for(int id:new ArrayList<>(saved))detailCard(id);}
    void showHistory(){shell("History",3);if(history.isEmpty()){msg("Belum ada riwayat.");return;}for(int id:new ArrayList<>(history))detailCard(id);}
    void detailCard(int id){api("/anime/"+id+"/full",j->{Anime a=parse(j);card(a);},m->{});}
    void cards(List<Anime>a){HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout r=new LinearLayout(this);r.setPadding(dp(16),0,dp(16),dp(8));for(Anime x:a)r.addView(mini(x),new LinearLayout.LayoutParams(dp(150),dp(250)));hs.addView(r);body.addView(hs);}
    View mini(Anime a){LinearLayout c=base();ImageView im=img(150,174);c.addView(im);image(a.image,im);TextView t=tx(trim(a.title,30),14,TXT);t.setTypeface(null,1);t.setPadding(dp(10),dp(8),dp(10),0);c.addView(t);TextView m=tx(a.year+" • "+(a.score>0?String.format(Locale.US,"%.1f",a.score):"—"),11,MUT);m.setPadding(dp(10),dp(4),dp(10),dp(8));c.addView(m);c.setOnClickListener(v->showDetail(a.id));return c;}
    void card(Anime a){LinearLayout c=base();c.setPadding(dp(12),dp(12),dp(12),dp(12));LinearLayout r=new LinearLayout(this);ImageView im=img(92,124);r.addView(im);image(a.image,im);LinearLayout inf=new LinearLayout(this);inf.setOrientation(LinearLayout.VERTICAL);inf.setPadding(dp(12),0,0,0);TextView t=tx(trim(a.title,42),16,TXT);t.setTypeface(null,1);inf.addView(t);inf.addView(tx(a.year+" • "+a.type,12,MUT));inf.addView(tx(a.status,12,MUT));r.addView(inf,new LinearLayout.LayoutParams(0,-2,1));c.addView(r);c.setOnClickListener(v->showDetail(a.id));body.addView(c,mlp(-1,145,16,16,8,0));}
    void showDetail(int id){
        shell(null,-1);action("← Kembali",v->showExplore());msg("Memuat detail...");
        api("/anime/"+id+"/full",j->{clearMessages();Anime a=parse(j);
            TextView title=tx(a.title,25,TXT);title.setTypeface(null,1);title.setPadding(dp(16),dp(14),dp(16),dp(4));body.addView(title);
            msg(a.year+" • "+a.type+" • "+a.status);
            LinearLayout acts=new LinearLayout(this);acts.setPadding(dp(16),dp(8),dp(16),dp(10));
            Button w=button(saved.contains(id)?"TERSIMPAN":"+ WATCHLIST");Button e=button("EPISODE");
            acts.addView(w,new LinearLayout.LayoutParams(0,dp(48),1));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(48),1);ep.setMargins(dp(8),0,0,0);acts.addView(e,ep);body.addView(acts);
            w.setOnClickListener(v->{if(saved.contains(id))saved.remove(id);else saved.add(id);saveLocal();w.setText(saved.contains(id)?"TERSIMPAN":"+ WATCHLIST");});
            TextView sy=tx(a.synopsis==null||a.synopsis.isEmpty()?"Sinopsis belum tersedia.":a.synopsis,13,MUT);sy.setPadding(dp(16),0,dp(16),dp(14));body.addView(sy);
            ImageView im=img(150,210);LinearLayout.LayoutParams ip=mlp(150,210,16,16,0,12);body.addView(im,ip);image(a.image,im);
            section("Daftar episode");episodes(id);
        },m->{clearMessages();msg("Detail gagal dimuat.");});
    }
    void episodes(int id){
        msg("Memuat episode...");
        final int marker=body.getChildCount()-1;
        api("/anime/"+id+"/episodes?page=1",j->{if(marker< body.getChildCount())body.removeViewAt(marker);JSONArray a=j.optJSONArray("data");if(a==null)return;
            for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;int n=o.optInt("mal_id");String tt=o.optString("title","Episode "+n);
                LinearLayout r=base();r.setOrientation(LinearLayout.HORIZONTAL);r.setPadding(dp(14),dp(10),dp(10),dp(10));LinearLayout txs=new LinearLayout(this);txs.setOrientation(LinearLayout.VERTICAL);txs.addView(tx("Episode "+n,15,TXT));txs.addView(tx(trim(tt,44),12,MUT));r.addView(txs,new LinearLayout.LayoutParams(0,-2,1));Button p=button("PUTAR");r.addView(p,new LinearLayout.LayoutParams(dp(82),dp(44)));p.setOnClickListener(v->showPlayer(id,n,null));r.setOnClickListener(v->showPlayer(id,n,null));body.addView(r,mlp(-1,66,16,16,6,0));}
        },m->{if(marker<body.getChildCount())body.removeViewAt(marker);msg("Episode belum tersedia.");});
    }
    void showPlayer(int id,int ep,String source){
        shell(null,-1);action("← Kembali",v->showDetail(id));TextView h=tx("Episode "+ep,23,TXT);h.setTypeface(null,1);h.setPadding(dp(16),dp(16),dp(16),dp(8));body.addView(h);
        history.remove((Integer)id);history.add(0,id);while(history.size()>30)history.remove(history.size()-1);saveLocal();
        if(source!=null&&!source.isEmpty()){WebView w=new WebView(this);w.setWebViewClient(new WebViewClient());w.getSettings().setJavaScriptEnabled(true);w.getSettings().setDomStorageEnabled(true);w.loadUrl(source);body.addView(w,new LinearLayout.LayoutParams(-1,dp(430)));}
        else{LinearLayout c=base();c.setPadding(dp(20),dp(24),dp(20),dp(24));TextView t=tx("Sumber tontonan belum terhubung",19,TXT);t.setTypeface(null,1);c.addView(t);c.addView(tx("Katalog dan episode sudah siap. Player menerima URL dari backend hanya quando sumber tersebut mengizinkan pemutaran/embedding.",13,MUT));body.addView(c,mlp(-1,190,16,16,14,0));}
    }

    void api(String path,java.util.function.Consumer<JSONObject>ok,java.util.function.Consumer<String>bad){
        exec.execute(()->{HttpURLConnection c=null;try{URL u=new URL(API+path);c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();String s=read(in);if(code<200||code>=300)throw new IOException("HTTP "+code);JSONObject j=new JSONObject(s);post(()->ok.accept(j));}catch(Exception e){post(()->bad.accept(e.getMessage()==null?"network error":e.getMessage()));}finally{if(c!=null)c.disconnect();}});
    }
    String read(InputStream in)throws Exception{if(in==null)return "";BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();String l;while((l=r.readLine())!=null)b.append(l);r.close();return b.toString();}
    String url(String s){try{return URLEncoder.encode(s,StandardCharsets.UTF_8.name());}catch(Exception e){return s;}}
    List<Anime> animes(JSONObject j){List<Anime>o=new ArrayList<>();JSONArray a=j.optJSONArray("data");if(a!=null)for(int i=0;i<a.length();i++)o.add(parse(a.optJSONObject(i)));return o;}
    Anime parse(JSONObject o){Anime a=new Anime();if(o==null)return a;a.id=o.optInt("mal_id");a.title=o.optString("title","Unknown");a.synopsis=o.optString("synopsis","");a.type=o.optString("type","");a.status=o.optString("status","");a.score=o.optDouble("score",0);a.episodes=o.optInt("episodes",0);String air=o.optString("aired","");a.year=air.length()>=4?air.substring(0,4):"—";JSONObject ii=o.optJSONObject("images");if(ii!=null){JSONObject jpg=ii.optJSONObject("jpg");if(jpg!=null)a.image=jpg.optString("large_image_url",jpg.optString("image_url",""));}return a;}
    void image(String u,ImageView v){if(u==null||u.isEmpty())return;exec.execute(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);Bitmap b=BitmapFactory.decodeStream(c.getInputStream());c.disconnect();post(()->{if(b!=null)v.setImageBitmap(b);});}catch(Exception ignored){}});}
    void post(Runnable r){new Handler(Looper.getMainLooper()).post(r);}
    void section(String s){TextView t=tx(s,19,TXT);t.setTypeface(null,1);t.setPadding(dp(16),dp(16),dp(16),dp(8));body.addView(t);}
    void msg(String s){TextView t=tx(s,13,MUT);t.setPadding(dp(16),dp(12),dp(16),dp(8));body.addView(t);}
    void clearMessages(){if(body==null)return;while(body.getChildCount()>0){View v=body.getChildAt(body.getChildCount()-1);if(v instanceof TextView && ((TextView)v).getCurrentTextColor()==MUT)body.removeViewAt(body.getChildCount()-1);else break;}}
    void action(String s,View.OnClickListener l){Button b=button(s);b.setOnClickListener(l);body.addView(b,mlp(-1,48,16,16,10,0));}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(TXT);b.setTextSize(12);b.setAllCaps(false);b.setBackground(round(ACC,16));return b;}
    LinearLayout base(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setBackground(round(SUR,18));return l;}
    ImageView img(int w,int h){ImageView i=new ImageView(this);i.setScaleType(ImageView.ScaleType.CENTER_CROP);i.setBackground(round(SUR2,14));return i;}
    TextView tx(String s,float z,int c){TextView t=new TextView(this);t.setText(s==null?"":s);t.setTextSize(z);t.setTextColor(c);return t;}
    GradientDrawable round(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    LinearLayout.LayoutParams mlp(int w,int h,int l,int r,int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    String trim(String s,int n){return s==null?"":s.length()<=n?s:s.substring(0,n-1)+"…";}
    @Override protected void onDestroy(){exec.shutdownNow();super.onDestroy();}
}
