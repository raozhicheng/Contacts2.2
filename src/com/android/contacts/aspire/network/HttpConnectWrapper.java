package com.android.contacts.aspire.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.util.StringAndInputStreamUtil;
import com.android.contacts.aspire.msg.request.GetCountInfoRequestMsg;
import com.android.contacts.aspire.msg.request.GetInfoByContactIdRequestMsg;
import com.android.contacts.aspire.msg.request.IRequestMsg;
import com.android.contacts.aspire.msg.request.ListFriendsRequestMsg;
import com.android.contacts.aspire.msg.request.LoginAuthMsgReq;
import com.android.contacts.aspire.msg.respone.IResponseMsg;
import com.android.contacts.aspire.msg.respone.LoginAuthMsgResp;

import android.util.Log;

public  class HttpConnectWrapper 
{
    //cmwap网关地址 
	protected static String cmwapip="http://10.0.0.172:80/";
    //连接
	private  HttpURLConnection httpConn = null;
    //输入流
	private InputStream is = null;

    /**
     * 发起一个http post请求，返回结果InputStream
     * @param url String 请求的url
     * @param contentType String  httprequest头中的ContentType
     * @param postdata byte[]  post的数据
     * @param isCMWap boolean  是否使用CMWAP 连接
     * @param requestProperty HashMap 在 setRequestProperty 中设置的属性和值
     * @return InputStream
     * @throws IOException
     */
      public InputStream httpPost(String url,String contentType,byte[] postdata,boolean isCMWap,Hashtable requestProperty) throws IOException
      {
    	 InputStream is = null;
    	 httpConn = openConnectionPost(url,contentType,isCMWap,requestProperty,postdata);
         HttpUtil.isLegalHttpUrl(url);
         try
         {
        	 is = getHttpContent(httpConn);
         }
         catch (Exception e) 
         {
			// TODO: handle exception
             try
             {
                 if (httpConn != null)
                 {
                	 	httpConn.disconnect();
                 }
             }
             catch (Exception ex){}
             finally
             {
                  httpConn=null;
             }
		}
         
        return is;
      }
      
      public InputStream getHttpContent(HttpURLConnection httpConn) throws IOException
       {
           try
           {
               int code = httpConn.getResponseCode();
               if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_MOVED_TEMP)
               {
                   throw new IOException("http code "+code);
               }

               int contentLength = (int) httpConn.getContentLength();
//             httpConn.setReadTimeout(10);
               if (contentLength == -1)
               {
            	   
               }

               is = httpConn.getInputStream();
           }
           catch (IOException e)
           {
        	   Log.e("http", "in  catch(IOException ex) of byte[] httpReadHelp");
               try
               {
                   if (is != null) is.close();
                   is = null;
               }
               catch (Exception ex)
               {}
               throw e;
           }
           
           return is;
    }
      
    public HttpURLConnection openConnectionPost(String url,String contentType, boolean isCMWap,
                                                    Hashtable requestProperty,byte[] postdata) throws
                                                    IOException
      {
    	System.out.println("POST url = " + url);
    	System.out.println("isCMWap = " + isCMWap);
    	HttpURLConnection httpConn = null;
        OutputStream dos = null;
        HttpUtil.isLegalHttpUrl(url);
        URL urlObj = null;
          try
          {         
              
        	  //如果是CMWAP 使用代理
              if (isCMWap)
              {
            	  HttpURLParam urlparam = new HttpURLParam(url);
            	  String CMWAPurl = cmwapip + urlparam.getPath();
//            	  System.out.println("CMWAP url = " + CMWAPurl);
            	  urlObj = new URL(CMWAPurl);
            	  httpConn = (HttpURLConnection) urlObj.openConnection();
                  httpConn.setRequestProperty("X-Online-Host",urlparam.getHost() + ":" +urlparam.getPort());
                  urlparam = null;
              }
              else
              {
            	  urlObj = new URL(url);
                  httpConn = (HttpURLConnection) urlObj.openConnection();
              }
              httpConn.setDoOutput(true);
              httpConn.setRequestMethod("POST");
              
              httpConn.setConnectTimeout(1000 * 15);
              httpConn.setReadTimeout(1000 * 30);
              
              if(requestProperty!=null)
              {
                Enumeration perpertyKeys = requestProperty.keys();
                while (perpertyKeys.hasMoreElements())
                {
                  String key = (String) perpertyKeys.nextElement();
                  String value = (String) requestProperty.get(key);
                  httpConn.setRequestProperty(key, value);
                }
              }
              httpConn.setRequestProperty("Content-Type", contentType);
              httpConn.setRequestProperty("contentlength", Integer.toString(postdata.length));
              dos = httpConn.getOutputStream();              
              dos.write(postdata);
//              if (postdata.length > 1024) {
//            	  dos.flush();
//              }
              return httpConn;
          }
          finally
          {        	 
              try
              {
                  if (dos != null)
                  {
                      dos.close();
                  }
              }
              catch (Exception ex){}
              finally
              {
                  dos = null;
              }

          }
    }
    
    public void releaseConnect()
    {
        try
        {
        	if (is != null)
        	{
        		is.close();
        	}
            if (httpConn != null)
            {
           	 	httpConn.disconnect();
            }
        }
        catch (Exception ex){}
        finally
        {
        	 is = null;
             httpConn=null;
        }
    }
    
    /**
       * 发起一个http get请求，返回结果byte[]
       * 
       * @param url
       *            String http请求的完整url
       * @param isCMWap
       *            boolean 是否使用CMWAP接入点，false表示不用，也就是使用cmnet
       * @param requestProperty
       *            在 setRequestProperty 中设置的属性和值
       * @return byte[]
       */
   public InputStream  httpGet(String url,boolean isCMWap,Hashtable requestProperty) throws IOException
   {
       HttpURLConnection httpConn = openConnectionGet(url,isCMWap,requestProperty);
       // #if "${wtk.debug}" == "true"
       HttpUtil.isLegalHttpUrl(url);
       // #endif
       try
       {
           return getHttpContent(httpConn);
       }
       finally
       {
           try
           {
               if (httpConn != null)
               {
                   httpConn.disconnect();
               }
           }
           catch (Exception ex){}
           finally
           {
                httpConn=null;
           }
       }
   }
   
   
   public static HttpURLConnection openConnectionGet(String url, boolean isCMWap,
           Hashtable requestProperty) throws
        IOException
        {
            HttpURLConnection httpConn = null;
            //#if "${wtk.debug}" == "true"
            HttpUtil. isLegalHttpUrl(url);
            //#endif
            
            URL urlObj = null;
            HttpURLParam urlparam = new HttpURLParam(url);
            //如果是CMWAP
            if (isCMWap)
            {
                String CMWAPurl = cmwapip + urlparam.getPath();
                urlObj = new URL(CMWAPurl);
                httpConn = (HttpURLConnection) urlObj.openConnection();
                httpConn.setRequestProperty("X-Online-Host",urlparam.getHost() + ":" +urlparam.getPort());
            }
            else
            {
                urlObj = new URL(url);
                httpConn = (HttpURLConnection) urlObj.openConnection();
            }
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("Get");

            
            if (requestProperty != null)
            {
                Enumeration perpertyKeys = requestProperty.keys();
                while (perpertyKeys.hasMoreElements())
                {
                    String key = (String) perpertyKeys.nextElement();
                    String value = (String) requestProperty.get(key);
                    httpConn.setRequestProperty(key, value);
                }
            }
            httpConn.setRequestProperty("Connection", "close");
            return httpConn;
        }
//    
/////**
////* 打印二进制形式的日志
////* 
////* @param logBytes
////*            二进制日志数据
////*/
////	public static final String bytesToHexString(byte[] bArray) {
////		StringBuffer sb = new StringBuffer(bArray.length);
////		String sTemp;
////
////		for (int i = 0; i < bArray.length; i++) {
////			if (i == 0) {
////				sb.append("[");
////			}
////			sTemp = Integer.toHexString(0xFF & bArray[i]);
////			if (sTemp.length() < 2) {
////				sb.append(0);
////			}
////
////			sb.append(sTemp.toUpperCase());
////			sb.append(" ");
////			if (i == MessageHeader.HEADER_LEN - 1) {
////				sb.append("]");
////			}
////		}
////
////		return sb.toString();
////	}
//	
   
	/*
	 * 由于cmlan连接方式只是短暂的需要,若切换到CMLAN时,功能完成后,必会切回其他的apn
	 */
	public boolean checkNetWorkType(){
		String proxyHost =  android.net.Proxy.getDefaultHost();
		if(proxyHost != null)
		{
		  return true;
		}
		else{
		 return false;
		}
	}
	
	
	
	public static int send(IRequestMsg req,IResponseMsg resp)
	{
		if(Config.IS_DEBUG)
		{
			return  sendDebug(req,resp);
		}
		else
		{
			return  sendRun(req,resp);
		}
	}
	
	
	//用户登录 参数 :手机号码
	private static int sendRun(IRequestMsg req,IResponseMsg resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;		
		HttpConnectWrapper http = new HttpConnectWrapper();
        InputStream is = null;
        try {
        	is = http.httpPost(req.getURL(), req.getContentType(), req.getData(), http.checkNetWorkType(), req.getNeedRespHeaderProps());
            if(is!=null)
            {
            	resp.parseInputStream(is);//此函数会关闭is
                errorCode = ErrorInfo.SUCCESS;
            }

           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            http.releaseConnect();
        }        
        return errorCode;	       
	}
	
	
	private static int sendDebug(IRequestMsg req,IResponseMsg resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;		
		HttpConnectWrapper http = new HttpConnectWrapper();
        InputStream is = null;
        try {
        	String a= req.getClass().getName();
        	if(req.getClass().getName().equals(LoginAuthMsgReq.class.getName()))
        	{
        		is = testInputStreamLoginAuthMsgReq();
        	}
        	else if(req.getClass().getName().equals(GetCountInfoRequestMsg.class.getName()))
        	{
        		is = testInputStreamGetCountInfoRequestMsg();
        	}
        	else if(req.getClass().getName().equals(ListFriendsRequestMsg.class.getName()))
        	{
        		is = testInputStreamListFriendsRequestMsg();
        	}
        	else if(req.getClass().getName().equals(GetInfoByContactIdRequestMsg.class.getName()))
        	{
        		is = testInputStreamGetInfoByContactIdRequestMsg();
        	}
        	
        	
            resp.parseInputStream(is);//此函数会关闭is
            errorCode = ErrorInfo.SUCCESS;
           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            http.releaseConnect();
        }        
        return errorCode;	    
	}
	
	private static InputStream testInputStreamLoginAuthMsgReq()
	{
		//return StringAndInputStreamUtil.String2InputStream("<users_loginAuth_response>  <result>  123456  </result></users_loginAuth_response>");
		return StringAndInputStreamUtil.String2InputStream("<users_loginAuth_response xmlns=\"http://jiekou.shequ.10086.cn/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jiekou.shequ.10086.cn/1.0/ http://jiekou.shequ.10086.cn/1.0/139.xsd\" list=\"true\">  <result>bebf224f7981bc3262c4e14f13a463ae</result></users_loginAuth_response>");
//
	}
	
	private static InputStream testInputStreamGetCountInfoRequestMsg()
	{
		return StringAndInputStreamUtil.String2InputStream("<address_list_getCountInfo_response xmlns=\"http://jiekou.shequ.10086.cn/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jiekou.shequ.10086.cn/1.0/ http://jiekou.shequ.10086.cn/1.0/139.xsd\" list=\"true\"><total>2</total><group><item><groupId>10001</groupId><groupName>好友瑜</groupName><groupCount>1</groupCount></item><item><groupId>10002</groupId><groupName>家人婷</groupName><groupCount>1</groupCount></item></group></address_list_getCountInfo_response>");
	}
	
	private static InputStream testInputStreamListFriendsRequestMsg()
	{
		return StringAndInputStreamUtil.String2InputStream("<address_list_listFriends_response xmlns=\"http://jiekou.shequ.10086.cn/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jiekou.shequ.10086.cn/1.0/ http://jiekou.shequ.10086.cn/1.0/139.xsd\" list=\"true\"><resultCount>2</resultCount><listFriend><item><contactId>1001</contactId><friendName>梁波1</friendName><mobile>125801</mobile><email>lb@123.com</email><nameSpell>liangbo</nameSpell><updateTime>2010-09-02:23:25:1365</updateTime></item><item><contactId>1002</contactId><friendName>!2</friendName><mobile>125802</mobile><email>lb@123.com</email><nameSpell>liangbo</nameSpell><updateTime>2010-09-02:23:25:1365</updateTime></item></listFriend></address_list_listFriends_response>");

	}
	
	private static InputStream testInputStreamGetInfoByContactIdRequestMsg()
	{
		return StringAndInputStreamUtil.String2InputStream("<address_list_getInfoByContactId_response xmlns=\"http://jiekou.shequ.10086.cn/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jiekou.shequ.10086.cn/1.0/ http://jiekou.shequ.10086.cn/1.0/139.xsd\" list=\"true\"><result>1</result><listFriend><item><contactId>1001</contactId><friendName>梁波瑜1</friendName><friendMobile>125801</friendMobile><email>lb@123.com</email><typeIdList><item>10001</item></typeIdList><friendQQ>qq1</friendQQ><friendFetion>飞信1</friendFetion><friendOtherNumber>其他n1</friendOtherNumber><friendOtherTel>其他t1</friendOtherTel><friendURL>个人网1</friendURL><officePhone>公司电话1</officePhone><friendBirthday>friendBirthday</friendBirthday><friendMsn>friendMsn</friendMsn><friendCompany>friendCompany</friendCompany><friendPosition>friendPosition</friendPosition><friendTel>friendTel</friendTel><friendEleTel>friendEleTel</friendEleTel><friendState>friendState</friendState><friendCity>friendCity</friendCity><friendPostalCode>friendPostalCode</friendPostalCode><friendAddress>friendAddress</friendAddress><companyURL>companyURL</companyURL><companyAddress>companyAddress</companyAddress></item><item><contactId>1002</contactId><friendName>!2婷</friendName><friendMobile>125802</friendMobile><email>lb@123.com</email><typeIdList>10002</typeIdList><friendOtherNumber>其他n1</friendOtherNumber></item></listFriend></address_list_getInfoByContactId_response>");
		//return StringAndInputStreamUtil.String2InputStream("<address_list_getInfoByContactId_response xmlns=\"http://jiekou.shequ.10086.cn/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jiekou.shequ.10086.cn/1.0/ http://jiekou.shequ.10086.cn/1.0/139.xsd\" list=\"true\"><result>1</result><listFriend><item><contactId>1001</contactId><friendName>梁波1</friendName><friendMobile>125801</friendMobile><email>lb@123.com</email><typeIdList><item>10001</item></typeIdList><friendQQ>qq1</friendQQ><friendFetion>飞信1</friendFetion><friendOtherNumber>其他n1</friendOtherNumber><updateTime>2010-09-02:23:25:1365</updateTime></item><item><contactId>1002</contactId><friendName>梁波2</friendName><friendMobile>125802</friendMobile><email>lb@123.com</email><typeIdList><item>10002</item></typeIdList><officePhone>公司电话2</officePhone><updateTime>2010-09-02:23:25:1365</updateTime></item></listFriend></address_list_getInfoByContactId_response>");

	}
	
}
