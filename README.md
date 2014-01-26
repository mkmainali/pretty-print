# Introduction
Utility class for pretty printing table data. This class supports printing via annotations.

# How to use
Straightforward to use. You can initialize the printer yourself or use the builder. Default PrintStream is the
System.out.

```java
PrettyTablePrinter printer = new PrettyTablePrinter();
```
or

```java
PrettyTablePrinter builder = new PrettyTablePrinter.PrettyTablePrinterBuilder()
                .setAlign(PrettyTablePrinter.Align.CENTER)
                .disableRowSeparator()
                .build();
```

# Examples

## Default output

```java
String[] headers = {"A", "B", "C", "D"};
String[][] data = {{"A1", "B1", "C1", "D1"}, {"A2", "B2", "C2", "D2"}, {"A3", "B3", "C3", "D3"}};

PrettyTablePrinter printer = new PrettyTablePrinter();
printer.print(headers, data);
```

output:
```
+------------+------------+------------+------------+
|  A         |  B         |  C         |  D         |
+------------+------------+------------+------------+
|  A1        |  B1        |  C1        |  D1        |
|  A2        |  B2        |  C2        |  D2        |
|  A3        |  B3        |  C3        |  D3        |
+------------+------------+------------+------------+
```

## center aligned
```
+------------+------------+------------+------------+
|     A      |     B      |     C      |     D      |
+------------+------------+------------+------------+
|     A1     |     B1     |     C1     |     D1     |
|     A2     |     B2     |     C2     |     D2     |
|     A3     |     B3     |     C3     |     D3     |
+------------+------------+------------+------------+
```

## right aligned
```
+------------+------------+------------+------------+
|         A  |         B  |         C  |         D  |
+------------+------------+------------+------------+
|        A1  |        B1  |        C1  |        D1  |
|        A2  |        B2  |        C2  |        D2  |
|        A3  |        B3  |        C3  |        D3  |
+------------+------------+------------+------------+
```

## Multi row spanning column
```
+------------+--------------------+------------+------------+
|     A      |         B          |     C      |     D      |
+------------+--------------------+------------+------------+
|     A1     |  This is a very l  |     C1     |     D1     |
|            |  ong column spann  |            |            |
|            |  ing multiple row  |            |            |
|     A2     |         B2         |     C2     |     D2     |
|     A3     |         B3         |     C3     |     D3     |
+------------+--------------------+------------+------------+
```

## Printing annotated objects

Suppose we have a following class definition
```java
public static class Row {

        @PrettyPrint
        private String title;

        @PrettyPrint(header = "view")
        private int count;

        private String name;

        public Row(String title, String name, int count) {
            this.title = title;
            this.name = name;
            this.count = count;
        }
    }
```

Table can be printed using above as follows:

```java
List<Row> rows = new ArrayList<Row>(3);
for (int i = 0; i < 3; i++) {
    rows.add(new Row("R" + i, "N" + i, i));
}

printer.print(rows);
```

Output:

```
+------------+------------+
|   title    |    view    |
+------------+------------+
|     R0     |     0      |
|     R1     |     1      |
|     R2     |     2      |
+------------+------------+
```

#### Notes:
..* Only annotated fields will be printed
..* Defined header value will be used as header if available. If header value is not defined, then the field name will be used as the header.
..* null value will be printed as empty string
..* If the field is object, it should have a proper toString(), otherwise you will be seeing hash code printed

# Limitations
..* Printing column value spanning multiple row is supported. However, it does not do any smart way of dividing data into multiple rows. (maybe in future)
..* Does not support vertical alignment


