package com.cn.ucoon.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.cn.ucoon.pojo.Evaluate;
import com.cn.ucoon.pojo.Mission;
import com.cn.ucoon.pojo.MissionOrders;
import com.cn.ucoon.pojo.User;
import com.cn.ucoon.service.EvaluateService;
import com.cn.ucoon.service.MissionOrderService;
import com.cn.ucoon.service.MissionService;
import com.cn.ucoon.service.UserService;
import com.cn.ucoon.util.PayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/mission")
public class MissionController {

	@Autowired
	private MissionService missionService;
	
	@Autowired
	private MissionOrderService missionOrderService;

	@Autowired
	private UserService userService;

	@Autowired
	private EvaluateService evaluateService;
	
	/**
	 * 发布任务
	 * 
	 * @param mission
	 *            对表表单 自动封装Mission对象
	 * @param file
	 *            多张图片
	 * @param request
	 *            请求
	 * @return 跳转页面
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/add-mission")
	public String publishMission(
			@RequestParam(value = "missionTitle", required = false) String missionTitle,
			@RequestParam(value = "missionDescribe", required = false) String missionDescribe,
			@RequestParam(value = "missionPrice", required = false) Double missionPrice,
			@RequestParam(value = "peopleCount", required = false) Integer peopleCount,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "telephone", required = false) String telephone,
			@RequestParam(value = "detailPlace", required = false) String detailPlace,
			@RequestParam(value = "missionLng", required = false) String missionLng,
			@RequestParam(value = "missionLat", required = false) String missionLat,
			@RequestParam(value = "imgUpload", required = false) MultipartFile[] file,
			HttpServletRequest request) throws ParseException {

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");//小写的mm表示的是分钟  
		Date endDate=sdf.parse(endTime);
		Date StartDate=sdf.parse(startTime);
		Mission mission = new Mission();
		mission.setEndTime(endDate);
		mission.setMissionDescribe(missionDescribe);
		mission.setMissionPrice(new BigDecimal(missionPrice));
		mission.setMissionTitle(missionTitle);
		mission.setPeopleCount(peopleCount);
		mission.setPlace(place+detailPlace);
		mission.setStartTime(StartDate);
		mission.setTelephone(telephone);
		mission.setMissionLat(missionLat);
		mission.setMissionLng(missionLng);
		
		
		String path = ImageController.MISSION_IMAGE_LOCATION;
		Integer userId = (Integer) request.getSession().getAttribute("user_id");
		String timestamp = String.valueOf(System.currentTimeMillis());
		String uuid = String.valueOf(UUID.randomUUID());
		uuid = uuid.replace("-", "");
		String realpath = path + "/" + userId + timestamp + uuid;// 文件夹位置
		File dir = new File(realpath);
		dir.mkdirs();
		System.out.println("wenjian:" + file.length);
		for (int i = 0; i < file.length; i++) {
			if (!file[i].isEmpty()) {
				String fileName = file[i].getOriginalFilename();// 文件原名称
				String type = fileName.indexOf(".") != -1 ? fileName.substring(
						fileName.lastIndexOf(".") + 1, fileName.length())
						: null;
				try {
					file[i].transferTo(new File(realpath + "/" + i + "." + type));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		mission.setPictures(userId + timestamp + uuid);
		mission.setUserId(userId);
		mission.setViewCount(0);
		mission.setMissionStatus(0); //待支付
		mission.setPicCount(file.length);
		mission.setPublishTime(new Date());
		MissionOrders missionOrders = new MissionOrders();
		if(missionService.publishMission(mission)){
			
			System.out.println("mission_id:" + mission.getMissionId());
			
			missionOrders.setMissionId(mission.getMissionId());
			missionOrders.setMissionOrderNum(PayUtil.getOrdersNum(userId, mission.getMissionId()));
			missionOrders.setOrderTime(new Date());
			missionOrders.setState(0);//未支付
			missionOrders.setUserId(userId);
			missionOrderService.makeOrders(missionOrders);
		}
		//存order，mission，微信支付时取
		request.getSession().setAttribute("orders", missionOrders);
		request.getSession().setAttribute("mission", mission);
		
		
		//任务发布，订单生成,跳转支付界面
		return "redirect:/mission-pay";
	}

	/**
	 * 分页查询
	 * 
	 * @param userId
	 *            通过userId搜索
	 * @param missionStatus
	 *            通过missionStatus搜索
	 * @param keyWord
	 *            通过关键字搜索任务
	 * @param startIndex
	 *            开始位置
	 * @param endIndex
	 *            结束位置
	 * @return json
	 */
	@RequestMapping(value = "/getMissionsLimited", method = RequestMethod.POST)
	@ResponseBody
	public String getMissionsLimited(
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "missionStatus", required = false) Integer missionStatus,
			@RequestParam(value = "keyWord", required = false) String keyWord,
			@RequestParam(value = "startIndex", required = true) Integer startIndex,
			@RequestParam(value = "endIndex", required = true) Integer endIndex,
			@RequestParam(value = "latitude", required = false) String latitude,
			@RequestParam(value = "longitude", required = false) String longitude,
			@RequestParam(value = "type", required = false) String type,
			HttpServletRequest request) {
		List<HashMap<String, Object>> missions = null;
		if (keyWord != null && keyWord != "") {
			missions = missionService.getMissionByKeyWord("%" + keyWord + "%",
					startIndex, endIndex,latitude,longitude);
		} else if (userId != null) {
			
			userId = (Integer) request.getSession().getAttribute("user_id");
			List<Integer> list = new ArrayList<Integer>();
			if (missionStatus == null) {
				missions = missionService.selectLimitedbyUserId(userId,
						startIndex, endIndex);
			} else if(missionStatus == 1){
				list.add(1);
				list.add(2);
				
				missions = missionService.selectLimitedbyUserIdAndStatus(
						userId,  list, startIndex, endIndex);
			} else if(missionStatus == 2){
				list.add(3);
				list.add(4);
				missions = missionService.selectLimitedbyUserIdAndStatus(
						userId,  list, startIndex, endIndex);
			} else if(missionStatus == 3){
				list.add(0);
				missions = missionService.selectLimitedbyUserIdAndStatus(
						userId,  list, startIndex, endIndex);
			} else if(missionStatus == 4){
				list.add(5);
				missions = missionService.selectLimitedbyUserIdAndStatus(
						userId, list, startIndex, endIndex);
			} 
		} else {
			
			//取出的任务： 已支付，被选择的执行人 还小于需要的人数
			missions = missionService.getMissionLimited(startIndex, endIndex,latitude,longitude,type);
		}
		ObjectMapper mapper = new ObjectMapper();
		String jsonfromList = "";
		try {
			jsonfromList = mapper.writeValueAsString(missions);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jsonfromList = "{}";
		}
		System.out.println(jsonfromList);
		return jsonfromList;
	}

	/**
	 * 查询任务详情
	 * 
	 * @param missionId
	 *            任务id
	 * @param request
	 * @return json
	 */
	@RequestMapping(value = "/missionDetails", method = RequestMethod.POST)
	public String getmissionDetails(
			@RequestParam(value = "missionId", required = true) Integer missionId,
			HttpServletRequest request) {
		HashMap<String, String> missions = null;
		missions = missionService.selectForMissionDetails(missionId);
		ObjectMapper mapper = new ObjectMapper();
		String jsonfromList = "";
		try {
			jsonfromList = mapper.writeValueAsString(missions);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jsonfromList = "{}";
		}
		return jsonfromList;

	}

	
	//退款
	@RequestMapping(value = "missionOffShelf/{missionId}", produces = "text/html;charset=UTF-8;")
	@ResponseBody
	public String missionOffShelf(
			@PathVariable(value = "missionId") Integer missionId,
			HttpServletRequest request) {
		// 1判断是否本人操作
		// 2申请退款的条件：任务已支付且未执行
		// 3改变任务状态
		// 4退款
		Integer userId = missionService.selectUserIdByMissionId(missionId);
		Integer cuserId = (Integer) request.getSession().getAttribute("user_id");
		if (cuserId != null && cuserId == userId) {
			Mission mission = missionService.selectByPrimaryKey(missionId);
			mission.setMissionStatus(2);
			missionService.updateByPrimaryKey(mission);// 审核退款
			return "客服正在审核退款，如有需要请拨打客服电话";
		}
		return "系统异常，请重试";
	}
	
	//取消退款
	@RequestMapping(value = "cancelOff/{missionId}", produces = "text/html;charset=UTF-8;")
	@ResponseBody
	public String cancelOff(
			@PathVariable(value = "missionId") Integer missionId,
			HttpServletRequest request) {
		Integer userId = missionService.selectUserIdByMissionId(missionId);
		Integer cuserId = (Integer) request.getSession().getAttribute("user_id");
		if (cuserId != null && cuserId == userId) {
			Mission mission = missionService.selectByPrimaryKey(missionId);
			mission.setMissionStatus(1);
			missionService.updateByPrimaryKey(mission);// 审核退款
			return "已取消退款";
		}
		return "系统异常，请重试";
	}
	
	//取消任务
	@RequestMapping(value = "missionCancel/{missionId}", produces = "text/html;charset=UTF-8;")
	@ResponseBody
	public String missionCancel(
			@PathVariable(value = "missionId") Integer missionId,
			HttpServletRequest request) {
		Integer userId = missionService.selectUserIdByMissionId(missionId);
		Integer cuserId = (Integer) request.getSession().getAttribute("user_id");
		if (cuserId != null && cuserId == userId) {
			Mission mission = missionService.selectByPrimaryKey(missionId);
			mission.setMissionStatus(4);
			missionService.updateByPrimaryKey(mission);// 审核退款
			return "已取消任务";
		}
		return "系统异常，请重试";

	}
	
	//完成任务
	@RequestMapping(value = "missionDone/{missionId}", produces = "text/html;charset=UTF-8;")
	@ResponseBody
	public String missionDone(
			@PathVariable(value = "missionId") Integer missionId,
			HttpServletRequest request) {
		// 1判断是否本人操作
		// 2改变任务状态
		Integer userId = missionService.selectUserIdByMissionId(missionId);
		Integer cuserId = (Integer) request.getSession().getAttribute("user_id");
		if (cuserId != null && cuserId == userId) {
			Mission mission = missionService.selectByPrimaryKey(missionId);
			mission.setMissionStatus(5);
			missionService.updateByPrimaryKey(mission);// 下架
			return "任务完成";
		}
		return "系统异常，请重试";

	}


	@RequestMapping(value = "/task-info/{missionId}")
	public ModelAndView taskInfo(@PathVariable("missionId") Integer missionId,
			ModelAndView mv,HttpServletRequest request) {
		Integer user_id = (Integer) request.getSession().getAttribute("user_id");
		
		HashMap<String, String> mdetails = null;
		mdetails = missionService.selectForMissionDetails(missionId);
		User user = userService.getUserById(user_id);
		
		System.out.println(mdetails);
		mv.addObject("mdetails", mdetails);
		mv.addObject("user", user);
		mv.setViewName("task-info");
		
		
		//浏览量
		missionService.viewCount(missionId);
		
		return mv;
	}

	
	@RequestMapping(value = "/orderDetail/{applyId}")
	public ModelAndView getorderDetailsByOrderId(
			@PathVariable(value = "applyId") Integer applyId, ModelAndView mv) {
		List<HashMap<String, String>> oulist = null;
	//	oulist = applyService.selectorderDetailsByApplyId(applyId);
		mv.setViewName("myservice-task-info");
		if (oulist.size() > 0) {
			mv.addObject("ou", oulist.get(0));
		}else{
			mv.addObject("ou", null);
		}
		return mv;
	}
	
	@RequestMapping(value = "/mysend-task-info/{missionId}")
	public ModelAndView mysendTaskInfo(@PathVariable("missionId") Integer missionId,
			ModelAndView mv,HttpServletRequest request) {
		Integer user_id = (Integer) request.getSession().getAttribute("user_id");
		
		HashMap<String, String> mdetails = null;
		mdetails = missionService.selectForMissionDetails(missionId);
		User user = userService.getUserById(user_id);
		
		System.out.println(mdetails);
		mv.addObject("mdetails", mdetails);
		mv.addObject("user", user);
		mv.setViewName("mysend-task-info");
		
		
		return mv;
	}
	
	@RequestMapping(value = "/more-info/{mid}")
	public ModelAndView moreMinfo(@PathVariable(value = "mid") Integer mid,
			ModelAndView mv) {
		mv.addObject("mid", mid);
		mv.setViewName("more-info");
		return mv;
	}
	
	
	//发布者对执行者评价
	@RequestMapping(value = "/evaluate_publish/{missionId}")
	public ModelAndView evaluate(@PathVariable("missionId") Integer missionId,
			ModelAndView mv,HttpServletRequest request) {
		Integer user_id = (Integer) request.getSession().getAttribute("user_id");//发布者id
		Evaluate evaluate = null;
		evaluate = evaluateService.selectByMissionId(missionId);
		if(evaluate == null){
			//生成对象
			evaluate = new Evaluate();
			evaluate.setPublishId(user_id);
			
			evaluate.setMissionId(missionId);
			Integer publishId = missionService.getUserIdByMissionId(missionId);
			//evaluate.setExecutorId();
			
			evaluateService.insertEvaluate(evaluate);
		}
		
		
		User user = userService.getUserById(evaluate.getPublishId());
		
		mv.addObject("evaluate", evaluate);
		mv.addObject("user", user);
		
		mv.setViewName("evaluate");
		
		
		return mv;
	}
	
	
	@RequestMapping(value = "/addEvaluate", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addEvaluate(
			@RequestParam(value = "content", required = true) String content,
			@RequestParam(value = "missionId", required = true) Integer missionId,
			@RequestParam(value = "score", required = true) Float score,
			HttpServletRequest request) {
		JSONObject json = new JSONObject();
		Integer user_id = (Integer) request.getSession()
				.getAttribute("user_id");
		
		if(user_id == null || user_id == 0){
			json.put("result", "error");
			json.put("msg", "系统出错了");
			return json;
			
		}
		
		if(score == null || score == 0){
			json.put("result", "error");
			json.put("msg", "分数不能为0");
			return json;
			
		}
		
		Evaluate evaluate = new Evaluate();
		
		evaluate.setEpevaluateTime(new Date());
		evaluate.setExecutorEvaluate(content);
		evaluate.setExecutorScore(score);
		evaluate.setMissionId(missionId);
		
		//更新评价表
		
		
		
	

		if (evaluateService.updateExecutorByMissionId(evaluate)) {
			json.put("result", "success");
			json.put("msg", "评价成功");
			
			return json;
		}


		
		
		json.put("result", "error");
		json.put("msg", "评价失败");

		return json;
	}
}
