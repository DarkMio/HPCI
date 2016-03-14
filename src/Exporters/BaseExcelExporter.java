package Exporters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import piwik_interface.PiwikConnector;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Proof of concept excel exporter for any case of Piwik Output.
 * General approach, which should keep Data tight contained.
 */
public class BaseExcelExporter {

    private final PiwikConnector dataSource;
    private final Workbook wb;
    private JsonObject data;

    public BaseExcelExporter(PiwikConnector dataSource) {
        this.dataSource = dataSource;
        wb = new HSSFWorkbook();
    }

    public void export(File file) {

    }

    private void buildWorkbook() throws IOException { // always throw up(wards)
        data = dataSource.getData();
        for(Entry<String, JsonElement> topLevel : data.entrySet()) {
            createSheet(topLevel.getKey(), topLevel.getValue());
        }
    }

    private void createSheet(String title, JsonElement element) {
        Sheet page = wb.createSheet(title);
        Row topRow = page.createRow(0);
        topRow.createCell(0).setCellValue(title);
        writeData(page, element, 1, 0);
    }

    private void writeData(Sheet sheet, JsonElement element, int rowPointer, int cellPointer) {
        if(element.isJsonNull()) {
            writeJsonNull(sheet, element, rowPointer, cellPointer);
        } else if(element.isJsonPrimitive()) {
            writeJsonPrimitive(sheet, element, rowPointer, cellPointer);
        } else if(element.isJsonArray()) {
            writeJsonArray(sheet, element, rowPointer, cellPointer);
        } else if(element.isJsonObject()) {
            writeJsonObject(sheet, element, rowPointer, cellPointer);
        } else {
            throw new IllegalStateException("This exportable JSON Element is now exportable.");
        }
    }

    private void writeJsonNull(Sheet sheet, JsonElement element, int rowPointer, int cellPointer) {

    }

    private void writeJsonPrimitive(Sheet sheet, JsonElement element, int rowPointer, int cellPointer) {

    }

    private void writeJsonArray(Sheet sheet, JsonElement element, int rowPointer, int cellPointer) {

    }

    private void writeJsonObject(Sheet sheet, JsonElement element, int rowPointer, int cellPointer) {

    }
}
