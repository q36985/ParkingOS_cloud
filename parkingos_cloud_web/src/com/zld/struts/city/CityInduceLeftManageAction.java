package com.zld.struts.city;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;

public class CityInduceLeftManageAction extends Action {
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select*from remain_berth_tb where state=?  and ";
			String countSql = "select count(*)from  remain_berth_tb  where state=?  and " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+") ";
				params.addAll(parks);
				
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by update_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
		
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select*from remain_berth_tb where state=?  and ";
			String countSql = "select count(*)from  remain_berth_tb  where state=?  and " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"remain_berth_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+")";
				params.addAll(parks);
				
				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}

				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by update_time desc ",params, pageNum, pageSize);
				}
			}
		  
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		
		}
		return null;
	}

}
	
