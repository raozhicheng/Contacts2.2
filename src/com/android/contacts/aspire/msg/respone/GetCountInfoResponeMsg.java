package com.android.contacts.aspire.msg.respone;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.android.contacts.aspire.config.Config;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.StartElementListener;

/**
 * Filename: GetCountInfoResponeMsg.java Description: Copyright: Copyright
 * (c)2009 Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-8-31 上午09:01:12
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-8-31 liangbo 1.0 1.0 Version
 */

public class GetCountInfoResponeMsg extends BaseWith139ResponeMsg {
	
	//好友总数量
	public int total=0;
	
	//好友群组列表
	public ArrayList<GroupItem> groupItemList=new ArrayList<GroupItem>();
	private GroupItem groupItem;

	public GetCountInfoResponeMsg() {
		super("address_list_getCountInfo_response");

		// 总计数量total
		Element licenseTag = root.getChild(Config.URL_139_XMLNS,"total");
		licenseTag.setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					total= Integer.parseInt(body)  ;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Element  groupItemsTag = root.getChild(Config.URL_139_XMLNS,"group");
		setListGroupItem(groupItemsTag); 
	}
	
	private void setListGroupItem(Element groupItemsTag) {		
		Element groupItemTag = groupItemsTag.getChild(Config.URL_139_XMLNS,"item");
		groupItemTag.setStartElementListener(new StartElementListener() {			
			
			public void start(Attributes attributes) {
				groupItem=new GroupItem();
				
			}
		});
		groupItemTag.setEndElementListener(new EndElementListener() {

			public void end() {
				groupItemList.add(groupItem);
			}
		});
		
		//groupId解析
		Element groupIdTag = groupItemTag.getChild(Config.URL_139_XMLNS,"groupId");
		groupIdTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				groupItem.groupId=body;
				
			}
		}); 
		
		
		//groupId解析
		Element groupNameTag = groupItemTag.getChild(Config.URL_139_XMLNS,"groupName");
		//设置监听
		groupNameTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				groupItem.groupName=body;	
			}
		}); 
		
		//groupCount解析
		Element groupCountTag = groupItemTag.getChild(Config.URL_139_XMLNS,"groupCount");
		//设置监听
		groupCountTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				
				try {
					groupItem.groupCoun = Integer.parseInt(body);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}); 

}
	 @Override
     public String toString(){
 		String xml = " total = "+ total +"  "+ groupItemList.toString();	
		return super.toString() + xml.toString();
     }
}
