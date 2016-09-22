package com.cn.ucoon.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cn.ucoon.dao.BalanceMapper;
import com.cn.ucoon.pojo.Balance;
import com.cn.ucoon.service.BalanceService;

@Service
@Transactional
public class BalanceServiceImpl implements BalanceService {

	@Autowired
	private BalanceMapper balanceMapper;
	
	
	@Override
	public BigDecimal countBalance(Integer userId) {
		
		BigDecimal total = new BigDecimal("0.00");
		
		List<Balance> lists = balanceMapper.selectByUserId(userId);
		for (int i = 0; i < lists.size(); i++) {
			String C = lists.get(i).getPlusOrMinus();
			
			if(C.equals("plus")){
				total = total.add(lists.get(i).getQuantity());
				
			}else{
				
				total = total.subtract(lists.get(i).getQuantity());
			}
		}
		
		
		return total;
	}

}
