package com.dcampus.weblib.util.adaptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.SheetWriter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;



public class XlsTokenDownloadUrlAdaptor {
	
	public static final String SheetName = "外链列表";

	public void exportDownloadUrl(OutputStream os,List<Long> ids,List<String> urls,List<String> codes,List<String> names,Integer[] setCode)throws GroupsException{
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(SheetName);
		SheetWriter writer = new SheetWriter(sheet);

		sheet.setColumnWidth(0, 70*256);
		sheet.setColumnWidth(1, 70*256);

		if (setCode == null) {
			setCode = new Integer[ids.size()];
			for (int ii = 0; ii < setCode.length; ii++) {
				setCode[ii] = 0;
			}
		}

		// 先写个标题
		writer.append("资源").append("链接").append("提取密码").nextRow();
		int count = ids.size();
		for(int i = 0;i<count;i++){
			Long _id = ids.get(i);
			String _name = names.get(i);
			String _url = urls.get(i);
			String _code = setCode[i]==1?codes.get(i):"";
			writer.append(_name).append(_url).append(_code).nextRow();
		}

		// 写入到输出流中
		try {
			workbook.write(os);
		} catch (IOException e) {
			throw new GroupsException(e);
		}
	}

}
