/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts;

import java.io.FileDescriptor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * A custom text editor that helps automatically dismiss the activity along with the soft
 * keyboard.
 */
public class SearchEditText extends EditText implements IBinder {

    private boolean mMagnifyingGlassShown = true;
    private Drawable mMagnifyingGlass;

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMagnifyingGlass = getCompoundDrawables()[2];
    }

    /**
     * Conditionally shows a magnifying glass icon on the right side of the text field
     * when the text it empty.
     */
    
    public boolean onPreDraw() {
        boolean emptyText = TextUtils.isEmpty(getText());
        if (mMagnifyingGlassShown != emptyText) {
            mMagnifyingGlassShown = emptyText;
            if (mMagnifyingGlassShown) {
                setCompoundDrawables(null, null, mMagnifyingGlass, null);
            } else {
                setCompoundDrawables(null, null, null, null);
            }
            return false;
        }
        return super.onPreDraw();
    }

    /**
     * Forwards the onKeyPreIme call to the view's activity.
     */
    
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    	if(getContext() instanceof ContactsListActivity){
		    if (((ContactsListActivity)getContext()).onKeyPreIme(keyCode, event)) {
		        return true;
		    }
    	}
        return super.onKeyPreIme(keyCode, event);
    }

	
	public void dump(FileDescriptor fd, String[] args) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	
	public String getInterfaceDescriptor() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean isBinderAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void linkToDeath(DeathRecipient recipient, int flags)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	
	public boolean pingBinder() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public IInterface queryLocalInterface(String descriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean transact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
