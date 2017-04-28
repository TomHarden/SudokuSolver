package src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Timer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;

public class Driver extends JFrame
{
	// *********************************************************
    // **************** Configuration Variables ****************
    // *********************************************************
	private final static int DEFAULT_PUZZLE = 13;//ex. sodoku12.txt
	
	// *********************************************************
    // ******************** Class Constants ********************
    // *********************************************************
	private static final long serialVersionUID = 1L;
	public static Color color;
	
	// *********************************************************
    // ********************   Constructor   ********************
    // *********************************************************
	public Driver(Color c, int puzzle)
	{
		setTitle("Tom's Sudoku Puzzle Solver");
		setContentPane(new SudokuSolverView(puzzle));
	    color = c;
	    (new SudokuSolverView(puzzle)).setBackground(color);
	    setVisible(true);
	}
    
	// *********************************************************
    // ********************      Main       ********************
    // *********************************************************
	public static void main(String[] args)
	{
		int puzzleNum = 0;
		if (args.length != 0)
			puzzleNum = Integer.parseInt(args[0]);
		else
			puzzleNum = DEFAULT_PUZZLE;
		Driver gui = new Driver(Color.black, puzzleNum);	
        gui.setDefaultCloseOperation( EXIT_ON_CLOSE );
        gui.setExtendedState( Frame.MAXIMIZED_BOTH );
        gui.setVisible(true);
	}
	
}