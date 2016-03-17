package Exporters;

import com.google.gson.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import piwik_interface.PiwikConnector;
import piwik_interface.piwik_endpoints.PiwikTransitionsForAction;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * Proof of concept excel exporter for any case of Piwik Output.
 * General approach, which should keep Data tight contained.
 */
public class BaseExcelExporter {

    public static void main(String[] args) {
        PiwikConnector source = new PiwikConnector("", "", "") {
            @Override
            protected String[] getAPIEndpoint() {
                return null;
            }
            @Override
            protected HashMap<String, String> getParameters() {
                return null;
            }
            @Override
            protected HashMap<String, String> getPeriod() { return null; }
            @Override
            public JsonObject getData() throws MalformedURLException, IOException {
                final URL url = new URL("http://localhost/dump.json");
                final HttpURLConnection request = (HttpURLConnection) url.openConnection();
                final JsonParser jp = new JsonParser();
                final JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                return root.getAsJsonObject();
            }
        };

        BaseExcelExporter exporter = new BaseExcelExporter(source);
        try {
            File f = new File("test.xls");
            exporter.buildWorkbook();
            exporter.export(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final PiwikConnector dataSource;
    private final Workbook wb;
    private final CellStyle titleStyle;
    private JsonObject data;
    private int rowPointer = 1, columnPointer = 0;

    public BaseExcelExporter(PiwikConnector dataSource) {
        this.dataSource = dataSource;
        wb = new HSSFWorkbook();
        titleStyle = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 18);
        titleStyle.setFont(f);
    }

    public void export(File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
    }

    private void buildWorkbook() throws IOException { // always throw up(wards)
        data = dataSource.getData();
        for(Entry<String, JsonElement> topLevel : data.entrySet()) {
            rowPointer = 1;
            columnPointer = 0;
            createSheet(topLevel.getKey(), topLevel.getValue());
        }
    }

    private void createSheet(String title, JsonElement element) {
        Sheet page = wb.createSheet(title);
        Row topRow = page.createRow(0);
        Cell c = topRow.createCell(0);
        c.setCellValue(title);
        c.setCellStyle(titleStyle);
        writeData(page, element);
    }

    private void writeData(Sheet sheet, JsonElement element) {
        if(element.isJsonNull()) {
            writeJsonNull(sheet, element);
        } else if(element.isJsonPrimitive()) {
            writeJsonPrimitive(sheet, element);
        } else if(element.isJsonArray()) {
            writeJsonArray(sheet, element);
        } else if(element.isJsonObject()) {
            writeJsonObject(sheet, element);
        } else {
            throw new IllegalStateException("This exportable JSON Element is not exportable.");
        }
    }

    private void writeJsonNull(Sheet sheet, JsonElement element) {
        writeJsonPrimitive(sheet, new JsonPrimitive("null"));
    }

    private void writeJsonPrimitive(Sheet sheet, JsonElement element) {
        JsonPrimitive prim = element.getAsJsonPrimitive();

        Row r = sheet.getRow(rowPointer);
        if(r == null) {
            r = sheet.createRow(rowPointer);
        }
        Cell c = r.createCell(columnPointer);
        if(prim.isBoolean() || prim.isString()) {
            c.setCellValue(prim.getAsString());
        } else if (prim.isNumber()) {
            c.setCellType(Cell.CELL_TYPE_NUMERIC);
            c.setCellValue(prim.getAsNumber().doubleValue());
        } else {
            throw new NotImplementedException("Unexpected primitve JSON type.");
        }
    }

    private void writeJsonArray(Sheet sheet, JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        Iterator<JsonElement> iter = array.iterator();
        rowPointer = 0;
        for(int i = 0; iter.hasNext(); i++) {
            rowPointer++;
            columnPointer = 0;
            JsonObject single = iter.next().getAsJsonObject();

            if(i == 0) { // write description
                Row topRow = sheet.createRow(rowPointer++);
                for(Entry<String, JsonElement> descriptor: single.entrySet()) {
                    topRow.createCell(columnPointer++).setCellValue(descriptor.getKey());
                }
                columnPointer = 0;
            }
            Row row = sheet.createRow(rowPointer);
            for(Entry<String, JsonElement> dataEntry: single.entrySet()){
                JsonElement value = dataEntry.getValue();
                if(value.isJsonPrimitive()) {
                    /// / writeData(sheet, dataEntry.getValue(), rowPointer, cellOffset);
                    writeJsonPrimitive(sheet, value);
                    // row.createCell(columnPointer).setCellValue(dataEntry.getValue().toString());
                } else if (value.isJsonNull()) {
                    row.createCell(columnPointer);

                } else {
                    row.createCell(columnPointer).setCellValue("Error: Too much stacked data.");
                }
                columnPointer++;
                // System.out.println(dataEntry.getValue());
            }
        }
    }

    private void writeJsonObject(Sheet sheet, JsonElement element) {
        JsonObject jso = element.getAsJsonObject();
        Row titleRow = sheet.createRow(rowPointer++);

        for(Entry<String, JsonElement> single : jso.entrySet()) {
            titleRow.createCell(columnPointer).setCellValue(single.getKey());
            writeData(sheet, single.getValue());
            columnPointer++;
        }

        rowPointer += 2;
    }
}