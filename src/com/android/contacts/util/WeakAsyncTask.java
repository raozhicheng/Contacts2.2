/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.util;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

//继承自AsyncTask 多添加一个参数  ，WeakTarget 用来引用调用这个函数的 activity
public abstract class WeakAsyncTask<Params, Progress, Result, WeakTarget> extends
        AsyncTask<Params, Progress, Result> {
	//界面的 activity的引用
    protected WeakReference<WeakTarget> mTarget;

    public WeakAsyncTask(WeakTarget target) {
        mTarget = new WeakReference<WeakTarget>(target);
    }

    /** {@inheritDoc} 
     * 实现 AsyncTask的函数 在 任务在准备函数
     * 其中屏蔽子类的回调，转变为回调onPreExecute(target) 将界面引用传入
     * */
    @Override
    protected final void onPreExecute() {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            this.onPreExecute(target);
        }
    }

    /** {@inheritDoc} 
     * 实现 AsyncTask的函数 在 任务处理函数
     * 其中屏蔽子类的回调，转变为回调doInBackground(target, params) 将界面引用传入
     * */
    @Override
    protected final Result doInBackground(Params... params) {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            return this.doInBackground(target, params);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} 
     * 实现 AsyncTask的函数 在 任务完成函数
     * 其中屏蔽子类的回调，转变为回调onPostExecute(target, result) 将界面引用传入 将结果传入
     * */
    @Override
    protected final void onPostExecute(Result result) {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            this.onPostExecute(target, result);
        }
    }

    protected void onPreExecute(WeakTarget target) {
        // No default action
    }

    protected abstract Result doInBackground(WeakTarget target, Params... params);

    protected void onPostExecute(WeakTarget target, Result result) {
        // No default action
    }
}
