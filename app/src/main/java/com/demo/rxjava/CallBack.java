package com.demo.rxjava;

/**
 * Created by kkmike999 on 16/2/9.
 *
 * Http回调
 */
public interface CallBack<T> {

    void onSuccess(T result);

    void onFailure(String msg);
}
