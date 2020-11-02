package com.dcampus.weblib.web.action;

import java.sql.Timestamp;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dcampus.sys.service.UserService;
import com.dcampus.weblib.util.DateFormat;
import com.dcampus.common.paging.AndSearchTerm;
import com.dcampus.common.paging.DescSortItem;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchItem;
import com.dcampus.common.paging.SearchItem.Comparison;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.weblib.entity.DownloadLog;
import com.dcampus.weblib.entity.ErrorLog;
import com.dcampus.weblib.entity.LoginLog;
import com.dcampus.weblib.entity.OperateLog;
import com.dcampus.weblib.entity.UploadLog;
import com.dcampus.weblib.service.LogService;
import com.dcampus.weblib.entity.keys.LogSearchItemKey;
import com.dcampus.weblib.entity.keys.LogSortItemKey;
import com.dcampus.weblib.service.ApplicationService;
import com.dcampus.weblib.service.CategoryService;
import com.dcampus.weblib.service.GroupService;
import com.dcampus.weblib.service.GrouperService;
import com.dcampus.weblib.service.PermissionService;
import com.dcampus.weblib.service.ResourceService;
import com.dcampus.weblib.util.Filter;

@Controller
@RequestMapping(value = "/log")
public class LogController extends BaseController {

	@Autowired
	private LogService logService;

	
	/**
	 * 获取最近下载日志
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getLatestDownloadLogs", produces = "application/json; charset=UTF-8")
	public String getLatestDownloadLogs(Integer start, Integer limit){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<DownloadLog> downloadLogs = logService.getLatestDownloadLogs(start, limit);
		long count = logService.getDownloadLogTotalCount();
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"downloadLogs\":[");
	    for(int i=0;i<downloadLogs.size();i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(downloadLogs.get(i).getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(downloadLogs.get(i).getMemberName()).append("\",");
			if(downloadLogs.get(i).getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getName() == null? "": downloadLogs.get(i).getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getCompany() == null? "": downloadLogs.get(i).getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getDepartment() == null? "": downloadLogs.get(i).getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getEmail() == null? "": downloadLogs.get(i).getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getMobile() == null? "": downloadLogs.get(i).getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getIm() == null? "": downloadLogs.get(i).getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getUser().getPosition() == null? "": downloadLogs.get(i).getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(downloadLogs.get(i).getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(downloadLogs.get(i).getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(downloadLogs.get(i).getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(downloadLogs.get(i).getTargetObjectId()).append(",");
	    	sb.append("\"groupName\":\"").append(downloadLogs.get(i).getGroupName()).append("\",");
	    	sb.append("\"groupId\":").append(downloadLogs.get(i).getGroupId()).append(",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs.get(i).getTerminal()))).append("\"}");
	    	sb.append(",");
	    }
	    if(downloadLogs.size() > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 获取最近错误日志
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getLatestErrorLogs", produces = "application/json; charset=UTF-8")
	public String getLatestErrorLogs(Integer start, Integer limit){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<ErrorLog> errorLogs = logService.getLatestErrorLogs(start, limit);
		long count = logService.getErrorLogTotalCount();
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"errorLogs\":[");
	    for(int i=0;i<errorLogs.size();i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(errorLogs.get(i).getAccount()).append("\",");
	    	sb.append("\"detailType\":\"").append(errorLogs.get(i).getDetailType()).append("\",");
	    	sb.append("\"memberName\":\"").append(errorLogs.get(i).getMemberName()).append("\",");
			if(errorLogs.get(i).getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getName() == null? "": errorLogs.get(i).getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getCompany() == null? "": errorLogs.get(i).getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getDepartment() == null? "": errorLogs.get(i).getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getEmail() == null? "": errorLogs.get(i).getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getMobile() == null? "": errorLogs.get(i).getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getIm() == null? "": errorLogs.get(i).getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getUser().getPosition() == null? "": errorLogs.get(i).getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(errorLogs.get(i).getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(errorLogs.get(i).getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(errorLogs.get(i).getCreateDate())).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getTerminal()))).append("\",");
	    	String desd=JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getDescription()));
            System.out.println(desd);
   	    	if(desd.contains("\'"))
              desd=desd.replaceAll("\'","\"");
			sb.append("\"description\":\"").append(desd).append("\"}");

//	    	sb.append("\"description\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs.get(i).getDescription()))).append("\"}");
	    	sb.append(",");
	    }
	    if(errorLogs.size() > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 获取最近登录日志
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getLatestLoginLogs", produces = "application/json; charset=UTF-8")
	public String getLatestLoginLogs(Integer start, Integer limit){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<LoginLog> loginLogs = logService.getLatestLoginLogs(start, limit);
		long count = logService.getLoginLogTotalCount();
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"loginLogs\":[");
	    for(int i=0;i<loginLogs.size();i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(loginLogs.get(i).getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(loginLogs.get(i).getMemberName()).append("\",");
	    	sb.append("\"memberId\":").append(loginLogs.get(i).getMemberId()).append(",");
			if(loginLogs.get(i).getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getName() == null? "": loginLogs.get(i).getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getCompany() == null? "": loginLogs.get(i).getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getDepartment() == null? "": loginLogs.get(i).getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getEmail() == null? "": loginLogs.get(i).getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getMobile() == null? "": loginLogs.get(i).getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getIm() == null? "": loginLogs.get(i).getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getUser().getPosition() == null? "": loginLogs.get(i).getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"ip\":\"").append(loginLogs.get(i).getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(loginLogs.get(i).getCreateDate())).append("\",");
	    	sb.append("\"result\":\"").append(loginLogs.get(i).getResult()).append("\",");
	    	sb.append("\"reason\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getReason()))).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs.get(i).getTerminal()))).append("\"}");

	    	sb.append(",");
	    }
	    if(loginLogs.size() > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 获取最近操作日志
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getLatestOperateLogs", produces = "application/json; charset=UTF-8")
	public String getLatestOperateLogs(Integer start, Integer limit){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<OperateLog> operateLogs = logService.getLatestOperateLogs(start, limit);
		long count = logService.getOperateLogTotalCount();
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"operateLogs\":[");
	    for(int i=0;i<operateLogs.size();i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(operateLogs.get(i).getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(operateLogs.get(i).getMemberName()).append("\",");
			if(operateLogs.get(i).getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getName() == null? "": operateLogs.get(i).getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getCompany() == null? "": operateLogs.get(i).getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getDepartment() == null? "": operateLogs.get(i).getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getEmail() == null? "": operateLogs.get(i).getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getMobile() == null? "": operateLogs.get(i).getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getIm() == null? "": operateLogs.get(i).getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getUser().getPosition() == null? "": operateLogs.get(i).getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(operateLogs.get(i).getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(operateLogs.get(i).getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(operateLogs.get(i).getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(operateLogs.get(i).getTargetObjectId()).append(",");
	    	sb.append("\"action\":\"").append(operateLogs.get(i).getAction()).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getTerminal()))).append("\",");

			String desd=JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getDescription()));
			if(desd.contains("&rdquo;"))
				desd=desd.replaceAll("&rdquo;","\'");
			if(desd.contains("&ldquo;"))
				desd=desd.replaceAll("&ldquo;","\'");
			sb.append("\"description\":\"").append(desd).append("\"}");


//	    	sb.append("\"description\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs.get(i).getDescription()))).append("\"}");
	    	sb.append(",");
	    }
	    if(operateLogs.size() > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 获取最近上传日志
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/getLatestUploadLogs", produces = "application/json; charset=UTF-8")
	public String getLatestUploadLogs(Integer start, Integer limit){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		List<UploadLog> uploadLogs = logService.getLatestUploadLogs(start, limit);
		long count = logService.getUploadLogTotalCount();
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"uploadLogs\":[");
	    for(int i=0;i<uploadLogs.size();i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(uploadLogs.get(i).getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(uploadLogs.get(i).getMemberName()).append("\",");
			if(uploadLogs.get(i).getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getName() == null? "": uploadLogs.get(i).getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getCompany() == null? "": uploadLogs.get(i).getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getDepartment() == null? "": uploadLogs.get(i).getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getEmail() == null? "": uploadLogs.get(i).getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getMobile() == null? "": uploadLogs.get(i).getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getIm() == null? "": uploadLogs.get(i).getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getUser().getPosition() == null? "": uploadLogs.get(i).getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(uploadLogs.get(i).getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(uploadLogs.get(i).getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(uploadLogs.get(i).getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(HTML.escape(uploadLogs.get(i).getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(uploadLogs.get(i).getTargetObjectId()).append(",");
	    	sb.append("\"groupName\":\"").append(uploadLogs.get(i).getGroupName()).append("\",");
	    	sb.append("\"groupId\":").append(uploadLogs.get(i).getGroupId()).append(",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs.get(i).getTerminal()))).append("\"}");
	    	sb.append(",");
	    }
	    if(uploadLogs.size() > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}

	/**
	 * 搜索下载日志
	 * @param account
	 * @param action
	 * @param result
	 * @param createDate_begin
	 * @param createDate_end
	 * @param ip
	 * @param terminal
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchDownloadLogs", produces = "application/json; charset=UTF-8")
	public String searchDownloadLogs(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal, Integer start, Integer limit
			,String groupName, String memberName, String targetObject){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		SearchTerm searchTerm = this.generateAndSearchTerm(account, action, result, createDate_begin, createDate_end, ip, terminal,groupName, memberName, targetObject);
		SortTerm sortTerm = generateDescSortTerm();
		PageTerm pageTerm = generatePageTerm(start, limit);

		DownloadLog[] downloadLogs = logService.searchDownloadLogs(searchTerm, sortTerm, pageTerm);
		long count = logService.searchDownloadLogsCount(searchTerm);
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"downloadLogs\":[");
	    for(int i=0;i<downloadLogs.length;i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(downloadLogs[i].getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(downloadLogs[i].getMemberName()).append("\",");
			if(downloadLogs[i].getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getName() == null? "": downloadLogs[i].getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getCompany() == null? "": downloadLogs[i].getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getDepartment() == null? "": downloadLogs[i].getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getEmail() == null? "": downloadLogs[i].getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getMobile() == null? "": downloadLogs[i].getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getIm() == null? "": downloadLogs[i].getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getUser().getPosition() == null? "": downloadLogs[i].getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(downloadLogs[i].getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(downloadLogs[i].getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(downloadLogs[i].getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(downloadLogs[i].getTargetObjectId()).append(",");
	    	sb.append("\"groupName\":\"").append(downloadLogs[i].getGroupName()).append("\",");
	    	sb.append("\"groupId\":").append(downloadLogs[i].getGroupId()).append(",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(downloadLogs[i].getTerminal()))).append("\"}");
	    	sb.append(",");
	    }
	    if(downloadLogs.length > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 搜索错误日志
	 * @param account
	 * @param action
	 * @param result
	 * @param createDate_begin
	 * @param createDate_end
	 * @param ip
	 * @param terminal
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchErrorLogs", produces = "application/json; charset=UTF-8")
	public String searchErrorLogs(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal, Integer start, Integer limit
			,String groupName, String memberName, String targetObject){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		SearchTerm searchTerm = this.generateAndSearchTerm(account, action, result, createDate_begin, createDate_end, ip, terminal,groupName, memberName, targetObject);
		SortTerm sortTerm = generateDescSortTerm();
		PageTerm pageTerm = generatePageTerm(start, limit);

		ErrorLog[] errorLogs = logService.searchErrorLogs(searchTerm, sortTerm, pageTerm);
		long count = logService.searchErrorLogsCount(searchTerm);
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"errorLogs\":[");
	    for(int i=0;i<errorLogs.length;i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(errorLogs[i].getAccount()).append("\",");
	    	sb.append("\"detailType\":\"").append(errorLogs[i].getDetailType()).append("\",");
	    	sb.append("\"memberName\":\"").append(errorLogs[i].getMemberName()).append("\",");
			if(errorLogs[i].getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getName() == null? "": errorLogs[i].getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getCompany() == null? "": errorLogs[i].getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getDepartment() == null? "": errorLogs[i].getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getEmail() == null? "": errorLogs[i].getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getMobile() == null? "": errorLogs[i].getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getIm() == null? "": errorLogs[i].getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getUser().getPosition() == null? "": errorLogs[i].getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(errorLogs[i].getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(errorLogs[i].getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(errorLogs[i].getCreateDate())).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getTerminal()))).append("\",");

			String desd=JS.quote(Filter.convertHtmlBody(errorLogs[i].getDescription()));
			if(desd.contains("\'"))
				desd=desd.replaceAll("\'","\"");
			sb.append("\"description\":\"").append(desd).append("\"}");

//	    	sb.append("\"description\":\"").append(JS.quote(Filter.convertHtmlBody(errorLogs[i].getDescription()))).append("\"}");
	    	sb.append(",");
	    }
	    if(errorLogs.length > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 搜索登陆日志
	 * @param account
	 * @param action
	 * @param result
	 * @param createDate_begin
	 * @param createDate_end
	 * @param ip
	 * @param terminal
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchLoginLogs", produces = "application/json; charset=UTF-8")
	public String searchLoginLogs(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal, Integer start, Integer limit
			,String groupName, String memberName, String targetObject){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		
		SearchTerm searchTerm = this.generateAndSearchTerm(account, action, result, createDate_begin, createDate_end, ip, terminal,groupName, memberName, targetObject);

		SortTerm sortTerm = generateDescSortTerm();
		PageTerm pageTerm = generatePageTerm(start, limit);

		LoginLog[] loginLogs = logService.searchLoginLogs(searchTerm, sortTerm, pageTerm);
		long count = logService.searchLoginLogsCount(searchTerm);
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"loginLogs\":[");
	    for(int i=0;i<loginLogs.length;i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(loginLogs[i].getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(loginLogs[i].getMemberName()).append("\",");
			if(loginLogs[i].getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getName() == null? "": loginLogs[i].getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getCompany() == null? "": loginLogs[i].getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getDepartment() == null? "": loginLogs[i].getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getEmail() == null? "": loginLogs[i].getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getMobile() == null? "": loginLogs[i].getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getIm() == null? "": loginLogs[i].getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getUser().getPosition() == null? "": loginLogs[i].getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(loginLogs[i].getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(loginLogs[i].getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(loginLogs[i].getCreateDate())).append("\",");
	    	sb.append("\"result\":\"").append(loginLogs[i].getResult()).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(loginLogs[i].getTerminal()))).append("\"}");
	    	sb.append(",");
	    }
	    if(loginLogs.length > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 搜索操作日志
	 * @param account
	 * @param action
	 * @param result
	 * @param createDate_begin
	 * @param createDate_end
	 * @param ip
	 * @param terminal
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchOperateLogs", produces = "application/json; charset=UTF-8")
	public String searchOperateLogs(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal, Integer start, Integer limit
			,String groupName, String memberName, String targetObject){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		SearchTerm searchTerm = this.generateAndSearchTerm(account, action, result, createDate_begin, createDate_end, ip, terminal,groupName, memberName, targetObject);

		SortTerm sortTerm = generateDescSortTerm();
		PageTerm pageTerm = generatePageTerm(start,limit);

		OperateLog[] operateLogs = logService.searchOperateLogs(searchTerm, sortTerm, pageTerm);
		long count = logService.searchOperateLogsCount(searchTerm);
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"operateLogs\":[");
	    for(int i=0;i<operateLogs.length;i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(operateLogs[i].getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(operateLogs[i].getMemberName()).append("\",");
			if(operateLogs[i].getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getName() == null? "": operateLogs[i].getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getCompany() == null? "": operateLogs[i].getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getDepartment() == null? "": operateLogs[i].getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getEmail() == null? "": operateLogs[i].getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getMobile() == null? "": operateLogs[i].getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getIm() == null? "": operateLogs[i].getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getUser().getPosition() == null? "": operateLogs[i].getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(operateLogs[i].getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(operateLogs[i].getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(operateLogs[i].getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(operateLogs[i].getTargetObjectId()).append(",");
	    	sb.append("\"action\":\"").append(operateLogs[i].getAction()).append("\",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getTerminal()))).append("\",");
			String desd=JS.quote(Filter.convertHtmlBody(operateLogs[i].getDescription()));
			if(desd.contains("&rdquo;"))
				desd=desd.replaceAll("&rdquo;","\'");
			if(desd.contains("&ldquo;"))
				desd=desd.replaceAll("&ldquo;","\'");
			sb.append("\"description\":\"").append(desd).append("\"}");

//	    	sb.append("\"description\":\"").append(JS.quote(Filter.convertHtmlBody(operateLogs[i].getDescription()))).append("\"}");
	    	sb.append(",");
	    }
	    if(operateLogs.length > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 搜索上传日志
	 * @param account
	 * @param action
	 * @param result
	 * @param createDate_begin
	 * @param createDate_end  
	 * @param ip
	 * @param terminal
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/searchUploadLogs", produces = "application/json; charset=UTF-8")
	public String searchUploadLogs(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal, Integer start, Integer limit
			,String groupName, String memberName, String targetObject){
		start = start == null ? 0 : start;
		limit = limit == null ? Integer.MAX_VALUE : limit;
		SearchTerm searchTerm = this.generateAndSearchTerm(account, action, result, createDate_begin, createDate_end, ip, terminal,groupName, memberName, targetObject);

		SortTerm sortTerm = generateDescSortTerm();
		PageTerm pageTerm = generatePageTerm(start, limit);

		UploadLog[] uploadLogs = logService.searchUploadLogs(searchTerm, sortTerm, pageTerm);
		long count = logService.searchUploadLogsCount(searchTerm);
	    StringBuffer sb = new StringBuffer();
	    sb.append("{");
	    sb.append("\"count\":").append(count).append(",");
	    sb.append("\"uploadLogs\":[");
	    for(int i=0;i<uploadLogs.length;i++){
	    	sb.append("{");
	    	sb.append("\"account\":\"").append(uploadLogs[i].getAccount()).append("\",");
	    	sb.append("\"memberName\":\"").append(uploadLogs[i].getMemberName()).append("\",");
			if(uploadLogs[i].getUser()!=null){
				sb.append("\"name\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getName() == null? "": uploadLogs[i].getUser().getName()))).append("\",");
				sb.append("\"company\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getCompany() == null? "": uploadLogs[i].getUser().getCompany()))).append("\",");
				sb.append("\"department\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getDepartment() == null? "": uploadLogs[i].getUser().getDepartment()))).append("\",");
				sb.append("\"email\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getEmail() == null? "": uploadLogs[i].getUser().getEmail()))).append("\",");
				sb.append("\"mobile\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getMobile() == null? "": uploadLogs[i].getUser().getMobile()))).append("\",");
				sb.append("\"im\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getIm() == null? "": uploadLogs[i].getUser().getIm()))).append("\",");
				sb.append("\"position\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getUser().getPosition() == null? "": uploadLogs[i].getUser().getPosition()))).append("\",");

			}else{
				sb.append("\"name\":\"").append("\",");
				sb.append("\"company\":\"").append("\",");
				sb.append("\"department\":\"").append("\",");
				sb.append("\"email\":\"").append("\",");
				sb.append("\"mobile\":\"").append("\",");
				sb.append("\"im\":\"").append("\",");
				sb.append("\"position\":\"").append("\",");
			}
	    	sb.append("\"memberId\":").append(uploadLogs[i].getMemberId()).append(",");
	    	sb.append("\"ip\":\"").append(uploadLogs[i].getIp()).append("\",");
	    	sb.append("\"createDate\":\"").append(DateFormat.format(uploadLogs[i].getCreateDate())).append("\",");
	    	sb.append("\"targetObject\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getTargetObject()))).append("\",");
	    	sb.append("\"targetObjectId\":").append(uploadLogs[i].getTargetObjectId()).append(",");
	    	sb.append("\"groupName\":\"").append(uploadLogs[i].getGroupName()).append("\",");
	    	sb.append("\"groupId\":").append(uploadLogs[i].getGroupId()).append(",");
	    	sb.append("\"terminal\":\"").append(JS.quote(Filter.convertHtmlBody(uploadLogs[i].getTerminal()))).append("\"}");
	    	sb.append(",");
	    }
	    if(uploadLogs.length > 0){
	    	sb.setLength(sb.length() - 1);
	    }
	    sb.append("]");
	    sb.append("}");
	    return sb.toString();
	}
	
	/**
	 * 根据id删除下载日志
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteDownloadLogs", produces = "application/json; charset=UTF-8")
	public String deleteDownloadLogs(Long[] ids){
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				logService.deleteDownloadLog(id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据id删除错误日志
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteErrorLogs", produces = "application/json; charset=UTF-8")
	public String deleteErrorLogs(Long[] ids){
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				logService.deleteErrorLog(id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据id删除登陆日志
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteLoginLogs", produces = "application/json; charset=UTF-8")
	public String deleteLoginLogs(Long[] ids){
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				logService.deleteLoginLog(id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据id删除操作日志
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteOperateLogs", produces = "application/json; charset=UTF-8")
	public String deleteOperateLogs(Long[] ids){
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				logService.deleteOperateLog(id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	/**
	 * 根据id删除上传日志
	 * @param ids
	 * @return
	 */
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "/deleteUploadLogs", produces = "application/json; charset=UTF-8")
	public String deleteUploadLogs(Long[] ids){
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				logService.deleteUploadLog(id);
			}
		}
		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
	}
	
	private SearchTerm generateAndSearchTerm(String account, String action,String result, Long createDate_begin, Long createDate_end, String ip, String terminal,String groupName, String memberName, String targetObject){
		SearchTerm searchTerm = new AndSearchTerm();
		if(account != null && !account.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Account,Comparison.LK,account));
		}
		if(memberName != null && !memberName.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.MemberName,Comparison.LK,memberName));
		}
		if(targetObject != null && !targetObject.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.TargetObject,Comparison.LK,targetObject));
		}
		if(action != null && !action.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Action,Comparison.EQ,action));
		}
		if(result != null && !result.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Result,Comparison.EQ,result));
		}
		if(createDate_begin != null && createDate_begin != 0){
			searchTerm.add(new SearchItem<Timestamp>(LogSearchItemKey.CreateDate,Comparison.GE,new Timestamp(createDate_begin)));
		}
		if(createDate_end != null && createDate_end != 0){
			searchTerm.add(new SearchItem<Timestamp>(LogSearchItemKey.CreateDate,Comparison.LE,new Timestamp(createDate_end)));
		}
		if(ip != null && !ip.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Ip,Comparison.EQ,ip));
		}
		if(terminal != null && !terminal.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.Terminal,Comparison.LK,terminal));
		}
		if(groupName != null && !groupName.trim().equals("")){
			searchTerm.add(new SearchItem<String>(LogSearchItemKey.GroupName,Comparison.LK,groupName));
		}
		return searchTerm;
	}
	private SortTerm generateDescSortTerm(){
		SortTerm sortTerm = new SortTerm();
		sortTerm.add(new DescSortItem(LogSortItemKey.CreateDate));
		return sortTerm;
	}
	private PageTerm generatePageTerm(int start, int limit){
		PageTerm pageTerm = new PageTerm();
		pageTerm.setBeginIndex(start);
		pageTerm.setPageSize(limit);
		return pageTerm;
	}
}
