package cn.edu.hebtu.software.listendemo.Mine.index.settings;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISCameraConfig;
import com.yuyh.library.imgsel.config.ISListConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.edu.hebtu.software.listendemo.Entity.UnLock;
import cn.edu.hebtu.software.listendemo.Entity.User;
import cn.edu.hebtu.software.listendemo.QiniuUtils.Auth;
import cn.edu.hebtu.software.listendemo.R;
import cn.edu.hebtu.software.listendemo.Untils.Constant;
import cn.edu.hebtu.software.listendemo.Untils.StatusBarUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cn.edu.hebtu.software.listendemo.Untils.Constant.SP_NAME;
import static cn.edu.hebtu.software.listendemo.Untils.Constant.USER_KEEP_KEY;

//import static cn.edu.hebtu.software.listendemo.QiniuUtils.QiniuUtil.uploadImg2QiNiu;

public class EditMsgActivity extends AppCompatActivity implements View.OnClickListener {
    private String headpicPath = "";
    private ImageView ivExit;
    private ImageView ivHeader;
    private TextView tvSetHeader;
    private EditText etName;
    private Spinner spSex;
    private Spinner spGrade;
    private TextView tvBirth;
    private Spinner spYear;
    private List<UnLock> unLocks;
    private TextView tvChooseDate;
    private Button btnSave;
    private SharedPreferences sp;
    private OkHttpClient client = new OkHttpClient.Builder().build();
    private Gson gson = new Gson();
    private User user;
    private TextView tvPhoto;
    private TextView tvPic;
    private TextView tvCancel;
    private Dialog dialog;
    private DatePickerDialog dpd = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_FORM:
                    updateUser(user.getUid());
                    Toast.makeText(EditMsgActivity.this, "修改成功", Toast.LENGTH_SHORT).show();

                    break;
                case UPLOAD_HEADER:
                    String userStr = msg.obj + "";
                    sp.edit().putString(Constant.USER_KEEP_KEY, userStr).commit();
                    user = gson.fromJson(userStr,User.class);
                    //operatingAnim.cancel();
                    tvSetHeader.setText("上传成功");
                    refreshHeadImg();
                    //initData();
                    //setData();
                    break;
                case UPLOAD_ERROR:
                    tvSetHeader.setText("服务器繁忙...");

                    break;
                case UPLOAD_QINIU_TRUE:
                    //路径返回给服务器
                    Log.e("上传头像", msg.obj + "");
                    FormBody fb = new FormBody.Builder().add("fileUrl", msg.obj + "").add("uid", user.getUid() + "").build();
                    Request request = new Request.Builder()
                            .url(Constant.URL_HEAD_UPLOAD)
                            .post(fb)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Message message = new Message();
                            message.what = UPLOAD_ERROR;
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Message message = new Message();
                            message.obj = response.body().string();
                            Log.e("changHead", message.obj.toString());
                            message.what = UPLOAD_HEADER;
                            handler.sendMessage(message);
                        }
                    });
                    break;
            }

        }
    };
    private int year = 2016;
    private int month = 0;
    private int day = 1;
    private static final int REQUEAT_CODE = 100;
    private static final int REQUEST_CAMERA_CODE = 120;
    private static final int CAMERA_OK = 140;
    private static final int UPLOAD_HEADER = 200;
    private static final int UPLOAD_FORM = 400;
    private static final int UPLOAD_ERROR = 600;
    private static final int UPLOAD_QINIU_TRUE = 800;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_msg);

        findViews();
        initData();
        setData();
        setListener();
        StatusBarUtil.statusBarLightMode(this);
        StatusBarUtil.setStatusBarColor(this, R.color.white);
    }

    private void setListener() {
        ivHeader.setOnClickListener(this);
        ivExit.setOnClickListener(this);
        tvChooseDate.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    private void refreshHeadImg() {
        RequestOptions ro = new RequestOptions().circleCrop();
        if (null != user.getUheadPath() && !user.getUheadPath().equals(""))
            Glide.with(this).load(user.getUheadPath()).apply(ro).into(ivHeader);
        else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.head_user)).apply(ro).into(ivHeader);
        }
    }

    private void setData() {
        etName.setText(user.getUname());
        RequestOptions ro = new RequestOptions().circleCrop();
        if (null != user.getUheadPath() && !user.getUheadPath().equals(""))
            Glide.with(this).load(user.getUheadPath()).apply(ro).into(ivHeader);
        else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.head_user)).apply(ro).into(ivHeader);
        }
        if (null != user.getUsex()) {
            switch (user.getUsex()) {
                case "保密":
                    spSex.setSelection(0);
                    break;
                case "男":
                    spSex.setSelection(1);
                    break;
                case "女":
                    spSex.setSelection(2);
                    break;
                default:
                    spSex.setSelection(0);
                    break;
            }
        } else {
            spSex.setSelection(0);
        }
        spGrade.setSelection(user.getUgrade());
        if (null == user.getUbirth() || user.getUbirth().equals("")) {
            spYear.setSelection(0);
            month = 0;
            day = 1;
        } else {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(user.getUbirth());
                Calendar ca = Calendar.getInstance();
                ca.setTime(date);
                day = ca.get(Calendar.DAY_OF_MONTH);
                month = ca.get(Calendar.MONTH);
                year = ca.get(Calendar.YEAR);
                tvBirth.setText(year + "年" + (month + 1) + "月" + day + "日");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (year != (1 - spYear.getSelectedItemPosition())) {
                spYear.setSelection(2016 - year);
            } else spYear.setSelection(0);
        }
    }

    private void initData() {
        ISNav.getInstance().init(new ImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Glide.with(context).load(path).into(imageView);
            }
        });
        sp = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE);
        String userStr = sp.getString(Constant.USER_KEEP_KEY, Constant.DEFAULT_KEEP_USER);
        unLocks = null;
        try {
            JSONObject jsonObject = new JSONObject(userStr);
            String unLockList = jsonObject.get("unlockList").toString();
            Type type = new TypeToken<List<UnLock>>(){}.getType();
            unLocks = new Gson().fromJson(unLockList,type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        user = gson.fromJson(userStr, User.class);
        user.setUnLockList(unLocks);
        Log.e("getUesr1",gson.toJson(user));
    }

    private void findViews() {
        ivExit = findViewById(R.id.iv_edit_my_msg_exit);
        ivHeader = findViewById(R.id.iv_edit_my_msg_header);
        tvSetHeader = findViewById(R.id.tv_edit_my_msg_set_header);
        etName = findViewById(R.id.et_edit_my_msg_username);
        spSex = findViewById(R.id.sp_upd_my_msg_sex);
        spGrade = findViewById(R.id.spin_upd_my_msg_grade);
        tvBirth = findViewById(R.id.tv_upd_my_msg_birth_set);
        spYear = findViewById(R.id.spin_upd_my_msg_year);
        tvChooseDate = findViewById(R.id.tv_upd_my_msg_selectBirth);
        btnSave = findViewById(R.id.btn_upd_my_msg_save);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit_my_msg_exit:
                finish();
                break;
            case R.id.iv_edit_my_msg_header:
                showBottomDialog();
                break;
            case R.id.tv_upd_my_msg_selectBirth:
                Calendar now = Calendar.getInstance();
                dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month1, int dayOfMonth) {
                        String date = year + "年" + (month1 + 1) + "月" + dayOfMonth + "日";
                        if (year != (1 - spYear.getSelectedItemPosition())) {
                            int position = 2016 - year;
                            spYear.setSelection(position);
                        }
                        month = month1 + 1;
                        day = dayOfMonth;
                        tvBirth.setText(date);
                    }
                }, 2016 - spYear.getSelectedItemPosition(), month, day);
                dpd.show();
                break;
            case R.id.btn_upd_my_msg_save:
                updNewUserMsg();
                postForm();
                break;
            case R.id.tv_upd_choose_cancel:
                dialog.dismiss();
                break;
            case R.id.tv_upd_choose_take_photo:
                getPhoto();
                dialog.dismiss();
                break;
            case R.id.tv_upd_choose_take_pic:
                selectPhoto();
                dialog.dismiss();
                break;
        }
    }

    private void updNewUserMsg() {
        if (!tvBirth.getText().toString().isEmpty()) {
            try {
                Date date = new SimpleDateFormat("yyyy年MM月dd日").parse(tvBirth.getText().toString());
                Calendar ca = Calendar.getInstance();
                ca.setTime(date);
                int day = ca.get(Calendar.DAY_OF_MONTH);
                int month = ca.get(Calendar.MONTH);
                int year = ca.get(Calendar.YEAR);
                user.setUbirth(year + "-" + (month + 1) + "-" + day);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        user.setUname(etName.getText().toString());
        user.setUgrade(spGrade.getSelectedItemPosition());
        switch (spSex.getSelectedItemPosition()) {
            case 0:
                user.setUsex("保密");
                break;
            case 1:
                user.setUsex("男");
                break;
            case 2:
                user.setUsex("女");
                break;
        }
    }

    private void postForm() {
        Log.e("getUesr2",gson.toJson(user));
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), gson.toJson(user));
        Request request = new Request.Builder().url(Constant.URL_UPDATE_USER).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message message = new Message();
                message.what = UPLOAD_FORM;
                message.obj = response.body().string();
                handler.sendMessage(message);
            }
        });
    }

    private void selectPhoto() {
        ISListConfig config = new ISListConfig.Builder()
                // 是否多选, 默认true
                .multiSelect(false)
                // 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                .rememberSelected(true)
                // “确定”按钮背景色
                .btnBgColor(Color.GRAY)
                // “确定”按钮文字颜色
                .btnTextColor(Color.BLUE)
                // 使用沉浸式状态栏
                .statusBarColor(Color.parseColor("#3F51B5"))
                // 返回图标ResId
                .backResId(android.support.v7.appcompat.R.drawable.abc_action_bar_item_background_material)
                // 标题
                .title("选择图片")
                // 标题文字颜色
                .titleColor(Color.WHITE)
                // TitleBar背景色
                .titleBgColor(Color.parseColor("#3F51B5"))
                // 裁剪大小。needCrop为true的时候配置
                .cropSize(1, 1, 200, 200)
                .needCrop(true)
                // 第一个是否显示相机，默认true
                .needCamera(false)
                // 最大选择图片数量，默认9
                .maxNum(1)
                .build();
        // 跳转到图片选择器
        ISNav.getInstance().toListActivity(this, config, REQUEAT_CODE);
    }

    private void getPhoto() {
        if (ContextCompat.checkSelfPermission(EditMsgActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditMsgActivity.this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);
        } else {
            ISCameraConfig config1 = new ISCameraConfig.Builder()
                    .needCrop(true) // 裁剪
                    .cropSize(1, 1, 200, 200)
                    .build();
            ISNav.getInstance().toCameraActivity(this, config1, REQUEST_CAMERA_CODE);
        }
    }

    private void showBottomDialog() {
        //1、使用Dialog、设置style
        dialog = new Dialog(this, R.style.DialogTheme);
        //2、设置布局
        View view = View.inflate(this, R.layout.custom_dialog_choose_img, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        tvCancel = dialog.findViewById(R.id.tv_upd_choose_cancel);
        tvPhoto = dialog.findViewById(R.id.tv_upd_choose_take_photo);
        tvPic = dialog.findViewById(R.id.tv_upd_choose_take_pic);

        tvCancel.setOnClickListener(this);
        tvPhoto.setOnClickListener(this);
        tvPic.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_OK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //这里已经获取到了摄像头的权限，想干嘛干嘛了可以
                ISCameraConfig config1 = new ISCameraConfig.Builder()
                        .needCrop(true) // 裁剪
                        .cropSize(1, 1, 200, 200)
                        .build();
                ISNav.getInstance().toCameraActivity(this, config1, REQUEST_CAMERA_CODE);
            } else {
                //这里是拒绝给APP摄像头权限，给个提示什么的说明一下都可以。
                Toast.makeText(EditMsgActivity.this, "请手动打开相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            String path = data.getStringExtra("result"); // 图片地址
            RequestOptions ro = new RequestOptions().circleCrop();
            Glide.with(this).load(path).apply(ro).into(ivHeader);
            uploadImg(path);
        } else if (requestCode == REQUEAT_CODE) {
            if (data != null) {
                ArrayList<String> mSelectPath = data.getStringArrayListExtra("result");
                RequestOptions ro = new RequestOptions().circleCrop();
                Glide.with(this).load(mSelectPath.get(0)).apply(ro).into(ivHeader);
                uploadImg(mSelectPath.get(0));

            }
        }
    }


    public synchronized String uploadImg2QiNiu(String picPath) {
        UploadManager uploadManager = new UploadManager();
        // 设置图片名字
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String key = "icon_" + sdf.format(new Date());
        uploadManager.put(picPath, key, Auth.create(Constant.ACCESSKEY, Constant.SECRETKEY).uploadToken(Constant.BUCKET), new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject res) {
                // info.error中包含了错误信息，可打印调试
                // 上传成功后将key值上传到自己的服务器
                if (info.isOK()) {
                    Log.e("token===", Auth.create(Constant.ACCESSKEY, Constant.SECRETKEY).uploadToken(Constant.BUCKET));
                    headpicPath = "http://cdn.zin4ever.top/" + key;
                    Log.e(" headpicPath", headpicPath);
                    Message message = new Message();
                    message.what = UPLOAD_QINIU_TRUE;
                    message.obj = headpicPath;
                    headpicPath = "";
                    handler.sendMessage(message);
                } else {
                    Log.e("error", info.error);
                }
            }
        }, null);
        return headpicPath;
    }

    private void uploadImg(String path) {
        Log.e("path", path);
        tvSetHeader.setText("正在上传...");
        //上传到七牛
        this.headpicPath = uploadImg2QiNiu(path);
    }
    //发送自动登录信息
    private void updateUser(int userId) {
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody fb = new FormBody.Builder().add("id", userId + "").build();
        Request request = new Request.Builder().url(Constant.URL_UPDATE_MYSELF).post(fb).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            /**
             * 未完待续
             * @param call
             * @param response
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                List<UnLock> unLocks = null;
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String unLockList = jsonObject.get("unlockList").toString();
                    Type type = new TypeToken<List<UnLock>>(){}.getType();
                    unLocks = new Gson().fromJson(unLockList,type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                User user = new Gson().fromJson(json,User.class);
                user.setUnLockList(unLocks);
                getSharedPreferences(SP_NAME,MODE_PRIVATE).edit().putString(USER_KEEP_KEY,json).commit();
                finish();
            }
        });
    }
}
