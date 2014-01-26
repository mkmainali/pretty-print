package com.github.mkmainali.prettyprint;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

/**
 * Class for pretty printing a table data.
 *
 * @author mkmainali
 */
public class PrettyTablePrinter {

    public enum Align {
        LEFT,
        RIGHT,
        CENTER
    }

    private static final char DEFAULT_CORNER_MARKER = '+';

    private static final char DEFAULT_ROW_SEPARATOR = '-';

    private static final char DEFAULT_COLUMN_SEPARATOR = '|';

    private static final String DEFAULT_COLUMN_SPACE = getWhiteSpace(2);

    private static final int DEFAULT_MIN_COLUMN_WIDTH = 8;

    private static final int DEFAULT_MAX_COLUMN_WIDTH = 16;

    private PrintStream out;

    //marker used for each corner of the column
    private char cornerMarker = DEFAULT_CORNER_MARKER;
    //separator used for row
    private char rowSeparator = DEFAULT_ROW_SEPARATOR;
    //for separating each column
    private char columnSeparator = DEFAULT_COLUMN_SEPARATOR;
    private int minColumnWidth = DEFAULT_MIN_COLUMN_WIDTH;
    private int maxColumnWidth = DEFAULT_MAX_COLUMN_WIDTH;
    //empty column space on both left and right side of the column
    private String columnSpace = DEFAULT_COLUMN_SPACE;
    //alignment of data in each column.
    private Align align = Align.LEFT;

    public PrettyTablePrinter() {
        this(System.out);
    }

    public PrettyTablePrinter(PrintStream out) {
        this.out = out;
    }

    public void setAlign(Align align) {
        if (align == null) {
            throw new IllegalArgumentException("A non-null align is required");
        }
        this.align = align;
    }

    public void setCornerMarker(char cornerMarker) {
        if (Character.isWhitespace(cornerMarker)) {
            throw new IllegalArgumentException("A non-null corner marker is required");
        }
        this.cornerMarker = cornerMarker;
    }

    public void setRowSeparator(char rowSeparator) {
        if (Character.isWhitespace(rowSeparator)) {
            throw new IllegalArgumentException("A non-null/non-empty row separator is required");
        }
        this.rowSeparator = rowSeparator;
    }

    public void setColumnSeparator(char columnSeparator) {
        if (Character.isWhitespace(columnSeparator)) {
            throw new IllegalArgumentException("A non-null column separator is required");
        }
        this.columnSeparator = columnSeparator;
    }

    public void disableSeparators() {
        this.columnSeparator = ' ';
        this.cornerMarker = ' ';
        this.rowSeparator = ' ';
    }

    public void disableRowSeparator() {
        this.rowSeparator = ' ';
    }

    public void disableCornerMarker() {
        this.cornerMarker = ' ';
    }

    public void disableColumnSeparator() {
        this.columnSeparator = ' ';
    }

    public void setMinColumnWidth(int minColumnWidth) {
        if (minColumnWidth <= 0) {
            throw new IllegalArgumentException("Minimum column width must be greater than 0");
        }
        this.minColumnWidth = minColumnWidth;
    }

    public void setColumnSpace(int columnSpace) {
        if (columnSpace < 0) {
            throw new IllegalArgumentException("Column space must be a 0 or a positive integer");
        }
        this.columnSpace = getWhiteSpace(columnSpace);
    }

    public void setMaxColumnWidth(int maxColumnWidth) {
        if (maxColumnWidth < 0) {
            throw new IllegalArgumentException("Max column width must be a 0 or a positive integer");
        }
        if (maxColumnWidth < minColumnWidth) {
            throw new IllegalArgumentException("Max column width must be larger than min column width");
        }
        this.maxColumnWidth = maxColumnWidth;
    }

    public void setPrintStream(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("A non-null out stream is required");
        }
        this.out = out;
    }

    public void print(List<?> data) {
        if (data == null || data.size() == 0) return;
        List<Field> annotatedFields = getAnnotatedFields(data.get(0));
        String[] headers = extractHeaders(data.get(0), annotatedFields);
        String[][] printData = extractData(data, annotatedFields);
        print(headers, printData);
    }

    public void print(String[] headers, String[] row) {
        if (headers == null || headers.length == 0) return;
        if (row == null) return;
        doPrint(headers, new String[][]{row});
    }

    public void print(List<String> headers, List<List<String>> data) {
        if (headers == null || headers.size() == 0) return;
        if (data == null) return;

        String[] headersArray = headers.toArray(new String[headers.size()]);
        String[][] dataArray = new String[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            dataArray[i] = data.get(i).toArray(new String[data.get(i).size()]);
        }
        doPrint(headersArray, dataArray);
    }

    public void print(String[] headers, String[][] data) {
        if (headers == null || headers.length == 0) return;
        if (data == null) return;
        doPrint(headers, data);
    }

    private void doPrint(String[] headers, String[][] data) {
        if (headers.length == 0) return;

        //determine column widths
        int[] columnWidths = getColumnWidths(headers, data);
        //print the top level row separator
        printRowSeparator(columnWidths);
        //print headers
        printRow(headers, columnWidths);
        //print row separator that separates header and data
        printRowSeparator(columnWidths);
        //print the actual data
        for (String[] row : data) {
            printRow(row, columnWidths);
        }
        //print the row separator at the bottom
        printRowSeparator(columnWidths);
    }

    private void printRow(String[] data, int[] columnWidths) {

        //loop until printing data is complete. this is for printing column extending multiple columns
        while (data != null) {

            String[] tmp = null;
            for (int i = 0; i < data.length; i++) {
                out.print(columnSeparator);
                String columnData = data[i];//mappedData.get(i);
                //check if this column has any data left to be printed
                if (columnData != null) {
                    //print if all remaining data fits in this row, otherwise move remaining to the next row
                    if (columnData.length() > columnWidths[i]) {
                        printAligned(columnData.substring(0, columnWidths[i]), columnWidths[i]);
                        if (tmp == null) {
                            tmp = new String[data.length];
                        }
                        tmp[i] = columnData.substring(columnWidths[i], columnData.length());
                    } else {
                        printAligned(columnData, columnWidths[i]);
                    }
                } else {
                    printAligned("", columnWidths[i]);
                }
            }

            out.print(columnSeparator);
            out.println();
            data = tmp;
        }
    }

    private void printAligned(String str, int width) {
        int diff = Math.abs(str.length() - width);
        out.print(columnSpace);
        switch (align) {
            case LEFT:
                out.print(str);
                out.print(getWhiteSpace(diff));
                break;
            case RIGHT:
                out.print(getWhiteSpace(diff));
                out.print(str);
                break;
            case CENTER:
                int left = diff / 2;
                int right = diff - left;
                out.print(getWhiteSpace(left));
                out.print(str);
                out.print(getWhiteSpace(right));
                break;
        }
        out.print(columnSpace);
    }

    private void printRowSeparator(int[] columnWidths) {
        for (int i = 0; i < columnWidths.length; i++) {
            //calculate column width considering column space on both ends
            int columnWidth = columnWidths[i] + 2 * columnSpace.length();

            for (int j = 0; j <= columnWidth; j++) {
                if (j == 0) out.print(cornerMarker);
                else if (i == (columnWidths[i] - 1) && j == columnWidth) {
                    out.print(cornerMarker);
                } else {
                    out.print(rowSeparator);
                }
            }
        }
        out.print(cornerMarker);
        out.println();
    }

    private int[] getColumnWidths(String[] headers, String[][] data) {
        int[] maxColumnWidths = new int[headers.length];

        for (int i = 0; i < maxColumnWidths.length; i++) {
            maxColumnWidths[i] = Math.max(minColumnWidth, headers[i].length());
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                maxColumnWidths[j] = Math.max(data[i][j].length(), maxColumnWidths[j]);
            }
        }

        for (int i = 0; i < maxColumnWidths.length; i++) {
            maxColumnWidths[i] = Math.min(maxColumnWidths[i], maxColumnWidth);
        }

        return maxColumnWidths;
    }

    private static String getWhiteSpace(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private String[] extractHeaders(Object data, List<Field> annotatedFields) {
        String[] headers = new String[annotatedFields.size()];
        for (int i = 0; i < annotatedFields.size(); i++) {
            Field f = annotatedFields.get(i);
            String header = f.getAnnotation(PrettyPrint.class).header();
            headers[i] = StringUtils.isBlank(header) ? f.getName() : header;
        }
        return headers;
    }

    private List<Field> getAnnotatedFields(Object data) {
        final Field[] fields = data.getClass().getDeclaredFields();
        List<Field> tmp = new ArrayList<Field>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(PrettyPrint.class)) {
                tmp.add(f);
            }
        }
        return tmp;
    }

    private String[][] extractData(List<?> data, List<Field> annotatedFields) {
        String[][] printData = new String[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            Object rowData = data.get(i);
            Class<?> clazz = rowData.getClass();
            String[] row = new String[annotatedFields.size()];

            for (int j = 0; j < row.length; j++) {
                try {
                    //extract data from the annotated fields for printing.
                    Field dataField = clazz.getDeclaredField(annotatedFields.get(j).getName());
                    dataField.setAccessible(true);
                    Object extractedData = dataField.get(rowData);
                    row[j] = extractedData == null ? "" : extractedData.toString();
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            printData[i] = row;
        }
        return printData;
    }

    public static class PrettyTablePrinterBuilder {

        private final PrettyTablePrinter printer = new PrettyTablePrinter();

        public PrettyTablePrinter build() {
            return printer;
        }

        public PrettyTablePrinterBuilder setPrintStream(PrintStream out) {
            printer.setPrintStream(out);
            return this;
        }

        public PrettyTablePrinterBuilder setCornerMarker(char cornerMarker) {
            printer.setCornerMarker(cornerMarker);
            return this;
        }

        public PrettyTablePrinterBuilder setRowSeparator(char rowSeparator) {
            printer.setRowSeparator(rowSeparator);
            return this;
        }

        public PrettyTablePrinterBuilder setColumnSeparator(char columnSeparator) {
            printer.setColumnSeparator(columnSeparator);
            return this;
        }

        public PrettyTablePrinterBuilder setAlign(PrettyTablePrinter.Align align) {
            printer.setAlign(align);
            return this;
        }

        public PrettyTablePrinterBuilder setColumnSpace(int columnSpace) {
            printer.setColumnSpace(columnSpace);
            return this;
        }

        public PrettyTablePrinterBuilder setMinColumnWidth(int minColumnWidth) {
            printer.setMinColumnWidth(minColumnWidth);
            return this;
        }

        public PrettyTablePrinterBuilder setMaxColumnWidth(int maxColumnWidth) {
            printer.setMaxColumnWidth(maxColumnWidth);
            return this;
        }

        public PrettyTablePrinterBuilder disableSeparators() {
            printer.disableSeparators();
            return this;
        }

        public PrettyTablePrinterBuilder disableCornerMarker() {
            printer.disableCornerMarker();
            return this;
        }

        public PrettyTablePrinterBuilder disableColumnSeparator() {
            printer.disableColumnSeparator();
            return this;
        }

        public PrettyTablePrinterBuilder disableRowSeparator() {
            printer.disableRowSeparator();
            return this;
        }
    }
}
