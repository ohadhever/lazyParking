package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ExpirationDatePanel extends JPanel {
	private JComboBox<Integer> month;
	private JComboBox<Integer> day;
	private JTextField year;
	private boolean isUnlimited;
	
	
	public ExpirationDatePanel() {
		isUnlimited = false;
		setLayout(new BorderLayout());
		add(new JLabel("<html><center>Expiration<br>Date</center></html>"), BorderLayout.WEST);
		JPanel centerPanel = new JPanel(new GridLayout(2, 1));
		JPanel top = new JPanel();
		Integer[] months =  {1,2,3,4,5,6,7,8,9,10,11,12};
		month = new JComboBox<Integer>(months);
		SimpleDateFormat format = new SimpleDateFormat("yyyy"); // Because Date class is autistic
		int currentYear = Integer.parseInt(format.format(new Date()));
		year = new JTextField(Integer.toString(currentYear), 4);
		year.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int chosenYear;
				try {
					chosenYear = Integer.parseInt(year.getText());
				} catch (NumberFormatException e1) {
					chosenYear = 0;
				}
				day.setModel(new DefaultComboBoxModel<Integer>(getDaysOfMonth((int)(month.getSelectedItem()), chosenYear)));
			}
		});
		day = new JComboBox<Integer>(getDaysOfMonth(1, currentYear));
		month.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int chosenYear;
				try {
					chosenYear = Integer.parseInt(year.getText());
				} catch (NumberFormatException e1) {
					chosenYear = 0;
				}
				day.setModel(new DefaultComboBoxModel<Integer>(getDaysOfMonth((int)(month.getSelectedItem()), chosenYear)));
			}
		});
		top.add(new JLabel("Day:"));
		top.add(day);
		top.add(new JLabel("Month:"));
		top.add(month);
		top.add(new JLabel("Year:"));
		top.add(year);
		centerPanel.add(top);
		JCheckBox unlimited = new JCheckBox("unlimited", false);
		unlimited.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				if (unlimited.isSelected()) {
					day.setEnabled(false);
					month.setEnabled(false);
					year.setEnabled(false);
					isUnlimited = true;
				}
				else {
					day.setEnabled(true);
					month.setEnabled(true);
					year.setEnabled(true);
					isUnlimited = false;
				}
			}
		});
		centerPanel.add(unlimited);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public boolean isUnlimited() {
		return isUnlimited;
	}
	
	public Date getDate() {
		if (isUnlimited)
			return null;
		int year = 0, month, day;
		Date date;
		
		try {
			 year = Integer.parseInt(this.year.getText());
		} catch (NumberFormatException e) {
			return null;
		}
		if (year < 0)
			return null;
		month = (int)this.month.getSelectedItem();
		day = (int)this.day.getSelectedItem();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy"); // Because Date class is autistic
		try {
			date = format.parse("" + day + "-" + month + "-" + year);
		} catch (ParseException e) {
			date = null;
		}
		return date;
	}
	
	private Integer[] getDaysOfMonth(int month, int year) {
		int days = 0;
		if(month == 2) {
			if (year % 4 == 0 && year % 100 != 0)
				days = 29;
			else
				days = 28;
		}
		switch(month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				days = 31;
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				days = 30;
				break;
		}
		
		Integer[] daysArray = new Integer[days];
		for (int i = 0; i < days; i++)
			daysArray[i] = i + 1;
		return daysArray;
	}
}
