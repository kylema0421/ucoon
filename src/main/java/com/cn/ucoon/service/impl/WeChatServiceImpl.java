package com.cn.ucoon.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.cn.ucoon.dao.UserMapper;
import com.cn.ucoon.pojo.User;
import com.cn.ucoon.pojo.wx.resp.Article;
import com.cn.ucoon.pojo.wx.resp.NewsMessage;
import com.cn.ucoon.pojo.wx.resp.TextMessage;
import com.cn.ucoon.service.WeChatService;
import com.cn.ucoon.util.MessageUtil;
import com.cn.ucoon.util.WeixinUtil;

/*
 * 核心服务类
 * */
@Service
@Transactional
public class WeChatServiceImpl implements WeChatService  {

	

	@Resource
	private UserMapper userDao;
	
	/**
	 * 处理微信发来的请求
	 * **/
	@Override
	public String processRequest(HttpServletRequest request) {
		String respMessage = null;
		try {
			// 默认返回的文本消息内容
			String respContent = null;
			// xml请求解析
			Map<String, String> requestMap = new MessageUtil()
					.parseXml(request);

			// 发送方账号（open_id）
			String openID = requestMap.get("FromUserName");
			// System.out.println("发送方" + fromUserName);

			// 公众账号
			String toUserName = requestMap.get("ToUserName");
			// System.out.println("公众号id" + toUserName);
			// 消息类型
			String msgType = requestMap.get("MsgType");

			// 回复默认文本消息
			 TextMessage textMessage = new TextMessage();
			 textMessage.setToUserName(openID);
			 textMessage.setFromUserName(toUserName);
			 textMessage.setCreateTime(new Date().getTime());
			 textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			 textMessage.setFuncFlag(0);
			 textMessage.setContent("内测中");
			 respMessage = MessageUtil.textMessageToXml(textMessage);

			 
			 
			 
			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String content = requestMap.get("Content").trim();// 接收用户的消息文本
//				String str = new TextService().getText(content, textMessage,
//						openID, toUserName, request);
//				if (str != null) {
//					respMessage = str;
//				}

				// else if(content.startsWith("歌曲")){
				// String respMusicMessage = null;
				// String keyWord = content.replaceAll("^歌曲[\\+ ~!@#%^-_=]?",
				// "").trim();
				// if("".equals(keyWord)){
				// textMessage.setContent(BaiduMusicService.getUsage());
				// } else {
				// String [] strs = keyWord.split("@");
				// String musicTitle = strs[0];
				// String musicAuthor = "";//默认作者为空
				// if(2 == strs.length){
				// musicAuthor = strs[1];
				// }
				// Music music = BaiduMusicService.searchMusic(musicTitle,
				// musicAuthor);

				// MusicMessage musicMessage = new MusicMessage();
				// musicMessage.setCreateTime(new Date().getTime());
				// musicMessage.setFromUserName(toUserName);
				// musicMessage.setToUserName(fromUserName);
				// musicMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_MUSIC);
				// musicMessage.setMusic(music);
				// respMusicMessage =
				// MessageUtil.musicMessageToXml(musicMessage);

			}

			// 图片消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "您发送的是图片消息";
			}

			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				// respContent = "您发送的是地理位置消息";
			}

			// 链接消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				// respContent = "您发送的是链接消息";
			}

			// 音频消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				// respContent = "您发送的是音频消息";
			}

			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
//					NewsMessage newsMessage = new NewsMessage();
//					newsMessage.setFromUserName(toUserName);
//					newsMessage.setToUserName(openID);
//					newsMessage.setCreateTime(new Date().getTime());
//					newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
//					newsMessage.setFuncFlag(0);
//					List<Article> articlelist = new ArrayList<Article>();
//
//					Article article1 = new Article();
//					article1.setTitle("吼~终于等到你啦！");
//					article1.setDescription("属于集大人的服务定制平台！查成绩、查课表等查询功能全新推出！【回复“部落”查看更多校内资讯】");
//					article1.setPicUrl("http://www.jmutong.com/JMUTong/Images/welcome.jpg");
//					article1.setUrl("http://mp.weixin.qq.com/s?__biz=MzA5NDE4NTYzMA==&mid=205847065&idx=1&sn=1af387ea0187cb8ef7e2dadc11ac3009#rd");
//					articlelist.add(article1);
//					newsMessage.setArticleCount(articlelist.size());
//					newsMessage.setArticles(articlelist);
					//respMessage = MessageUtil.newsMessageToXml(newsMessage);
					respContent = "欢迎关注有空ucoon";
					textMessage.setContent(respContent);
					respMessage = MessageUtil.textMessageToXml(textMessage);
					User user = userDao.selectByOpenId(openID);
					if(user == null){
						user = new User();
						
						Date regist_time  = new Date();
						
						JSONObject json = WeixinUtil.getBaseUserInfo(openID);
						user.setOpenId(openID);
						user.setNickName(json.getString("nickname"));
						user.setSex(json.getInteger("sex"));
						user.setProvince(json.getString("province"));
						user.setCity(json.getString("city"));
						user.setCountry(json.getString("country"));
						user.setHeadImgUrl(json.getString("headimgurl"));
						user.setRegistTime(regist_time);//根据当前时间戳更新
						
						userDao.regist(user);
					}
					
					
					
					
				
				}
				// 取消订阅
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
//					UserDao userDao = UserDaoFactory.getInstance().getUserDao();
//					System.out.println("用户:'" + userDao.getUserRealName(openID)
//							+ "'取消关注！      openID=" + openID);
				}
				// 自定义菜单点击事件
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
//					String eventkey = requestMap.get("EventKey");
//					respMessage = new ClickEventService().getClickEventXml(
//							eventkey, openID, toUserName, request, textMessage);
				}
				// 用户上报地址位置，更新用户地理位置信息
				else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
					System.out.println("1");
					String latitude = requestMap.get("Latitude"); //纬度
					String longitude = requestMap.get("Longitude"); //经度
					String precision = requestMap.get("Precision"); //精确度
					User user = new User();
					user.setLatitude(latitude);
					user.setLongitude(longitude);
					userDao.updateUserLatAndLng(user);
				}
			}

			// textMessage.setContent(respContent);
			// respMessage = MessageUtil.textMessageToXml(textMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		return respMessage;
	}

}