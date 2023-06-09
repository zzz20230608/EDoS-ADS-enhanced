/*****************************************************************/
/* Copyright 2013 Code Strategies                                */
/* This code may be freely used and distributed in any project.  */
/* However, please do not remove this credit if you publish this */
/* code in paper or electronic form, such as on a web site.      */
/*****************************************************************/

package org.cloudbus.cloudsim.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.cloudbus.cloudsim.Log;

public class Excel {
	
	public static void ExcelWrite(String fileName, String sheetName, String[][] Data) {
		try {
			int rows = Data.length;
			int columns = Data[0].length;
			
			FileOutputStream fileOut = new FileOutputStream(fileName);
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet(sheetName);

			for(int i = 0; i < rows; i++)
			{
				HSSFRow row = worksheet.createRow(i);
				
				for(int j = 0; j < columns; j++)
				{
					HSSFCell cell = row.createCell(j);
					
					try
					{
						cell.setCellValue(Double.parseDouble(Data[i][j]));
					}
					catch(Exception e)
					{
						cell.setCellValue(Data[i][j]);
					} 
					
				}
			}

			workbook.write(fileOut);
			workbook.close();
			fileOut.flush();
			fileOut.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.printLine(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			
		}

		
	}

}