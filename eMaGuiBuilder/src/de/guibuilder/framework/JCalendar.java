/* 
 * Copyright (c) 2003, Cameron Zemek
 * 
 * This file is part of JCalendar.
 * 
 * JCalendar is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * JCalendar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.guibuilder.framework;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.*;

/**
 * Swing component for selecting a java.util.Date
 * 
 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
 */
class JCalendar extends JPanel {
   private static final long serialVersionUID = 1L;
	// calendar view objects	
	private CalendarTableModel tblModel;
	private JTable calTable;
	private JScrollPane scrollTable;

	// month components
	private JComboBox cboMonth;
	private JButton prevMonth;
	private JButton nextMonth;

	// year components
	private JLabel lblYear;
	private JButton prevYear;
	private JButton nextYear;

	protected EventListenerList listenerList = new EventListenerList();
	
	private static String months[] = {
	     "January","February","March","April","May","June",
	     "July","August","September","October","November","December" };
	private int lastSelectedRow;
	private int lastSelectedColumn;

	/**
	 * Creates default JCalendar. Weeks start on Calendar.MONDAY
	 */
	JCalendar() {
	   // Init Monatsnamen
		try {
		   String sms = GuiUtil.getDefaultResourceBundle().getString("CalendarMonthNames");
		   if (sms != null) {
		       int cnt = 0;
		       StringTokenizer toks = new StringTokenizer(sms,",");
		       while (toks.hasMoreTokens()) {
			       String m = toks.nextToken();
			       months[cnt] = m;
			       cnt++;
		       }
		   }
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		ImageIcon backIcon = null;
		try {
			backIcon = new ImageIcon(getClass().getResource("/icons/Back16.gif"));
		} catch (Exception ex) {
			System.err.println("JCalendar; Missing Icon: '/icons/Back16.gif'");
		}
		ImageIcon nextIcon = null;
		try {
			nextIcon = new ImageIcon(getClass().getResource("/icons/Forward16.gif"));
		} catch (Exception ex) {
			System.err.println("JCalendar; Missing Icon: '/icons/Forward16.gif'");
		}
		//EmptyBorder buttonBorder = new EmptyBorder(2, 2, 2, 2);
		EmptyBorder buttonBorder = new EmptyBorder(1, 1, 1, 1);

		// Construct Table portion of calendar
		tblModel = new CalendarTableModel();
		calTable = new JTable(tblModel);
		calTable.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				// When the table selection is changed; CalendarListeners will be notified				
				JCalendar.this.fireDateChanged();
			}
		});
		calTable.setDefaultRenderer(Object.class, new CalendarTableRenderer());
		calTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		calTable.setIntercellSpacing(new Dimension(0, 0));
		calTable.setShowGrid(false);
		calTable.setBackground(this.getBackground());
		calTable.setSelectionBackground(new Color(0.6f, 0.6f, 0.8f));
		calTable.setCellSelectionEnabled(true);
		calTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader header = calTable.getTableHeader();
		header.setResizingAllowed(false);
		header.getColumnModel().setColumnMargin(0);
		header.setReorderingAllowed(false);
		header.setDefaultRenderer(new CalendarHeaderRenderer());

		scrollTable = new JScrollPane(calTable);
		scrollTable.setBorder(new EmptyBorder(5, 2, 2, 2));
		scrollTable.setHorizontalScrollBarPolicy(
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollTable.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		// Construct month navigation controls
		cboMonth = new JComboBox(months);
		cboMonth.setBorder(buttonBorder);
		cboMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(getDate());
				cal.set(Calendar.MONTH, cboMonth.getSelectedIndex());
				setDate(cal.getTime());
				fireDateChanged();	
			}
		});
		prevMonth = new JButton(backIcon);
		prevMonth.setBorder(buttonBorder);
		prevMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectPreviousMonth();
			}
		});
		nextMonth = new JButton(nextIcon);
		nextMonth.setBorder(buttonBorder);
		nextMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNextMonth();
			}
		});

		// Construct year navigation controls
		lblYear =
			new JLabel(
				String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
		//lblYear.setBorder(new javax.swing.border.EmptyBorder(2, 4, 2, 4));
		//##lblYear.setBorder(new javax.swing.border.EmptyBorder(2, 2, 2, 2));
		prevYear = new JButton(backIcon);
		prevYear.setBorder(buttonBorder);
		prevYear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectPreviousYear();
			}
		});
		nextYear = new JButton(nextIcon);
		nextYear.setBorder(buttonBorder);
		nextYear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNextYear();
			}
		});

		// Setup panel layout
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		cboMonth.setSelectedIndex(month);
		Box month_yearSelector = new Box(BoxLayout.X_AXIS);
		month_yearSelector.add(prevMonth);
		month_yearSelector.add(cboMonth);
		month_yearSelector.add(nextMonth);
		month_yearSelector.add(Box.createHorizontalGlue());
		month_yearSelector.add(prevYear);
		month_yearSelector.add(lblYear);
		month_yearSelector.add(nextYear);
		setLayout(new BorderLayout());
		add(month_yearSelector, BorderLayout.NORTH);
		add(scrollTable, BorderLayout.CENTER);
		setBorder(new EmptyBorder(5, 5, 5, 5));

		// Setup GUI defaults
		setBackground(Color.WHITE);
		//setPreferredSize(new Dimension(210, 164));
		setPreferredSize(new Dimension(215, 164)); // Nimbus
		selectCurrentDate();
		this.setKeyListener();
	} //end JCalendar constructor

	public void setBackground(Color c) {
		super.setBackground(c);

		if (c == null || calTable == null) {
			return;
		}

		calTable.setBackground(c);

		JTableHeader header = calTable.getTableHeader();
		header.setBackground(c);

		scrollTable.getViewport().setBackground(c);
		scrollTable.setBackground(c);

		cboMonth.setBackground(c);
		prevMonth.setBackground(c);
		nextMonth.setBackground(c);

		lblYear.setBackground(c);
		prevYear.setBackground(c);
		nextYear.setBackground(c);
	}

	void addCalendarListener(CalendarListener listener) {
		listenerList.add(CalendarListener.class, listener);
	}

	void removeCalendarListener(CalendarListener listener) {
		listenerList.remove(CalendarListener.class, listener);
	}

	void fireDateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CalendarListener.class) {
				((CalendarListener) listeners[i + 1]).dateChanged(
					new ChangeEvent(this));
			}
		}
	}

	protected void selectCurrentDate() {
		Calendar cal = Calendar.getInstance();
		setDate(cal.getTime());
	}

	protected void selectPreviousDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.DAY_OF_YEAR, false);
		setDate(cal.getTime());
		fireDateChanged();
	}

	protected void selectNextDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.DAY_OF_YEAR, true);
		setDate(cal.getTime());
		fireDateChanged();
	}

	protected void selectPreviousMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.MONTH, false);
		setDate(cal.getTime());
		fireDateChanged();
	}

	protected void selectNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.MONTH, true);
		setDate(cal.getTime());
		fireDateChanged();
	}

	protected void selectPreviousYear() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.YEAR, false);
		setDate(cal.getTime());
		fireDateChanged();
	}

	protected void selectNextYear() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		cal.roll(Calendar.YEAR, true);
		setDate(cal.getTime());
		fireDateChanged();
	}

	/**
	 * Returns the selected date
	 */
	Date getDate() {
		int row = calTable.getSelectedRow();
		int column = calTable.getSelectedColumn();
		return tblModel.getDateAt(row, column);
	}

	void setDate(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cboMonth.setSelectedIndex(cal.get(Calendar.MONTH)); // Change Event!
		lblYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
		tblModel.setDate(cal.getTime());
		Point pos =
			tblModel.getDayOfMonthPosition(cal.get(Calendar.DAY_OF_MONTH));
		//	the change in cell selection will result in fireDateChanged() being called
		calTable.changeSelection(pos.y, pos.x, false, false);		
		this.fireDateChanged(); // Jetzt auch der Tag!
	}
	private void setKeyListener() {
	    this.calTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                // nix                
            }

            public void keyReleased(KeyEvent e) {
                ListSelectionModel rowSM = calTable.getSelectionModel();
                int selRow = rowSM.getMinSelectionIndex();
                int selCol = calTable.getSelectedColumn();
                if (selRow != lastSelectedRow || selCol != lastSelectedColumn) {
                    fireDateChanged();
                }
                lastSelectedRow = selRow;
                lastSelectedColumn = selCol;
            }

            public void keyTyped(KeyEvent e) {
                // nix
            }
	    
	    });
	}

	/**
	 * Table model used by JCalendar
	 * 
	 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
	 */
	private static class CalendarTableModel extends AbstractTableModel {
      private static final long serialVersionUID = 1L;

		private static String columnNames[] =
			{ "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
			//{ "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" };

		private Calendar cal;
		private int year, month, daysInMonth; // cached
		private int firstDay;
		/**
		 * Constructs CalendarModel
		 */
		CalendarTableModel() {
			   String ds = GuiUtil.getDefaultResourceBundle().getString("CalendarDayNames");
			   if (ds != null) {
			       int cnt = 0;
			       StringTokenizer toks = new StringTokenizer(ds,",");
			       while (toks.hasMoreTokens()) {
				       String d = toks.nextToken();
				       columnNames[cnt] = d;
				       cnt++;
			       }
			   }
		   
			cal = Calendar.getInstance();
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			setDate(cal.getTime());
		}

		Date getDate() {
			return cal.getTime();
		}

		void setDate(Date date) {
			cal.setTime(date);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);
			firstDay = firstOfMonth(month, year);
			daysInMonth = daysInMonth(month, year);
			fireTableStructureChanged();
		}

		/**
		 * Returns a java.util.Date that the cell (row, column) represents
		 */
		Date getDateAt(int row, int column) {
			Calendar c = Calendar.getInstance();
			c.setTime(getDate());
			int day = (row * 7 + column) - firstDay + 1;
			c.set(year, month, 1); // reset calendar to first day in month
			c.set(Calendar.DAY_OF_MONTH, day);
			return c.getTime();
		}

		/** 
		 * Returns the cell position (column, row) of the specified day.
		 * Used by JCalendar to select a date
		 * */
		protected Point getDayOfMonthPosition(int dayOfMonth) {
			int daysInFirstRow = 7 - firstDay;
			int column = 0;
			if (dayOfMonth <= daysInFirstRow) {
				column = 7 - Math.abs(dayOfMonth - daysInFirstRow - 1);
			} else {
				column = (dayOfMonth - daysInFirstRow - 1) % 7;
			}
			int row = (int) Math.ceil((dayOfMonth - daysInFirstRow) / 7.0);
			return new Point(column, row);
		}

		/**
		 * Returns true if the cell (row, column) belongs to the currently displayed month
		 */
		boolean isCellCurrentMonth(int row, int column) {
			int day = (row * 7 + column) - firstDay;
			return (day >= 0 && day < daysInMonth);
		}

		/**
		 * Returns the number of days in a month
		 */
		protected int daysInMonth(int _month, int _year) {
			Calendar c = Calendar.getInstance();
			c.set(_year, _month, 1);
			c.roll(Calendar.DAY_OF_MONTH, false);
			return c.get(Calendar.DAY_OF_MONTH);
		}

		/**
		 * Returns the column coord for the first day of the month
		 */
		protected int firstOfMonth(int _month, int _year) {
			int firstDayOfMonth = 0;
			Calendar c = Calendar.getInstance();
			c.set(_year, _month, 1);
			c.setFirstDayOfWeek(Calendar.MONDAY);
			firstDayOfMonth = c.get(Calendar.DAY_OF_WEEK);

			int offset = firstDayOfMonth - Calendar.MONDAY;
			if (firstDayOfMonth < Calendar.MONDAY) {
				offset = 7 + offset;
			}
			return offset;
		}

		//
		// TableModel interface
		//

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getColumnCount() {
			return 7; // There are 7 days in a week
		}

		public int getRowCount() {
			//	A month can span over a 6 week period 
			// (eg. starts on a SUNDAY and finish on a MONDAY)
			return 6;
		}

		public Object getValueAt(int row, int column) {
			Calendar c = Calendar.getInstance();
			c.setTime(getDateAt(row, column));
			return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		}
	} //end CalendarTableModel
	/**
	 * Interface to listen to date change events on JCalendar
	 * 
	 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
	 */
	public interface CalendarListener extends EventListener{
		public void dateChanged(ChangeEvent e);
	}
	/**
	 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
	 */
	private static class CalendarTableRenderer extends DefaultTableCellRenderer{
      private static final long serialVersionUID = 1L;

		CalendarTableRenderer(){
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			CalendarTableModel tblModel = (CalendarTableModel)table.getModel();
			if(tblModel.isCellCurrentMonth(row, column)){		
				setForeground(Color.BLACK);
			}else{
				setForeground(Color.GRAY);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	} //end CalendarTableRenderer
	/**
	 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
	 */
	private static class CalendarHeaderRenderer extends DefaultTableCellRenderer{
      private static final long serialVersionUID = 1L;

		CalendarHeaderRenderer(){
			setForeground(Color.white);
			setBackground(Color.blue.darker());
			setHorizontalAlignment(SwingConstants.CENTER);
		}
	}	
} //end JCalendar class
