package com.cn.ucoon.controller;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cn.ucoon.service.UserService;

@Controller
@RequestMapping("/chat")
public class ChatController {

	@Resource
	private UserService userService;

	/**
	 * 聊天
	 * 
	 * @param request
	 * @param model
	 * @throws IOException
	 */
	@RequestMapping(value = "/api-1", method = RequestMethod.GET)
	public String chat(@RequestParam(value = "fromuserid") String fromuserid,
			@RequestParam(value = "touserid") String touserid) {

		if(fromuserid == null || touserid==null){
			return "redirect:/html/404.html";
		}
		
		return "redirect:/html/chat.html?fromuserid=" + fromuserid + "&touserid=" + touserid;
	}
}
