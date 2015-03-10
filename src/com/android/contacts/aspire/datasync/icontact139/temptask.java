package com.android.contacts.aspire.datasync.icontact139;

import java.util.ArrayList;

import com.google.android.collect.Lists;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

public class temptask {
	
	public static void addContact(ContentResolver cr)
	{
		for(int i=0;i<5;i++)
		{
			
		
		
		try { 
            ArrayList<ContentProviderOperation> ops = Lists.newArrayList(); 
            int rawContactInsertIndex = ops.size(); 
            //     public static final String ACCOUNT_NAME = "account_name";
            //     public static final String ACCOUNT_TYPE = "account_type";
            //     public static final String SOURCE_ID = "sourceid";
            //     public static final String VERSION = "version";
            //     public static final String DIRTY = "dirty";
            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)                     
                    .withValue(RawContacts.ACCOUNT_TYPE, "com.139")
                    .withValue(RawContacts.ACCOUNT_NAME, "13714669692")
                    .withValue(RawContacts.SOURCE_ID, "139-1")
                    .withValue(RawContacts.VERSION, "10000")
                    .withValue(RawContacts.DIRTY, "1")
                    .build()); 
//         try {
//			new Object().wait(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
          
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI) 
                    .withValueBackReference(Data.RAW_CONTACT_ID, 
                            rawContactInsertIndex).withValue(Data.MIMETYPE, 
                            StructuredName.CONTENT_ITEM_TYPE).withValue( 
                            StructuredName.DISPLAY_NAME, "鏈眡"+i) 
                    .build()); 
            
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI) 
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex) 
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE) 
                    .withValue(Phone.NUMBER, "1398929734"+i) 
                    .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); 
            cr.applyBatch(ContactsContract.AUTHORITY, ops); 
        } catch (RemoteException e) { 
            // TODO Auto-generated catch block 
            e.printStackTrace(); 
        } catch (OperationApplicationException e) { 
            // TODO Auto-generated catch block 
            e.printStackTrace(); 
        } 
		}}
	

}
