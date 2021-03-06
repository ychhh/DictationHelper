package cn.edu.hebtu.software.listendemo.Host.listenWord;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.hebtu.software.listendemo.Entity.Word;
import cn.edu.hebtu.software.listendemo.Entity.WrongWord;
import cn.edu.hebtu.software.listendemo.Host.listenResult.ListenResultActivity;
import cn.edu.hebtu.software.listendemo.R;
import cn.edu.hebtu.software.listendemo.Untils.Constant;
import cn.edu.hebtu.software.listendemo.Untils.ReadManager;
import cn.edu.hebtu.software.listendemo.Untils.SmoothScrollLayoutManager;
import cn.edu.hebtu.software.listendemo.Untils.StatusBarUtil;

public class ListenWordActivity extends AppCompatActivity {

    private ListenWordRecyclerViewAdapter listenWordRecyclerViewAdapter;
    private RecyclerView recyclerViewListenWord;
    private List<Word>  listenWordlist = new ArrayList<>();
    private List<Word> mineWordlist=new ArrayList<>();
    private List<WrongWord>  errorWordlist;
    private int i=0;
    private PopupWindow popupWindow=null;
    private View popupView=null;
    private ReadManager readManager = new ReadManager(ListenWordActivity.this,"");
    private KeyboardView keyboard;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_word);
        setTitle("听写单词");
        initData();
        initView();

        StatusBarUtil.statusBarLightMode(this);


    }



    private void initView(){
        keyboard = (KeyboardView) findViewById(R.id.kv_keyboard);
        recyclerViewListenWord=findViewById(R.id.rv_listenword);
        listenWordRecyclerViewAdapter=new ListenWordRecyclerViewAdapter(this,listenWordlist,R.layout.activity_listenword_recycler_item,keyboard);
        recyclerViewListenWord.setAdapter(listenWordRecyclerViewAdapter);

        SmoothScrollLayoutManager layoutManager = new SmoothScrollLayoutManager(ListenWordActivity.this){

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewListenWord.setLayoutManager(layoutManager);
        recyclerViewListenWord.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*
                new state 有三种状态
                SCROLL_STATE_IDLE = 0 静止没有滚动
                SCROLL_STATE_DRAGGING = 1  用户正在拖拽
                SCROLL_STATE_SETTLING = 2 自动滚动
                 */

//                if(newState == 0){
//                    if(lastPage != layoutManager.findFirstVisibleItemPosition()){
//                        readManager.pronounce(listenWordlist.get(lastPage+1).getWenglish());
//
//                    }
//                    Log.e("findFirst",""+layoutManager.findFirstVisibleItemPosition());
//                    lastPage = layoutManager.findFirstVisibleItemPosition();
//                }
//


            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                /*
                    recyclerView : 当前滚动的view
                    dx : 水平滚动距离
                    dy : 垂直滚动距离

                    dx > 0 时为手指向左滚动,列表滚动显示右面的内容
                    dx < 0 时为手指向右滚动,列表滚动显示左面的内容
                    dy > 0 时为手指向上滚动,列表滚动显示下面的内容
                    dy < 0 时为手指向下滚动,列表滚动显示上面的内容

                 */

            }
        });


        final Button btnNext=findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("firstVisible",""+layoutManager.findFirstCompletelyVisibleItemPosition());
                int positionToSave = layoutManager.findFirstVisibleItemPosition();
//                layoutManager.smoothScrollToPosition(recyclerViewListenWord,new RecyclerView.State(),positionToSave+1);
                Log.e("positionToSave",positionToSave+"");
                layoutManager.scrollToPosition(positionToSave+1);
                View view = recyclerViewListenWord.getChildAt(0);
                EditText editText = findViewById(R.id.et_word);
                mineWordlist.get(positionToSave).setWenglish(editText.getText().toString());
                Log.e("word",""+editText.getText().toString());
                if(positionToSave == listenWordlist.size()-2){
                    btnNext.setText("交卷");
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                readManager.pronounce(listenWordlist.get(positionToSave+1).getWenglish());
                                Log.e("text","pronounce");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }else if(positionToSave == listenWordlist.size()-1){
                    //showPopupView(v);
                    CustomDialogListenWord dialog=new CustomDialogListenWord(listenWordlist,mineWordlist,ListenWordActivity.this);
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(),"listen");
                }else{
                    //延迟播放
                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    readManager.pronounce(listenWordlist.get(positionToSave+1).getWenglish());
                                    Log.e("text","pronounce");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                }
//                RecyclerView.LayoutManager layoutManager = recyclerViewListenWord.getLayoutManager();
//                if (layoutManager instanceof LinearLayoutManager) {
//                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//                    //获取第一个可见view的位置
//                    final int firstItemPosition = linearManager.findFirstVisibleItemPosition();
//
//                    int postion=firstItemPosition+1;
//                    if(firstItemPosition<listenWordlist.size()){
//                        EditText editText=findViewById(R.id.et_word);
//                        Word word=new Word();
//                        String str=editText.getText().toString();
//                        word.setWenglish(str);
//                        mineWordlist.add(word);
//                        recyclerViewListenWord.scrollToPosition(postion);
//
//                        //延迟播放
//                        new Thread(){
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(1000);
//                                    readManager.pronounce(listenWordlist.get(firstItemPosition+1).getWenglish());
//                                    Log.e("text","pronounce");
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
//                    }
//                    if(postion==listenWordlist.size()-1){
//                        btnNext.setText("交卷");
//                    }
//                    if(firstItemPosition==listenWordlist.size()-1){
//                        showPopupView(v);
//                    }
//                }
            }
        });
        readManager.pronounce(listenWordlist.get(0).getWenglish());

    }


    private void initData() {
        Intent intent=getIntent();
        String str=intent.getStringExtra(Constant.DETAIL_CON_RECITE_OR_DICTATION);
        if(str!=null && !str.equals("")){
            Log.e("tt",str);
            Type listType=new TypeToken< List<Word> >(){}.getType();
            listenWordlist=new Gson().fromJson(str,listType);
        }
        String str1=intent.getStringExtra(Constant.RECITE_CON_DICTATION);
        if(str1!=null && !str1.equals("")){
            Type listType=new TypeToken<List<Word>>(){}.getType();
            Log.e("tt",str1);
            listenWordlist=new Gson().fromJson(str1,listType);
        }
        String str2=intent.getStringExtra(Constant.NEWWORD_CON_LEARNWORD_DICTATION);
        if(str2!=null){
            Type listType=new TypeToken< List<Word> >(){}.getType();
            listenWordlist=new Gson().fromJson(str2,listType);
            str1=null;
        }

        String str3=intent.getStringExtra(Constant.WRONGWORD_CON_LEARNWORD_DICTATION);
        if(str3!=null){
            Type listType=new TypeToken< List<WrongWord> >(){}.getType();
            errorWordlist=new Gson().fromJson(str3,listType);
            listenWordlist=new ArrayList<>();
            for(WrongWord w:errorWordlist){
                Word word=new Word();
                word.setWenglish(w.getWenglish());
                word.setWchinese(w.getWchinese());
                word.setWimgPath(w.getWimgPath());
                word.setBid(w.getBid());
                word.setIsTrue(w.getIsTrue());
                word.setType(w.getType());
                word.setUnid(w.getUnid());
                listenWordlist.add(word);
            }
            str2=null;
        }
        if(listenWordlist != null && !listenWordlist.isEmpty()){
            for(Word word : listenWordlist){
                Word w = new Word();
                mineWordlist.add(w);
            }
        }
    }

    private void showPopupView(View v) {
        popupView= LayoutInflater.from(this).inflate(R.layout.custom_dialog_finishlisten,null);
        popupWindow=new PopupWindow(popupView, dip2px(this,300) , dip2px(this,200), true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        Button btnOk=popupView.findViewById(R.id.btn_OK_listen);
        Button btnCancel=popupView.findViewById(R.id.btn_cancel_listen);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(ListenWordActivity.this,ListenResultActivity.class);
                intent.putExtra("success",new Gson().toJson(listenWordlist));
                intent.putExtra("mine",new Gson().toJson(mineWordlist));
                startActivity(intent);
                popupWindow.dismiss();
                finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(v, Gravity.CENTER,0,20);
    }

    //  将物理像素装换成真实像素
    private int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void showExitDialog(){
        // 点击取消绑定
        // 显示一个Dialog
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle("确定退出");
        adBuilder.setMessage("退出将不会记录本次听写");
        adBuilder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 选中“确定”按钮，解除绑定
                // 更改SharedP中数据
                finish();
                // 修改显示样式
            }
        });
        adBuilder.setNegativeButton("我手滑了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 选中“取消”按钮，取消界面

            }
        });
        adBuilder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
            adBuilder.setTitle("是否退出本次听写");
            adBuilder.setMessage("退出后本次听写将不做记录");
            adBuilder.setPositiveButton("确认退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 选中“确定”按钮，解除绑定
                    // 更改SharedP中数据
                    finish();
                }
            });
            adBuilder.setNegativeButton("我手滑了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 选中“取消”按钮，取消界面

                }
            });
            adBuilder.create().show();
        }
        return false;
    }
}

