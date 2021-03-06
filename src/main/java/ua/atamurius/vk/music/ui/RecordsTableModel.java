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
        return 5;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0: return l.l("table.column.no");
        case 1: return l.l("table.column.author");
        case 2: return l.l("table.column.title");
        case 3: return l.l("table.column.album");
        case 4: return l.l("table.column.progress");
        }
        throw new IndexOutOfBoundsException(columnIndex +" not in [0,1]");
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 1:
            case 2:
            case 3: return String.class;
            case 4: return Records.Item.class;
            default:
                throw new IndexOutOfBoundsException(columnIndex +" not in [0,3]");
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0: return (rowIndex + 1);
        case 1: return items.get(rowIndex).getAuthor();
        case 2: return items.get(rowIndex).getTitle();
        case 3: return items.get(rowIndex).getAlbum();
        case 4: return items.get(rowIndex);
        default:
            throw new IndexOutOfBoundsException(columnIndex +" not in [0,3]");
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable && columnIndex > 0 && columnIndex < 4;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 1:
            items.get(rowIndex).setAuthor(aValue.toString());
            break;
        case 2:
            items.get(rowIndex).setTitle(aValue.toString());
            break;
        case 3:
            items.get(rowIndex).setAlbum(aValue.toString());
            break;
        default:
            throw new IndexOutOfBoundsException(columnIndex +" not in [1,2]");
        }
        items.notifyObservers();
    }

    public void remove(int[] inds) {
        Arrays.sort(inds);
        for (int i = inds.length - 1; i >= 0; i--) {
            items.remove(inds[i]);
        }
        items.notifyObservers();
    }
}
