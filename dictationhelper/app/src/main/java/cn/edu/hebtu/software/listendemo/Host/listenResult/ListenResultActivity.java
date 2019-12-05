package cn.edu.hebtu.software.listendemo.Host.listenResult;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.hebtu.software.listendemo.Entity.User;
import cn.edu.hebtu.software.listendemo.Entity.Word;
import cn.edu.hebtu.software.listendemo.Host.index.ListenIndexActivity;
import cn.edu.hebtu.software.listendemo.R;
import cn.edu.hebtu.software.listendemo.Untils.Constant;
import cn.edu.hebtu.software.listendemo.Untils.WrongWordDBHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListenResultActivity extends AppCompatActivity {
    private ListenResultRecyclerViewAdapter listenResultSuccessRecyclerViewAdapter;
    private ListenResultRecyclerViewAdapter listenResultMineRecyclerViewAdapter;
    private RecyclerView recyclerViewListenSuccess;
    private RecyclerView recyclerViewListenMine;
    private List<Word>  successList;
    private List<Word>  mineList;
    private TextView tvReturnHost;
    private  TextView tvReturnBookDetail;
    private SQLiteDatabase database;
    private  float sum;
    private int error=0;
    private  double score;
    public Date date;
    public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_result);
        WrongWordDBHelper wrongWordDBHelper =new WrongWordDBHelper(this,"tbl_wrongWord.db",1);
        database =wrongWordDBHelper.getWritableDatabase();
        initData();
        initView();
    }

    private void initView(){
        //返回到主页
        tvReturnHost=findViewById(R.id.tv_returnHost);
        tvReturnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ListenResultActivity.this, ListenIndexActivity.class);
                startActivity(intent);
            }
        });
        //再听写一次
        tvReturnBookDetail=findViewById(R.id.tv_returnBookDetail);
        tvReturnBookDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerViewListenSuccess=findViewById(R.id.rv_success);
        recyclerViewListenMine=findViewById(R.id.rv_mine);
        sum=successList.size();
        error=0;
        for(int i=0;i<successList.size();i++){
            successList.get(i).setIsTrue(Constant.SPELL_TRUE);
            if(mineList.get(i).getWenglish().equals(successList.get(i).getWenglish())){
                mineList.get(i).setIsTrue(Constant.SPELL_TRUE);
            }else{
                Word w=successList.get(i);
                Cursor cursor =database .query("TBL_WRONGWORD", null, "WENGLISH=?", new String[]{w.getWenglish()}, null, null, null);
                Log.e("num",cursor.getCount()+"");
                if(cursor.getCount()==0) {
                    //添加错词
                    ContentValues word = new ContentValues();
                    word.put("WENGLISH", w.getWenglish());
                    word.put("WCHINESE", w.getWchinese());
                    word.put("WIMGPATH", w.getWimgPath());
                    word.put("UNID", w.getUnid());
                    word.put("BID", w.getBid());
                    word.put("TYPE", w.getType());
                    word.put("ISTRUE", w.getIsTrue());
                    date = new Date(System.currentTimeMillis());
                    word.put("ADDTIME", simpleDateFormat.format(date));
                    long row = database.insert("TBL_WRONGWORD", null, word);
                    Log.e("插入错词的行号", row + "");
                }
                mineList.get(i).setIsTrue(Constant.SPELL_FALSE);
                error++;
            }
        }
        score=(sum-error)*(1.0)/sum * 100;
        //传递测试数据
        //sendScore();
        final LeanTextView mText = findViewById(R.id.lean);
        mText.setText(Html.fromHtml("<u>"+(int)score+"</u>"));
        mText.setmDegrees(20);
        listenResultSuccessRecyclerViewAdapter=new ListenResultRecyclerViewAdapter(this,successList,R.layout.activity_grade_version_recycler_item);
        listenResultMineRecyclerViewAdapter=new ListenResultRecyclerViewAdapter(this,mineList,R.layout.activity_grade_version_recycler_item);

        //设置适配器
        recyclerViewListenSuccess.setAdapter(listenResultSuccessRecyclerViewAdapter);
        recyclerViewListenMine.setAdapter(listenResultMineRecyclerViewAdapter);
        //必须调用，设置布局管理器
        recyclerViewListenSuccess.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewListenMine.setLayoutManager(new GridLayoutManager(this, 2));


    }
    private void sendScore() {
        SharedPreferences sp = getSharedPreferences(Constant.SP_NAME,MODE_PRIVATE);
        User user = new Gson().fromJson(sp.getString(Constant.USER_KEEP_KEY,Constant.DEFAULT_KEEP_USER),User.class);
        OkHttpClient okHttpClient=new OkHttpClient();
        FormBody fb = new FormBody.Builder().add("sum",sum+"").add("error",error+"").add("score",score+"").add("date",simpleDateFormat.format(date)).add("uid",user.getUid()+"").build();
        Request request = new Request.Builder().url(Constant.URL_BOOKS_FIND_ALL).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonBooks = response.body().string();
               /* Message message = new Message();
                message.what = GET_BOOKS;
                Type type = new TypeToken<List<Book>>() {
                }.getType();
                res = gson.fromJson(jsonBooks, type);
                handler.sendMessage(message);*/
            }
        });
    }

    private void initData() {
        Intent intent=getIntent();
        String str=intent.getStringExtra("success");
        String str1=intent.getStringExtra("mine");
        Type listType=new TypeToken< List<Word> >(){}.getType();
        if(str!=null && !str.equals("")){
            successList=new Gson().fromJson(str,listType);
        }
        if(str1!=null && !str1.equals("")){
            mineList=new Gson().fromJson(str1,listType);
        }
    }
}