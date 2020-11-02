package com.dcampus.weblib.util.excelAdaptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 进行excel文件的读取帮助类
 * 
 * @author zim
 * 
 */
public class SheetReader implements IExcelReader{

	/** excel文件中的sheet **/
	private HSSFSheet sheet;

	/** 当前行 **/
	private int curRow = 0;




	/**
	 * 构造器
	 * 
	 * @param is
	 *            excel文件输入流
	 * @param sheetName
	 *            sheet名字
	 * @throws IOException
	 */
	public SheetReader(InputStream is, String sheetName) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook(is);
		this.sheet = wb.getSheet(sheetName);
	}

	public SheetReader(InputStream is) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook(is);
		this.sheet = wb.getSheetAt(0);
	}

	/**
	 * 读取一行，若行指针尚未移动，则将会把行指针移动到第一行进行读取<br>
	 * 但如果行指针已经移动，则需要通过nextRow进行行指针移动
	 * 
	 * @return list.toArray(new String[list.size()])
	 */
	public String[] readRow() {
		HSSFRow row = nextRow();
		if (row == null) {
			return new String[0];
		}

		List<String> list = new ArrayList<String>();
		for (int k = 0; k < row.getLastCellNum(); k++) {
			HSSFCell content = row.getCell(k);
			if (content == null) {
				list.add(null);
			} else {
				list
						.add(content.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ? String
								.valueOf((int)content.getNumericCellValue())
								: content.getRichStringCellValue().getString());
			}
		}

		return list.toArray(new String[list.size()]);
	}

	/**
	 * 移动到下一行
	 * 
	 * @return sheet.getRow(curRow++)
	 */
	public HSSFRow nextRow() {
		if (sheet == null) {
			return null;
		}
		return sheet.getRow(curRow++);
	}

	public int getCurRow() {
		return curRow;
	}

}
