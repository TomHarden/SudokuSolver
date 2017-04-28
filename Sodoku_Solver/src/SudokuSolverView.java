package src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SudokuSolverView extends JPanel
{
	public SudokuSolverView(int puzzleNum)
	{
		super();
        
        this.setLayout(new GridLayout(1, 3));
        
        JPanel unsolvedPanel = new JPanel();
        unsolvedPanel.setLayout(new BorderLayout());
        SudokuView view_unsolved = new SudokuView(new SudokuModel(puzzleNum), Color.WHITE);
        JLabel titleLabel_unsolved = new JLabel("Unsolved Puzzle (#" + puzzleNum + ")");
        unsolvedPanel.add(view_unsolved, BorderLayout.CENTER);
        unsolvedPanel.add(titleLabel_unsolved, BorderLayout.NORTH);

        this.add(unsolvedPanel);//unsolved
        this.add(getSolvedPanel(puzzleNum, Color.GREEN, true));//solved with brute force algo
        this.add(getSolvedPanel(puzzleNum, Color.CYAN, false));//solved with smart algo
	}
	/*public SudokuSolverView(int[][] puzzle)
	{
	}*/
	
	private JPanel getSolvedPanel(int puzzleNum, Color color, boolean solveByBruteForce)
	{
        JPanel solvedPanel = new JPanel();
        solvedPanel.setLayout(new BorderLayout());
        SudokuModel model = new SudokuModel(puzzleNum);
        
        long startTime = System.nanoTime();
        model.solveSudoku_Smart();
        long endTime = System.nanoTime();
        
        SudokuView view = new SudokuView(model, color);
    	long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
    	
    	String method = "Smart";
    	if (solveByBruteForce)
    		method = "Brute Force";
    	JLabel timeLabel = new JLabel("\"" + method + "\" - " + duration + " ms");
    	solvedPanel.add(view, BorderLayout.CENTER);
        solvedPanel.add(timeLabel, BorderLayout.NORTH);
        return solvedPanel;
	}
}
