package src;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SudokuView extends JPanel
{
	JButton[][] grid;//[row][column] 
	public SudokuView(SudokuModel model)
	{
		this(model, Color.GRAY);
	}
	public SudokuView(SudokuModel model, Color background)
	{
		this.setLayout(new GridLayout(model.getNumRows(), model.getNumColumns()));
		grid = new JButton[model.getNumRows()][model.getNumColumns()];
		for (int row = 0; row < model.getNumRows(); row++)
		{
			for (int column = 0; column < model.getNumColumns(); column++)
			{
				String num = " ";
				if (model.getNumAt(row, column) != Capped_Set.BLANK)
					num = "" + model.getNumAt(row, column);
				grid[row][column] = new JButton(num);
				grid[row][column].setBackground(background);
				this.add(grid[row][column]);
			}
		}
	}
}
