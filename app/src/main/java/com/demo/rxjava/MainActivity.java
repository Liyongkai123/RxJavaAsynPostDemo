package com.demo.rxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.tv_hello);

        findViewById(R.id.btn_post).setOnClickListener(this);
        findViewById(R.id.btn_rxjava_post).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_post:

                post(1);

                break;

            case R.id.btn_rxjava_post:

                rxJavaPost(1);

                break;
        }
    }

    /**
     * 普通网络请求
     *
     * @param userid
     */
    private void post(int userid) {
        UserHttp http = new UserHttp();
        http.post("http://server.com/user", userid, new CallBack<String>() {
            @Override
            public void onSuccess(String result) { // 主线程 回调
                User user = new UserDAO().parse(result);

                textView.setText(user.getName());
            }

            @Override
            public void onFailure(String msg) {}
        });
    }

    private void rxJavaPost(final int userid) {
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userid));// 请求参数

        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                logThread("create call(Subscriber)");

                UserHttp client   = new UserHttp();
                Response response = client.post("http://kkmike999.com/user", userid);// 同步请求

                if (response.getStatus() == 200) {
                    // 请求成功

                    subscriber.onNext(response.getResult());
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Throwable("请求失败"));
                }
            }
        }).subscribeOn(Schedulers.newThread());// 切换线程Thread1


        observable
                .map(new Func1<String, JSONObject>() {
                    @Override
                    public JSONObject call(String result) {// Thread1回调
                        logThread("map call(String)");

                        try {
                            return new JSONObject(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return new JSONObject();
                    }
                })
                .observeOn(Schedulers.newThread()) // 切换线程Thread2
                .map(new Func1<JSONObject, User>() {
                    @Override
                    public User call(JSONObject json) {// Thread2回调
                        logThread("map call(JSONObject)");

                        User user = new UserDAO().parse(json);

                        return user;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())// 切换回mainThread
                .subscribe(new Subscriber<User>() {

                    @Override
                    public void onNext(User user) {// mainThread回调
                        logThread("onNext(User)");

                        textView.setText(user.getName());
                    }

                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}
                })
        ;
    }

    /***
     * 输出线程
     *
     * @param method
     */
    private void logThread(String method) {
        Log.i(method, Thread.currentThread().toString());
    }
}
