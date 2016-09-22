<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html>
<html>
<head>
<base href="<%=basePath%>">

<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
<title>任务大厅</title>
<script src="js/jquery-2.1.4.min.js"></script>
<script src="js/mui.min.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<link href="css/mui.min.css" rel="stylesheet" />
<link href="css/style.css" rel="stylesheet" />
<link href="css/iconfont.css" rel="stylesheet" />
</head>
<script type="text/javascript">
	var currentPage = 0;
	var onePageNums = 10;
	var latitude = "";
	var longitude = "";
	var URL = window.location.href.split('#')[0]; //获取当前页面的url
	URL = encodeURIComponent(URL);
	var appid,nonceStr,signature,timestamp;
	//ajax同步更新全局变量，异步无法更新
	$.ajax({
	    url: "/ucoon/wx/sign?url="+URL,
	    success: function(result){
	    	appid = result.appId;
	    	timestamp=result.timestamp;
	    	nonceStr=result.nonceStr;
	    	signature=result.signature;
	    },
	  	dataType: "json",
	  	async:false
	});
	
	wx.config({
	    debug: false,
	    appId: appid,
	    timestamp: timestamp,
	    nonceStr: nonceStr,
	    signature: signature,
	    jsApiList: [
	      'checkJsApi',
	      'openLocation',
	      'getLocation'
	    ]
	});
	wx.ready(function(){
	
		wx.getLocation({
		    type: 'wgs84', // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
		    success: function (res) {
		        latitude = res.latitude; // 纬度，浮点数，范围为90 ~ -90
		        longitude = res.longitude; // 经度，浮点数，范围为180 ~ -180。
		        var speed = res.speed; // 速度，以米/每秒计
		        var accuracy = res.accuracy; // 位置精度
		       	setTimeout(loaddata(currentPage * onePageNums, (currentPage + 1) * onePageNums - 1, '', true,'all'),1000);
		    }
		});
	});
	$(document).ready(
			function() {
			
				
				$(".mui-input-clear").bind(
						"keyup",
						function(e) {
								initIndex();
								loaddata(currentPage * onePageNums,
										(currentPage + 1) * onePageNums - 1,
										this.value, true,'all');
						})
				$(".clearfix").bind(
						"tap",
						function() {
							window.location.href = "mission/task-info/"
									+ $(this).attr("data-m");
						})
						
						
				
				$('.task-select-title-col').on('tap', function() {
					var li = this;
					var classList = li.classList;
					if (!classList.contains('title-active')) {
						var active = li.parentNode.querySelector('.title-active');
						active.classList.remove('title-active');
						classList.add('title-active');
						
					}
					initIndex();
					//加载数据
					if(classList.contains('all')){
						loaddata(currentPage * onePageNums,
										(currentPage + 1) * onePageNums - 1,
										this.value, true,'all');
					} else{
						loaddata(currentPage * onePageNums,
										(currentPage + 1) * onePageNums - 1,
										this.value, true,'nearby');
					}
				});

			})
	function initIndex() {
		currentPage = 0;
		onePageNums = 10;
	}
	function loaddata(startIndex, endIndex, keyWord, clearable,type) {
		$
				.ajax({
					url : 'mission/getMissionsLimited',
					data : {
						startIndex : startIndex,
						endIndex : endIndex,
						keyWord : keyWord,
						latitude : latitude,
						longitude : longitude,
						type : type
					},
					async : false,
					type : 'post',
					dataType : 'json',
					success : function(data) {
						if (clearable == true) {

							$(".task").empty();
						}
						for (var i = 0; i < data.length; i++) {
							$(".task")
									.append(
											"<li class='task-col clearfix' data-m='"+data[i].mission_id+"'><a href=' '> <img"
									+" class='mui-pull-left' src='"+data[i].head_img_url+"'>"
													+ "<div class='task-price mui-pull-right'>"
													+ "<i class='mui-icon iconfont icon-qian'></i> <span"
											+"class='task-price-num'>"
													+ data[i].mission_price
													+ "</span>"
													+ "</div>"
													+ "<div class='task-detail'>"
													+ "	<p class='task-title'>"
													+ data[i].mission_title
													+ "</p>"
													+ "<p class='task-description'>"
													+ data[i].mission_describe
													+ "</p>"
													+ "<div class='time-add clearfix'>"
													+ "	<div class='mui-pull-left time-add-content'>"
													+ "		<i class='mui-icon iconfont icon-time'></i><span"
											+"		class='add-task-time'>"
													+ getDateDiff(data[i].publish_time)
													+ "</span>"
													+ "		</div>"
													+ "		<div class='mui-pull-left'>"
													+ "			<i class='mui-icon mui-icon-location'></i><span"
										+"			class='distance'>" +   data[i].distance + "</span>"
													+ "		</div>"
													+ "	</div>"
													+ "</div>" + "</a></li> ");

						}

					}
				})
	}
	function getMonthDay(timestamp) {
		var date = new Date(timestamp);
		month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) + "月"
				: date.getMonth() + 1 + "月";
		day = date.getDate() + 1 < 10 ? "0" + date.getDate() + "日" : date
				.getDate()
				+ "日";

		return month + day;
	}
	
	
	function getDateDiff (dateStr) {
	    var publishTime = dateStr/1000,
	        d_seconds,
	        d_minutes,
	        d_hours,
	        d_days,
	        timeNow = parseInt(new Date().getTime()/1000),
	        d,
	
	        date = new Date(publishTime*1000),
	        Y = date.getFullYear(),
	        M = date.getMonth() + 1,
	        D = date.getDate(),
	        H = date.getHours(),
	        m = date.getMinutes(),
	        s = date.getSeconds();
	        //小于10的在前面补0
	        if (M < 10) {
	            M = '0' + M;
	        }
	        if (D < 10) {
	            D = '0' + D;
	        }
	        if (H < 10) {
	            H = '0' + H;
	        }
	        if (m < 10) {
	            m = '0' + m;
	        }
	        if (s < 10) {
	            s = '0' + s;
	        }
	
	    d = timeNow - publishTime;
	    d_days = parseInt(d/86400);
	    d_hours = parseInt(d/3600);
	    d_minutes = parseInt(d/60);
	    d_seconds = parseInt(d);
	
	    if(d_days > 0 && d_days < 3){
	        return d_days + '天前';
	    }else if(d_days <= 0 && d_hours > 0){
	        return d_hours + '小时前';
	    }else if(d_hours <= 0 && d_minutes > 0){
	        return d_minutes + '分钟前';
	    }else if (d_seconds < 60) {
	        if (d_seconds <= 0) {
	            return '刚刚发表';
	        }else {
	            return d_seconds + '秒前';
	        }
	    }else if (d_days >= 3 && d_days < 30){
	        return M + '-' + D + '&nbsp;' + H + ':' + m;
	    }else if (d_days >= 30) {
	        return Y + '-' + M + '-' + D + '&nbsp;' + H + ':' + m + ':' + s;
	    }
	}   
</script>
<body>
	<div id="offCanvasWrapper"
		class="mui-off-canvas-wrap mui-draggable mui-scalable">
		<!--侧滑菜单部分-->
		<aside id="offCanvasSide"
			class="mui-off-canvas-right mui-transitioning">
			<div id="offCanvasSideScroll"
				class="mui-scroll-wrapper scroll-wrapper-bg">
				<div class="mui-scroll">
					<div class="basic-mes">
						<!--头像-->
						<img src="${user.headImgUrl}">
						<div class="ucoon-user">
							${user.nickName }
							<c:choose>
								 <c:when test="${user.sex == 2}">
								 <i class="mui-icon iconfont icon-woman"></i>
								 </c:when>
								 <c:otherwise>
									<i class="mui-icon iconfont icon-man"></i>
								 </c:otherwise>
							</c:choose>
						</div>
						<!--五星评分,默认5星-->
						<div class="user-score">
							<span class="fivestar"> <i
								class="mui-icon iconfont icon-star"></i> <i
								class="mui-icon iconfont icon-star"></i> <i
								class="mui-icon iconfont icon-star"></i> <i
								class="mui-icon iconfont icon-star-half"></i> 
								<i
								class="mui-icon iconfont icon-star-empty"></i>
							</span>
						</div>
						<!--个性签名-->
						<c:choose>
						 <c:when test="${!empty user.signature}">
						 <p class="user-talk">${user.signature }</p>
						 </c:when>
						 <c:otherwise>
						 <p class="user-talk">用一句话介绍自己吧:) 这里加个修改的图片</p>
						 </c:otherwise>
						 </c:choose>
						
						<!--财富情况-->
						<div class="treasure" id="wealth">
								<span class=""><i class="mui-icon iconfont icon-qian"></i>${balance }</span>
								<span class=""><i class="mui-icon iconfont icon-love"></i>${credits }</span>
						</div>
						<!--侧滑菜单列表-->
						<ul class="aside-menu">
							<li id="wode"><i class="mui-icon iconfont icon-wode"></i>我的信息</li>
							<li id="mysend"><i class="mui-icon iconfont icon-plane"></i>我发布的</li>
							<li id="myservice"><i class="mui-icon iconfont icon-service"></i>我服务的</li>
							<li id="chat-list"><i class="mui-icon iconfont icon-service"></i>消息中心</li>
							<li id="help"><i class="mui-icon mui-icon-help"></i>帮助联系</li>
							<li id="info"><i class="mui-icon mui-icon-info"></i>关于我们</li>
						</ul>
						<script type="text/javascript">
							$('#wealth').bind('tap',function(){
								window.location.href="wealth/";
							})
							
							$('#wode').bind('tap',function(){
								window.location.href="";
							})
							$('#mysend').bind('tap',function(){
								window.location.href="mysend";
							})
							$('#myservice').bind('tap',function(){
								window.location.href="myservice";
							})
							
							$('#chat-list').bind('tap',function(){
								window.location.href="chat/chat-list";
							})
							
							$('#help').bind('tap',function(){
								window.location.href="feedback";
							})
							
							$('#wode').bind('tap',function(){
								window.location.href="";
							})
							
						</script>
					</div>
				</div>
			</div>
		</aside>
		<!--主界面部分-->
		<div class="mui-inner-wrap mui-transitioning">
			<!--顶部导航栏-->
			<header class="mui-bar mui-bar-nav " id="header">
				<!--侧滑按钮-->
				<a id="offCanvasBtn" href="#offCanvasSide"
					class="mui-icon mui-action-menu mui-icon-bars mui-pull-right "></a>
				<!--顶部搜索框-->
				<!-- onkeyup="initIndex();loaddata(currentPage*onePageNums,(currentPage+1)*onePageNums-1,this.value,true);" -->
				<div class="mui-input-row mui-search ">
					<input type="search" class="mui-input-clear" placeholder="搜索内容、地点">
				</div>
			</header>
			<!--底部导航菜-->
			<nav class="mui-bar mui-bar-tab" id="nav-tap-bar">
				<a class="mui-tab-item mui-active" id="ucoon-me" href="">
					<span class="tab-icon tab-me-cur"></span>
					<span class="tab-name mui-tab-label">我有空</span>
				</a>
				<a class="mui-tab-item" id="ucoon-we" href="we">
					<span class="tab-icon tab-we"></span>
					<span class="tab-name mui-tab-label">都有空</span>
				</a>
				<a class="mui-tab-item" id="ucoon-who" href="who-new">
					<span class="tab-icon tab-who"></span>
					<span class="tab-name mui-tab-label">谁有空</span>
				</a>
			</nav>
			
			<!--主界面中间区域-->
			<div id="offCanvasContentScroll"
				class="mui-content mui-scroll-wrapper">
				<div class="mui-scroll">
					<!--图片轮播-->
					<div id="slider" class="mui-slider">
						<div class="mui-slider-group mui-slider-loop">
							<!-- 额外增加的一个节点(循环轮播：第一个节点是最后一张轮播) -->
							<div class="mui-slider-item mui-slider-item-duplicate">
								<a href="#"><img src="images/home_pic2.jpg"></a>
							</div>
							<!-- 第一张 -->
							<div class="mui-slider-item">
								<a href="#"><img src="images/home_pic4.jpg"></a>
							</div>
							<!-- 第二张 -->
							<div class="mui-slider-item">
								<a href="#"><img src="images/home_pic3.jpg"></a>
							</div>
							<!-- 第三张 -->
							<div class="mui-slider-item">
								<a href="#"><img src="images/home_pic2.jpg"></a>
							</div>
							<!-- 第四张 -->
							<div class="mui-slider-item">
								<a href="#"><img src="images/home_pic4.jpg"></a>
							</div>
							<!-- 额外增加的一个节点(循环轮播：最后一个节点是第一张轮播) -->
							<div class="mui-slider-item mui-slider-item-duplicate">
								<a href="#"><img src="images/home_pic4.jpg"></a>
							</div>
						</div>
						<!--图片轮播导航点-->
						<div class="mui-slider-indicator">
							<div class="mui-indicator mui-active"></div>
							<div class="mui-indicator"></div>
							<div class="mui-indicator"></div>
							<div class="mui-indicator"></div>
						</div>
					</div>

					<!--主内容切换（全部&附近）-->
					<div class="task-select">
						<!--选择导航-->
						<div class="task-select-title">
							<div class="task-select-title-col mui-pull-left all">
								<span class="title-active">全部</span>
							</div>
							<div class="task-select-title-col mui-pull-left">
								<span>附近</span>
							</div>
						</div>
						<!--任务信息-->
						<ul class="task">
							<!-- <li class="task-col clearfix"><a href="#"> <img
									class="mui-pull-left" src="images/home_pic2.jpg">
									<div class="task-price mui-pull-right">
										<i class="mui-icon iconfont icon-qian"></i> <span
											class="task-price-num">50.0</span>
									</div>
									<div class="task-detail">
										<p class="task-title">求代课</p>
										<p class="task-description">周五上午1-2节建发楼201着玩手机就行，就发给及分工及分工和分工会覆盖点个名地方司法所的范德萨</p>
										<div class="time-add clearfix">
											<div class="mui-pull-left time-add-content">
												<i class="mui-icon iconfont icon-time"></i><span
													class="add-task-time">07月30日</span>
											</div>
											<div class="mui-pull-left">
												<i class="mui-icon mui-icon-location"></i><span
													class="distance">1.2公里</span>
											</div>
										</div>
									</div>
							</a></li>-->
						</ul>
					</div>
				</div>
			</div>
			<!-- off-canvas backdrop -->
			<div class="mui-off-canvas-backdrop"></div>
		</div>
	</div>


	<script>
		mui.init({
			pullRefresh: {
				container: '#offCanvasContentScroll',
				down: {
					callback: pulldownRefresh
				},
				up: {
					contentrefresh: '正在加载...',
					callback: pullupRefresh
				}
			}
		});
		
		var count = 0;
			/**
			 * 下拉刷新具体业务实现
			 */
			function pulldownRefresh() {
				setTimeout(function() {

//					var table = document.body.querySelector('.mui-table-view');

//					var cells = document.body.querySelectorAll('.mui-table-view-cell');

//					for (var i = cells.length, len = i + 3; i < len; i++) {

//						var li = document.createElement('li');

//						li.className = 'mui-table-view-cell';

//						li.innerHTML = '<a class="mui-navigate-right">Item ' + (i + 1) + '</a>';

//						//下拉刷新，新纪录插到最前面；

//						table.insertBefore(li, table.firstChild);

//					}

					mui('#offCanvasContentScroll').pullRefresh().endPulldownToRefresh(); //refresh completed

				}, 1500);
			}
			
			/**
			 * 上拉加载具体业务实现
			 */
			function pullupRefresh() {
				setTimeout(function() {
					mui('#offCanvasContentScroll').pullRefresh().endPullupToRefresh((++count > 2)); //参数为true代表没有更多数据了。
//					var table = document.body.querySelector('.mui-table-view');

//					var cells = document.body.querySelectorAll('.mui-table-view-cell');

//					for (var i = cells.length, len = i + 20; i < len; i++) {

//						var li = document.createElement('li');

//						li.className = 'mui-table-view-cell';

//						li.innerHTML = '<a class="mui-navigate-right">Item ' + (i + 1) + '</a>';

//						table.appendChild(li);

//					}
				}, 1500);
			}
		//主界面和侧滑菜单界面均支持区域滚动；
		mui('#offCanvasSideScroll').scroll();
		mui('#offCanvasContentScroll').scroll();
		//实现ios平台原生侧滑关闭页面；
		if (mui.os.plus && mui.os.ios) {
			mui.plusReady(function() { //5+ iOS暂时无法屏蔽popGesture时传递touch事件，故该demo直接屏蔽popGesture功能
				plus.webview.currentWebview().setStyle({
					'popGesture' : 'none'
				});
			});
		}
		//图片轮播定时轮播 
		var slider = mui("#slider");
		slider.slider({
			interval : 5000
		});
		mui('#offCanvasSideScroll').scroll(function() {
			console.log('1');
		});
		mui('#offCanvasContentScroll').scroll(function() {
			console.log('1');
		});

		//页面跳转

		mui('#nav-tap-bar').on('tap', 'a', function() {
			var id = this.getAttribute('href');
			var href = this.href;
			mui.openWindow({
				url : href,
				id : id
			});
		});
	</script>

</body>

</html>