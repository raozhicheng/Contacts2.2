package com.android.contacts.aspire.network;


public class HttpUtil

{

  public static void isLegalHttpUrl(String url)
  {
        int idx = url.indexOf("://");
        if(idx==-1)
        {
            throw new IllegalArgumentException(url+" is not an right http url,no '://'");
        }
        if(!url.startsWith("http"))
        {
             throw new IllegalArgumentException(url+" is not an right http url,no \"http\"");
        }


  }

}
 
