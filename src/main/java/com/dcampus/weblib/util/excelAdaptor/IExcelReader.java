package com.dcampus.weblib.util.excelAdaptor;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public interface IExcelReader {


    /**
     * 读取一行，若行指针尚未移动，则将会把行指针移动到第一行进行读取<br>
     * 但如果行指针已经移动，则需要通过nextRow进行行指针移动
     * @return
     */
    public String[] readRow();

    /**
     * 移动到下一行
     * @return
     */
    public Row nextRow();

    /**
     * 获得当前行
     * @return
     */
    public int getCurRow();
}
