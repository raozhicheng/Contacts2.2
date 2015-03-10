package com.android.contacts.aspire.msg.respone;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.StartElementListener;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.config.ErrorInfo;

/**  
 * Filename:    GetInfoByContactIdResponeMsg.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 下午01:11:22  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */
//解析XML规则
public class GetInfoByContactIdResponeMsg extends BaseWith139ResponeMsg{

	//_response	[1,1]	成功返回1，失败返回0
	public int result=ErrorInfo.IC139_OP_ERROR;
	
	
	//_response	[0,1]	联系人列表
	public ArrayList<FriendItem> friendItemList=new ArrayList<FriendItem>();
	private FriendItem friendItem;

	
	
	public GetInfoByContactIdResponeMsg() {
		super("address_list_getInfoByContactId_response");


		// 返回结果result
		Element resultTag = root.getChild(Config.URL_139_XMLNS,"result");
		resultTag.setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					result= Integer.parseInt(body)  ;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Element  listFriendsTag = root.getChild(Config.URL_139_XMLNS,"listFriend");
		setlistFriendItem(listFriendsTag); 
	}
	
	private void setlistFriendItem(Element listFriendsTag) {		
		Element friendItemTag = listFriendsTag.getChild(Config.URL_139_XMLNS,"item");
		friendItemTag.setStartElementListener(new StartElementListener() {			
			
			public void start(Attributes attributes) {
				friendItem=new FriendItem();
				
			}
		});
		friendItemTag.setEndElementListener(new EndElementListener() {

			public void end() {
				friendItemList.add(friendItem);
			}
		});
		
		
		//contactId解析
		Element contactIdTag = friendItemTag.getChild(Config.URL_139_XMLNS,"contactId");
		contactIdTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				try {
					friendItem.contactId = Integer.parseInt(body) ;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}); 
		
		
		//contactId解析
		Element friendUserIdTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendUserId");
		friendUserIdTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
					friendItem.friendUserId =body ;
				}
				
			}
		}); 
		
		//updateTime解析
		Element updateTimeTag = friendItemTag.getChild(Config.URL_139_XMLNS,"updateTime");
		updateTimeTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.updateTime =body ;}
			}
		}); 
		
		//updateTime解析
		Element commonlyFlagTag = friendItemTag.getChild(Config.URL_139_XMLNS,"commonlyFlag");
		commonlyFlagTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.commonlyFlag =body ;}
			}
		}); 
		
		
		//friendName解析
		Element friendNameTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendName");
		friendNameTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendName =body ;}
			}
		}); 
		
		
		//nameSpell解析
		Element nameSpellTag = friendItemTag.getChild(Config.URL_139_XMLNS,"nameSpell");
		nameSpellTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.nameSpell = body ;}
			}
		});
		
		
		
		//friendMobile解析
		Element friendMobileTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendMobile");
		friendMobileTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendMobile =body ;}
			}
		}); 
		
		
		//friendOtherNumber解析
		Element friendOtherNumberTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendOtherNumber");
		friendOtherNumberTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendOtherNumber =body ;}
			}
		}); 
		
		//friendOtherTel解析
		Element friendOtherTelTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendOtherTel");
		friendOtherTelTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendOtherTel =body ;}
			}
		}); 
		
		//officePhone解析
		Element officePhoneTag = friendItemTag.getChild(Config.URL_139_XMLNS,"officePhone");
		officePhoneTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.officePhone =body ;}
			}
		}); 
		
		//friendTel解析
		Element friendTelTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendTel");
		friendTelTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendTel =body ;}
			}
		}); 
		
		//friendEleTel解析
		Element friendEleTelTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendEleTel");
		friendEleTelTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendEleTel =body ;}
			}
		}); 
		
		//email解析
		Element emailTag = friendItemTag.getChild(Config.URL_139_XMLNS,"email");
		emailTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.email =body ;}
			}
		}); 
		
		//friendQQ解析
		Element friendQQTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendQQ");
		friendQQTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendQQ =body ;}
			}
		}); 
		
		
		//friendQQ解析
		Element friendFetionTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendFetion");
		friendFetionTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendFetion =body ;}
			}
		}); 
		
		//friendMsn解析
		Element friendMsnTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendMsn");
		friendMsnTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendMsn =body ;}
			}
		}); 
		
		
		//friendURL解析
		Element friendURLTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendURL");
		friendURLTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendURL =body ;}
			}
		}); 
		
		//companyURL解析
		Element companyURLTag = friendItemTag.getChild(Config.URL_139_XMLNS,"companyURL");
		companyURLTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.companyURL =body ;}
			}
		}); 
		
		//friendCompany解析
		Element friendCompanyTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendCompany");
		friendCompanyTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendCompany =body ;}
			}
		}); 
		
		//friendPosition解析
		Element friendPositionTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendPosition");
		friendPositionTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendPosition =body ;}
			}
		}); 
		
		//friendState解析
		Element friendStateTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendState");
		friendStateTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendState =body ;}
			}
		}); 
		
		//friendCity解析
		Element friendCityTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendCity");
		friendCityTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendCity =body ;}
			}
		}); 
		
		
		//friendPostalCode解析
		Element friendPostalCodeTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendPostalCode");
		friendPostalCodeTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendPostalCode =body ;}
			}
		}); 
		
		
		//friendAddress解析
		Element friendAddressTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendAddress");
		friendAddressTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendAddress =body ;}
			}
		}); 
		
		
		//companyAddress解析
		Element companyAddressTag = friendItemTag.getChild(Config.URL_139_XMLNS,"companyAddress");
		companyAddressTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.companyAddress =body ;}
			}
		}); 
		
		//friendBirthday解析
		Element friendBirthdayTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendBirthday");
		friendBirthdayTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendBirthday =body ;}
			}
		}); 
		
		
		//friendSex解析
		Element friendSexTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendSex");
		friendSexTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendSex =body ;}
			}
		}); 
		
		
		//note解析
		Element noteTag = friendItemTag.getChild(Config.URL_139_XMLNS,"note");
		noteTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.note =body ;}
			}
		}); 
		
		
		//friendRemark解析
		Element friendRemarkTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendRemark");
		friendRemarkTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.friendRemark = body ;}
			}
		});
		
		//contactTime解析
		Element contactTimeTag = friendItemTag.getChild(Config.URL_139_XMLNS,"contactTime");
		contactTimeTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.contactTime = body ;}
			}
		});
		
		
		//blackFlag解析
		Element blackFlagTag = friendItemTag.getChild(Config.URL_139_XMLNS,"blackFlag");
		blackFlagTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.blackFlag = body ;}
			}
		});
		
		
		//twowayFlag解析
		Element twowayFlagTag = friendItemTag.getChild(Config.URL_139_XMLNS,"twowayFlag");
		twowayFlagTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.twowayFlag = body ;}
			}
		});
		
		
		//dataFromFlag解析
		Element dataFromFlagTag = friendItemTag.getChild(Config.URL_139_XMLNS,"dataFromFlag");
		dataFromFlagTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
				friendItem.dataFromFlag = body ;}
			}
		});
		
		
		
		Element  typeIdListTag = friendItemTag.getChild(Config.URL_139_XMLNS,"typeIdList");
		/*typeIdListTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				if(body!=null && body.length()>0)
				{
					String tempGroupID = body ;
					String[] tGroups =   tempGroupID.split(",");
					if(friendItem.typeIdList == null)
					{
						friendItem.typeIdList = new ArrayList<String>(); 
					}
					
					if(tGroups!=null)
					{
						
						for(int i=0,size=tGroups.length ;i<size;i++)
						{
							friendItem.typeIdList.add(tGroups[i]);
						}
					}
					
					
				}
			}
		});*/
		//Element  typeIdListTag = friendItemTag.getChild("typeIdList");
		//typeIdListTag.setEndElementListener( new EndElementListener() {
		/*typeIdListTag.setEndTextElementListener(new EndTextElementListener() {	
			
			public void end(final String body) {

				System.out.println("<item> tGroups body = "+body);
				if(body!=null && body.length()>0)
				{
					String tempGroupID = body ;
					if(tempGroupID.contains("<item>")){
						
						
						if(friendItem.typeIdList == null)
						{
							friendItem.typeIdList = new ArrayList<String>(); 
						}
						
						String[] tGroups2=tempGroupID.split("</item>");
						System.out.println("<item> tGroups2 length=    "+tGroups2.length);
						for(int i=0;i<tGroups2.length;i++){
							String ts= tGroups2[i].replaceAll("<item>", "").trim();
							if(ts!=null && ts.length()>0)
							{
								friendItem.typeIdList.add(ts);
								System.out.println("<item> +++++  ts=    "+ts);
							}
						}
						
					}else{
					
					
					String[] tGroups =   tempGroupID.split(",");
					if(friendItem.typeIdList == null)
					{
						friendItem.typeIdList = new ArrayList<String>(); 
					}
					
					if(tGroups!=null)
					{
						
						for(int i=0,size=tGroups.length ;i<size;i++)
						{
							friendItem.typeIdList.add(tGroups[i]);
							System.out.println(", +++++  ts=    "+tGroups[i]);
						}
					}
					
					
				}
			}
				}

			
			

		}

		);*/
		setTypeIdListItem(typeIdListTag); 
		//_response	[0,1]	联系人列表
//		public ArrayList<Integer> typeIdList=new ArrayList<Integer>();
//		private FriendItem friendItem;
//		public ArrayList<Integer> typeIdList=null;
	
	}
	
	
	private void setTypeIdListItem(Element typeIdListTag)
	{
		Element typeItemTag = typeIdListTag.getChild(Config.URL_139_XMLNS,"item");
		typeItemTag.setStartElementListener(new StartElementListener() {			
			
			public void start(Attributes attributes) {
				if(friendItem.typeIdList==null)
				{
					friendItem.typeIdList = new ArrayList<String>(); 
				}				
			}
		});
		typeItemTag.setEndTextElementListener(new EndTextElementListener() {

			public void end(String body) {
				
					friendItem.typeIdList.add( body );
				
			}
		});
	}
	
	
	
	public static GetInfoByContactIdResponeMsg testGetInfoByContactIdResponeMsg1()
	{
		GetInfoByContactIdResponeMsg resp = new GetInfoByContactIdResponeMsg();
		
		FriendItem lfi = new FriendItem();
		lfi.contactId = 10;
		lfi.updateTime = "2010-09-31";
		lfi.friendName ="详细 -联系人1";
		lfi.friendMobile="详细 -联系人1-电话";
		lfi.email ="详细 -联系人1-邮件2@liangbo.com";	
		
		lfi.friendOtherNumber ="详细 -联系人1-工作手机";	
		lfi.friendTel ="详细 -联系人1-电话(家庭)";
		lfi.friendOtherTel ="详细 -联系人1-电话1(其他)";
		lfi.officePhone ="详细 -联系人1-工作电话";
		lfi.friendEleTel ="详细 -联系人1-工作传真";
		
		
		lfi.friendQQ ="详细 -联系人1-QQ";
		lfi.friendFetion ="详细 -联系人1-飞信";
		lfi.friendMsn ="详细 -联系人1-MSN";
		
		lfi.friendURL ="详细 -联系人1-个人主页";
		lfi.companyURL ="详细 -联系人1-公司主页";
		
		lfi.friendCompany ="详细 -联系人1-公司";
		lfi.friendPosition ="详细 -联系人1-职务";
		
		lfi.friendState ="详细 -联系人1-省";
		lfi.friendCity ="详细 -联系人1-城";
		lfi.friendPostalCode ="详细 -联系人1-邮编";
		lfi.friendAddress ="详细 -联系人1-家庭地址";
		lfi.companyAddress ="详细 -联系人1-公司地址";
		
		lfi.friendBirthday ="详细 -联系人1-生日";
		
		
		resp.friendItemList.add(lfi);
		
		
		return  resp;
	}
	
	
	public static GetInfoByContactIdResponeMsg testGetInfoByContactIdResponeMsg2()
	{
		GetInfoByContactIdResponeMsg resp = new GetInfoByContactIdResponeMsg();
		
		FriendItem lfi = new FriendItem();
		lfi.contactId = 10;
		lfi.updateTime = "2010-09-32";
		lfi.friendName ="详细 -联系人2";
		lfi.friendMobile="详细 -联系人2-电话";
		lfi.email ="详细 -联系人2-邮件2@liangbo.com";	
		
		lfi.friendOtherNumber ="详细 -联系人2-工作手机";	
		lfi.friendTel ="详细 -联系人2-电话(家庭)";
		lfi.friendOtherTel ="详细 -联系人2-电话1(其他)";
		lfi.officePhone ="详细 -联系人2-工作电话";
		lfi.friendEleTel ="详细 -联系人2-工作传真";
		
		
		lfi.friendQQ ="详细 -联系人2-QQ";
		lfi.friendFetion ="详细 -联系人2-飞信";
		lfi.friendMsn ="详细 -联系人2-MSN";
		
		lfi.friendURL ="详细 -联系人2-个人主页";
		lfi.companyURL ="详细 -联系人2-公司主页";
		
		lfi.friendCompany ="详细 -联系人2-公司";
		lfi.friendPosition ="详细 -联系人2-职务";
		
		lfi.friendState ="详细 -联系人2-省";
		lfi.friendCity ="详细 -联系人2-城";
		lfi.friendPostalCode ="详细 -联系人2-邮编";
		lfi.friendAddress ="详细 -联系人2-家庭地址";
		lfi.companyAddress ="详细 -联系人2-公司地址";
		
		lfi.friendBirthday ="详细 -联系人2-生日";
		
		
		resp.friendItemList.add(lfi);
		
		
		return  resp;
	}
	
	
	public static GetInfoByContactIdResponeMsg testGetInfoByContactIdResponeMsg3()
	{
		GetInfoByContactIdResponeMsg resp = new GetInfoByContactIdResponeMsg();
		
		FriendItem lfi = new FriendItem();
		lfi.contactId = 10;
		lfi.updateTime = "2010-09-33";
		lfi.friendName ="详细 -联系人3";
		lfi.friendMobile="详细 -联系人3-电话";
		lfi.email ="详细 -联系人3-邮件2@liangbo.com";	
		
//		lfi.friendOtherNumber ="详细 -联系人2-工作手机";	
//		lfi.friendTel ="详细 -联系人2-电话(家庭)";
//		lfi.friendOtherTel ="详细 -联系人2-电话1(其他)";
//		lfi.officePhone ="详细 -联系人2-工作电话";
//		lfi.friendEleTel ="详细 -联系人2-工作传真";
//		
//		
//		lfi.friendQQ ="详细 -联系人2-QQ";
//		lfi.friendFetion ="详细 -联系人2-飞信";
//		lfi.friendMsn ="详细 -联系人2-MSN";
//		
//		lfi.friendURL ="详细 -联系人2-个人主页";
//		lfi.companyURL ="详细 -联系人2-公司主页";
//		
//		lfi.friendCompany ="详细 -联系人2-公司";
//		lfi.friendPosition ="详细 -联系人2-职务";
//		
//		lfi.friendState ="详细 -联系人2-省";
//		lfi.friendCity ="详细 -联系人2-城";
//		lfi.friendPostalCode ="详细 -联系人2-邮编";
//		lfi.friendAddress ="详细 -联系人2-家庭地址";
//		lfi.companyAddress ="详细 -联系人2-公司地址";
//		
//		lfi.friendBirthday ="详细 -联系人2-生日";
		
		
		resp.friendItemList.add(lfi);
		
		
		return  resp;
	}

}
