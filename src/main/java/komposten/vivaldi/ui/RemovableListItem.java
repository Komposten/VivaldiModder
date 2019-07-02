package komposten.vivaldi.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RemovableListItem extends JPanel
{
	private JTextField field;
	private JButton buttonRemove;
	
	
	public RemovableListItem(String text, ActionListener removeListener)
	{
		field = new JTextField(text);
		buttonRemove = new JButton("Remove");
		
		field.setEditable(false);
		
		if (removeListener != null)
		{
			buttonRemove.addActionListener(
					action -> removeListener.actionPerformed(new ActionEvent(this, 0, "remove")));
		}
		
		createLayout();
	}


	private void createLayout()
	{
		setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(field, constraints);
		constraints.weightx = 0;
		constraints.insets.left = 4;
		add(buttonRemove, constraints);
	}
	
	
	public String getText()
	{
		return field.getText();
	}
}
