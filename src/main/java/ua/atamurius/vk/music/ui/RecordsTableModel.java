package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;
import ua.atamurius.vk.music.Records;

import java.util.*;
import javax.swing.table.AbstractTableModel;

public class RecordsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
    private boolean isEditable = true;

    private final I18n l = new I18n(MainFrame.class);

    private final Records items;

    public RecordsTableModel(Records records) {
        this.items = records;
        this.items.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                fireTableDataChanged();
            }
        });
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public int getRowCount() {
        return items.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0: return l.l("table.column.no");
        case 1: return l.l("table.column.author");
        case 2: return l.l("table.column.title");
        }
        throw new IndexOutOfBoundsException(columnIndex +" not in [0,1]");
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Integer.class : String.class;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0: return (rowIndex + 1);
        case 1: return items.get(rowIndex).getAuthor();
        case 2: return items.get(rowIndex).getTitle();
        }
        throw new IndexOutOfBoundsException(columnIndex +" not in [0,1]");
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable && columnIndex > 0;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 1:
            items.get(rowIndex).setAuthor(aValue.toString());
            items.notifyObservers();
            break;
        case 2:
            items.get(rowIndex).setTitle(aValue.toString());
            items.notifyObservers();
            break;
        default:
            throw new IndexOutOfBoundsException(columnIndex +" not in [1,2]");
        }
    }
}
