package com.android.contacts.aspire.config;

public class Config 
{
    /** 日志MidLog表最大存储量 */
      public static int logsize=1000;
      
      /**协议版本*/
      public static String version = "1.0";
      
//    public static String URLservice = "http://211.139.191.156:8085/mid/service";
//    public static String URLcontente = "http://211.139.191.156:8085/mid/service";
    
    //test
    //public static String URL_SERVICE = "http://211.139.191.156:8085/MidGateway/client";
      //public static String URL_SERVICE = "http://10.1.31.30:8080/mid/contacts";
      //public static final String URL_SERVICE = "http://10.1.31.30:8080/mid/contacts";
      //public static final String URL_SERVICE = "http://10.1.4.52:8085/mid/contacts";
      public static final String URL_SERVICE = "http://211.139.191.156:8085/mid/contacts";
      
      public static final String URL_139_XMLNS = "http://jiekou.shequ.10086.cn/1.0/";
      //public static final String URL_139_XMLNS = "";
      
    /**
     * 上网平台接口访问地址
     */
//    public static String URL_NB_SERVICE = "http://211.139.191.156:8085/MidGateway/client";
//    public static String URL_SERVICE = "http://www.baidu.com";
    
    //139 和sim的帐号类型名称
    public static final String ACCOUNT_TYPE_139 = "com.139";
    public static final String ACCOUNT_TYPE_SIM = "SIM";
    
    
    public static final String DEFAULT_GROUP_SOURCE_ID="default_group";
    //public static final String DEFAULT_GROUP_SOURCE_ID_SIM="default_group_sim";
    
    public static final String DEFAULT_GROUP_NAME="未分组";
    //public static final String DEFAULT_GROUP_NAME_SIM="未分组-SIM";
    
    //同步139的时候 每次同步的联系人个数大小
    public static final int SYNC_PAGE_SIZE = 20;
    //同步139数据的时候 每次去的的本地的分页大小
    public static final int SYNC_LOCAL_PAGE_SIZE = 100;
    
    
    
    public static final boolean IS_DEBUG =  false;
    
    public static final boolean IS_SIMULATOR_TEST = true;
    
    public static final boolean IS_IMPORT_TEST = true;
    
    
}
