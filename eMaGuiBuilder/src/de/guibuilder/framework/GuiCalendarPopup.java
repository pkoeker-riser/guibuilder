/*
 * Created on 07.05.2005
 */
package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.metal.MetalComboBoxIcon;

/**
 * @author peter
 */
public class GuiCalendarPopup extends GuiDate implements ActionListener {
	private JButton button;
	protected JCalendar cal;
	private CalendarPopup calendarPopup;
	private GuiWindow owner;
	private GuiDate linkDate;

	public GuiCalendarPopup(GuiWindow owner) {
		this("calendarPopup", owner);
	}

	public GuiCalendarPopup(String label, GuiWindow owner) {
		super(label);
		this.owner = owner;
		calendarPopup = new CalendarPopup(this.cal, this.owner); // JFrame owner
	}

	protected void guiInit() {
		cal.addCalendarListener(new JCalendar.CalendarListener() {
			public void dateChanged(ChangeEvent e) {
				Date d = cal.getDate();
				component.setText(format.format(d));
				if (getText().equals(oldValue) == false) {
					setModified(true);
					// Support linked GuiDate Component ?
					if (linkDate != null) {
						Date xoldValue = linkDate.getValueDate();
						if (xoldValue == null || xoldValue.equals(d) == false) {
							linkDate.setValue(d);
							linkDate.setModified(true);
							linkDate.updateLinkedColumn(); // geändert: 25.5.2008
							linkDate.changed(); // Neu: 25.10.2008
						}
					}
				}
				// PostProcessor
				postProc();
				// OnChange
				if (isModified()) {
					if (actionChange != null && getRootPane() != null) {
						getRootPane().obj_ItemChanged(GuiCalendarPopup.this, actionChange,
								getValue());
					}
				}
				oldValue = getText();
			}
		});
		this.format = defaultFormat;
		this.format.setLenient(false);
	}

	public JComponent getJComponent() {
		super.getJComponent();
		if (this.cal == null) {
			this.cal = new JCalendar();
		}
		if (button == null) {
			button = new JButton(new MetalComboBoxIcon());
			button.addActionListener(this);
		}
		return this.button;
	}

	public final void setValue(Object o) {
		super.setValue(o);
		if (o != null) {
			try {
				Date d = format.parse(o.toString());
				this.setValue(d);
			} catch (Exception ex) {
				System.err.println("GuiCalendar#setValue: " + ex.getMessage());
			}
		}
	}

	public final void setValue(Date d) {
		super.setValue(d);
		cal.setDate(d);
	}

	public final void setValue(long d) {
		super.setValue(d);
		cal.setDate(new Date(d));
	}

	public final void setValue(Long d) {
		super.setValue(d);
		cal.setDate(new Date(d.longValue()));
	}

	void setGuiDate(GuiDate date) {
		this.linkDate = date;
		if (linkDate != null) {
			this.linkDate.setLinkPopup(this);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		togglePopup();
	}
	
	public final void setEnabled(boolean b) {
    this.getJComponent().setEnabled(b);
  }
	private void togglePopup() {
		if (calendarPopup.isVisible()) {
			calendarPopup.hide();
		} else {
			calendarPopup.setLocation(getPopupLocation());
			if (this.linkDate != null) {
				Date d = this.linkDate.getValueDate();
				if (d != null) {
					this.setValue(d);
				}
			}
			calendarPopup.show();
		}
	}

	/**
	 * Calculates the upper left location of the Popup.
	 */
	private Point getPopupLocation() {
		Point loc = button.getLocationOnScreen();
		Point popupLoc = new Point(loc);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		popupLoc.y += button.getHeight();
		if ((popupLoc.y + calendarPopup.getHeight()) > screen.getHeight()) {
			// clipped by bottom of screen, so move to above drop-down
			popupLoc.y -= (button.getHeight() + calendarPopup.getHeight());
		}
		if ((popupLoc.x + calendarPopup.getWidth()) > screen.getWidth()) {
			// clipped by right of screen, so move to left of drop-down
      popupLoc.x += button.getWidth() - calendarPopup.getWidth();
		}
		return popupLoc;
	}

	/**
	 * Popup window used by JCalendarBox
	 * 
	 * @author <a href="grom@capsicumcorp.com">Cameron Zemek</a>
	 */
	private class CalendarPopup extends JWindow {
		final private Dimension displaySize;
		final private JCalendar calendar;
		private MouseInceptor mouseInceptor;
		private Component glassPane;
		private GuiWindow frame;

		public CalendarPopup(JCalendar cal, GuiWindow frame) {
			super(frame.getWindow());
			calendar = cal;
			displaySize = calendar.getPreferredSize();
			displaySize.width += 10;
			displaySize.height += 4;
			mouseInceptor = new MouseInceptor(this, frame.getLayeredPane());
			this.frame = frame;
			this.glassPane = frame.getGlassPane();

			// Setup content pane
			JPanel contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.setBorder(BorderFactory.createLineBorder(Color.black));
			contentPane.add(calendar, BorderLayout.CENTER);
			setContentPane(contentPane);
			pack();
		}

		public void hide() {
		  if (mouseInceptor != null)
		    mouseInceptor.setVisible(false);
      // Wenn ich mir den Konstruktur der Klasse ansehe,
      // kann der Fall dass der frame == null ist durchaus auftreten.
      // Sicherheitshalber sollte man hier vorher prüfen
      // JFrame hat hier u.U. kein RootPane!? Wie das?
      if (frame != null && frame.getRootPane() != null) {
        // TODO
        // Bug#1627214: NullPointerException analysieren
        // zunächst hier nur Workaround
        try {
          frame.setGlassPane(glassPane);
        } catch (Exception ex) {
          //ex.printStackTrace();
          System.err
              .println("Error: Hiding CalendarPopup in GuiCalendarPopup::CalendarPopup.hide() results in Exception: "
                  + ex.getMessage());
        }
      }
      super.hide();
    }

		public void show() {
			super.show();
			if (frame != null) {
			  frame.setGlassPane(mouseInceptor);
			} else {
				System.out.println("Warning: GuiCalendarPopup missing Frame");
			}
			if (mouseInceptor != null)
			  mouseInceptor.setVisible(true);
		}

		public Dimension getMinimumSize() {
			return displaySize;
		}

		public Dimension getPreferredSize() {
			return getMinimumSize();
		}

		public Dimension getMaximumSize() {
			return getMinimumSize();
		}

		private class MouseInceptor extends JComponent implements MouseListener {
			private CalendarPopup popup;

			private JComponent root;

			public MouseInceptor(CalendarPopup popup, JComponent root) {
				addMouseListener(this);
				this.popup = popup;
				this.root = root;
			}

			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				if (!popup.getBounds().contains(p)) {
					popup.hide();
				}
				redispatchMouseEvent(e);
			}

			public void mouseEntered(MouseEvent e) {
				redispatchMouseEvent(e);
			}

			public void mouseExited(MouseEvent e) {
				redispatchMouseEvent(e);
			}

			public void mousePressed(MouseEvent e) {
				redispatchMouseEvent(e);
			}

			public void mouseReleased(MouseEvent e) {
				Point p = e.getPoint();
				if (!popup.getBounds().contains(p)) {
					popup.hide();
				}
				redispatchMouseEvent(e);
			}

			private void redispatchMouseEvent(MouseEvent e) {
				Point p = e.getPoint();

				Component delegate = SwingUtilities.getDeepestComponentAt(root, p.x,
						p.y);

				if (delegate == null) {
					return;
				}

				Point componentPoint = SwingUtilities.convertPoint(root, p, delegate);

				delegate.dispatchEvent(new MouseEvent(delegate, e.getID(), e.getWhen(),
						e.getModifiers(), componentPoint.x, componentPoint.y, e
								.getClickCount(), e.isPopupTrigger()));
			}
		}
	}
}