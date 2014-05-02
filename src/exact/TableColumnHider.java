package exact;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class TableColumnHider {

    private JTable table;
    private TableColumnModel tcm;
    private Map<String, IndexedColumn> hidden =new HashMap<String, IndexedColumn>();

    public TableColumnHider(JTable table) {
        this.table = table;
        this.tcm = table.getColumnModel();
    }

    public void hide(String columnName) {
        int index = tcm.getColumnIndex(columnName);
        TableColumn column = tcm.getColumn(index);
        IndexedColumn ic = new IndexedColumn(index, column);
        if (hidden.put(columnName, ic) != null) {
            throw new IllegalArgumentException("Duplicate column name.");
        }
        tcm.removeColumn(column);
    }

    public void show(String columnName) {
        IndexedColumn ic = hidden.remove(columnName);
        if (ic != null) {
            tcm.addColumn(ic.column);
            int lastColumn = tcm.getColumnCount() - 1;
            if (ic.index < lastColumn) {
                tcm.moveColumn(lastColumn, ic.index);
            }
        }
    }

    private static class IndexedColumn {

        private Integer index;
        private TableColumn column;

        public IndexedColumn(Integer index, TableColumn column) {
            this.index = index;
            this.column = column;
        }
    }
}