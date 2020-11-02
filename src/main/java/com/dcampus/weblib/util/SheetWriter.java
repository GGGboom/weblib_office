package com.dcampus.weblib.util;

import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 一个包装了HSSF的类，对excel进行读写
 *
 * @author zim
 *
 */
public class SheetWriter {
	private HSSFSheet sheet;

	private HSSFRow currentRow;

	private int rowNumber;

	private int cellNumber;

	public SheetWriter(HSSFSheet sheet) {
		this.sheet = sheet;
		this.rowNumber = 0;
		this.cellNumber = 0;
	}

	/**
	 * 添加cell到行
	 *
	 * @param text
	 * @return
	 */
	public SheetWriter append(String text) {
		// 还没进入行，则进入一行
		if (currentRow == null) {
			nextRow();
		}

		currentRow.createCell(cellNumber++).setCellValue(
			new HSSFRichTextString(text));

		return this;
	}

	/**
	 * 换下一行
	 *
	 * @return
	 */
	public SheetWriter nextRow() {
		currentRow = sheet.createRow(rowNumber++);
		cellNumber = 0;

		return this;
	}

	public static void main(String[] args) throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("xxx");
		SheetWriter writer = new SheetWriter(sheet);
		writer.append("aaaa").append("bbbb").nextRow().append("rrr");

		FileOutputStream fos = new FileOutputStream("F:/eee.xls");
		workbook.write(fos);
		fos.close();
	}

}
