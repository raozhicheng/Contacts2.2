
package com.android.contacts.aspire.msg.request;

import java.util.Hashtable;

import com.android.contacts.aspire.config.Config;


public abstract class XmlRequestMsg implements IRequestMsg {
    protected MessageHeader header = new MessageHeader();

    private Hashtable<String, String> props = null;

    private String contentType = "text/xml";

    private static String enc = "UTF-8";

    public byte[] getData() throws Exception {
//        if (!header.msgType.equals(MessageType.ServiceAccess_REQ)) {
            String xmlData = "";
//            if (header.msgType.equals(MessageType.GET_PASSWORD_REQ)
//                    || header.msgType.equals(MessageType.GET_BINDINFO_REQ)
//                    || header.msgType.equals(MessageType.BIND_FEEMSISDN_REQ)
//                    || header.msgType.equals(MessageType.UNBIND_FEEMSISDN_REQ)) {
//                xmlData = header.getMsgNHeadler() + toXml();
//            } else {
                xmlData = header.getMsgHeadler() + toXml();
//            }
            // test
            System.out.println("---request xmlMsg =" + xmlData);
            return xmlData.getBytes(enc);
//        } else {
//            return null;
//        }

    }

    public byte[] getData(int length) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void addNeedRespHeaderProp(String key, String value) {
        // TODO Auto-generated method stub
        if (value == null || key == null) 
        {
            return;
        }

        if (props == null) 
        {
            props = new Hashtable<String, String>();
        }

        props.put(key, value);
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return contentType;
    }

    public Hashtable getNeedRespHeaderProps() 
    {
        if (props == null) 
        {
            props = new Hashtable<String, String>();
        }

        props.put("MsgType", this.header.msgType);
        props.put("DeviceID", this.header.deviceID);
        props.put("PlatformCode", this.header.platformCode);
        props.put("ServiceCode", this.header.serviceCode);
        return props;
    }

    public boolean isCMWap() {
        // TODO Auto-generated method stub
        return false;
    }

    public abstract String toXml();

    public String getURL() {
        // TODO Auto-generated method stub
        return Config.URL_SERVICE;
    }
    
    public String getMsgType(){
        return this.header.msgType;
    }

}
