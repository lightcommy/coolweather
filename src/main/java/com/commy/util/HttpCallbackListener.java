package com.commy.util;

/**
 * Created by Commy on 2016/4/20.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);

}
