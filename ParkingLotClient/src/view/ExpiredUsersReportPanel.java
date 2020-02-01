package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ExpiredUsersReportPanel extends JPanel {
	private TablePanel tablePanel;
	
	public ExpiredUsersReportPanel(GUI parent) {
		LinkedList<GUIObserver> observers = parent.getObservers();
		setLayout(new BorderLayout());
		tablePanel = new TablePanel();
		add(tablePanel, BorderLayout.CENTER);
		JPanel buttonsPanel = new JPanel();
		JButton updateExpirationBtn = new JButton("Update Expiration");
		updateExpirationBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = tablePanel.getUsername();
				if (username != null) {
					JPanel updateExpirationPanel = new JPanel(new BorderLayout());
					updateExpirationPanel.add(new JLabel("Set new expiration date for " + username), BorderLayout.NORTH);
					ExpirationDatePanel expirationDatePanel = new ExpirationDatePanel();
					updateExpirationPanel.add(expirationDatePanel, BorderLayout.CENTER);
					Date date = null;
					if (JOptionPane.showConfirmDialog(ExpiredUsersReportPanel.this, updateExpirationPanel, "Update expiration", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
						if (expirationDatePanel.isUnlimited() || (date = expirationDatePanel.getDate()) != null)
							for (GUIObserver observer : observers)
								observer.updateDriverExpirationEvent(username, date);
						else
							JOptionPane.showMessageDialog(ExpiredUsersReportPanel.this, "the year must be a non-negative integer", "INVALID DATE", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonsPanel.add(updateExpirationBtn);
		
		JButton deleteDriverBtn = new JButton("Delete Driver");
		deleteDriverBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = tablePanel.getUsername();
				if (username != null)
					if (JOptionPane.showConfirmDialog(ExpiredUsersReportPanel.this, "Are you sure you want to delete " + username + "?", "Delete Driver", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
						for (GUIObserver observer : observers)
							observer.deleteDriverEvent(username);
			}
			
		});
		buttonsPanel.add(deleteDriverBtn);
		add(buttonsPanel, BorderLayout.SOUTH);
		
		parent.setExpiredUsersReportPanel(this);
		for (GUIObserver observer : observers)
			observer.requestForExpiredUsersEvent();
	}
	
	void deleteHighlightedRow() {
		tablePanel.deleteHighlightedRow();
	}
	
	void addRow(String username, Date expDate) {
		tablePanel.addRow(username, expDate);
	}
	
	
	private class TablePanel extends JScrollPane {
		private JTable table;
		private DefaultTableModel tableModel;
		private SimpleDateFormat format;
		
		public TablePanel() {
			super();
			String[] colNames = {"Username", "Expiration Date"};
			format = new SimpleDateFormat("dd-MM-yyyy");
			
			tableModel = new DefaultTableModel(colNames, 0) {
				
				@Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			
			table = new JTable(tableModel);
			setViewportView(table);
			table.setFillsViewportHeight(true);
			
			setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		
		void addRow(String username, Date expDate) { 
			String expDateStr = format.format(expDate);
			Object[] row = {username, expDateStr};
			
			tableModel.addRow(row);
		}
		
		void deleteHighlightedRow() {
			tableModel.removeRow(table.getSelectedRow());
		}
		
		String getUsername() {
			int idx = table.getSelectedRow();
			if (idx != -1)
				return (String)table.getValueAt(idx, 0);
			return null;
		}

	}
}
