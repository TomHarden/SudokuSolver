package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class SudokuModel
{
	//********************
	//*****CLASS VARS*****
	/**The Sudoku board.  It is comprised of sub-boards, which are represented by each elt of the board*/
	Capped_Set[] sudokuGrid;
	/**All the numbers needed to complete a square, column, or row.*/
	Capped_Set complete;
	int totalRows, totalColumns, totalSquares;
	
	//**********************
	//*****CONSTRUCTORS*****
	public SudokuModel()
	{
		this(1);//cannot generate a sudoku puzzle automatically, must use precanned ones!
	}
	public SudokuModel(int preMadeNum)
	{
		this(3, preMadeNum);
	}
	public SudokuModel(int m, int preMadeNum)
	{
		this(m, m, preMadeNum);
	}
	public SudokuModel(int rowsPerSq, int colsPerSq, int preMadeNum)
	{
		totalRows = rowsPerSq * rowsPerSq;
		totalColumns = colsPerSq * colsPerSq;
		totalSquares = rowsPerSq * colsPerSq;
		complete = new Capped_Set(totalSquares);
		for (int i = 1; i <= totalSquares; i++)
			complete.add(i);
		sudokuGrid = new Capped_Set[totalSquares];
		assignPuzzleFromFile(preMadeNum);
	}
	
	private void assignPuzzleFromFile(int puzzleNum)
	{
		String fileText = "sudoku" + puzzleNum + ".txt";
		ArrayList<String> boardFileTextLines = File_Manipulation.getFileText(fileText);
		for (int rowNum = 0; rowNum < boardFileTextLines.size(); rowNum++)
		{
			sudokuGrid[rowNum] = new Capped_Set(complete.getMaxSize());
			for(int colNum = 0; colNum < boardFileTextLines.get(rowNum).length(); colNum++)
			{
				int n = Integer.parseInt(boardFileTextLines.get(rowNum).substring(colNum, colNum + 1));
				if (n == 0)
					sudokuGrid[rowNum].add(Capped_Set.BLANK);
				else
					sudokuGrid[rowNum].add(n);
			}
		}
	}
	
	//***************************
	//*****SOLVE - RECURSIVE*****
	public void solveSudoku_BruteForce() { solveSudoku_BruteForce(0,0); }
	private boolean solveSudoku_BruteForce(int row, int column)
	{
		int nextRow = row;
		int nextColumn = column + 1;
		if (nextColumn >= totalColumns)
		{
			nextColumn = 0;
			nextRow++;
		}
		
		int sqNum = getNumAt(row, column);
		if (sqNum != Capped_Set.BLANK)//already a number on square!  No work required!
			if (nextTileExists(nextRow, nextColumn))
				return solveSudoku_BruteForce(nextRow, nextColumn);
			else
				return true;
		
		//go through list of nums to try in spot!
		for (int i = 0; i < complete.getMaxSize(); i++)
		{
			if (isValid(complete.get(i), row, column))//ie if no conflicts
			{
				set(complete.get(i), row, column);
				if (nextTileExists(nextRow, nextColumn))
					if(solveSudoku_BruteForce(nextRow, nextColumn))
						return true;
					else//if it does not return true, allow it to continue iterating to try other numbers in that location!!
						set(Capped_Set.BLANK, row, column);//reset assigned numbers to blank.  Need to iterate through the rest!
				else//there are no more tiles left to check.  It is valid, so return true!
					return true;
			}
		}
		set(Capped_Set.BLANK, row, column);
		return false;//if after exhausting every combination it does not work, return false.
	}
	
	//***********************************
	//**********SOLVE - "SMART"**********
	public void solveSudoku_Smart()
	{
		boolean isDone = false;
		boolean furtherProgress = false;//set to false for every iteration.  If not set to true within the iteration of the board, then it is obvious that no progress can be made, and the brute force alg must take care of the rest
		while(!isDone)//while the puzzle still needs to be solved
		{
			for (int r = 0; r < totalRows && !isDone; r++)//go row by row
				for (int c = 0; c < totalColumns && !isDone; c++)//go column by column
					if (sudokuGrid[r].getSet().get(c) == Capped_Set.BLANK)//if a space is blank, try to fill it in
						isDone = solveSudoku_Smart(r, c, furtherProgress);//if this returns true, there are no more blanks!
			if (!furtherProgress)
			{
				this.solveSudoku_BruteForce();//cannot deduce via context any further - solve what remains with BruteForce!!
				return;
			}
			furtherProgress = false;
		}
	}
	public boolean solveSudoku_Smart(int row, int column, boolean furtherProgress)
	{
		for (int i = 0, missingNum = this.getSquare_EmptyNums(row, column).get(i);
				i < this.getSquare_EmptyNums(row, column).size();
				missingNum = this.getSquare_EmptyNums(row, column).get(i++))//given the row and column parameters, iterate through the numbers the square is missing
		{
			//iterate through the square's missing numbers
			ArrayList<Integer> holdR = new ArrayList<Integer>();//holds rows that are possibilities for missing num
			ArrayList<Integer> holdC = new ArrayList<Integer>();//holds columns that are possibilities for missing num
				//go through the rows running through the square
				for (int r = getSquareStart_Row(row); r < getSquareStop_Row(row); r++)
					//if a squarerow is not entirely filled, check if the entire row contains one of the missing numbers of the square
					if (!(this.getSquareRow_IsFull(r, column) || this.getRow_FilledNums(r).contains(missingNum)))
						holdR.add(r);
				//go through the columns running through the square
				for (int c = getSquareStart_Column(column); c < getSquareStop_Column(column); c++)
					//if a squarecolumn is not entirely filled, check if the entire column contains one of the missing numbers of the square
					if (!(this.getSquareColumn_IsFull(row, c) || this.getColumn_FilledNums(c).contains(missingNum)))
						holdC.add(c);				
				
				//if a set of coordinates is not blank, remove that set!
				int openings = 0;
				int openRowIndex = -1;
				int openColumnIndex = -1;
				for (int r = 0; r < holdR.size(); r++)
					for (int c = 0; c < holdC.size(); c++)
						if (sudokuGrid[holdR.get(r)/*potential rowNum*/].getSet().get(holdC.get(c)/*potential columnNum*/)
								== Capped_Set.BLANK)//Check if the spot is blank.  If it isnt, it has a number already there!
						{
							//iterate through the lists that contain the rows/column coordinates of columns and rows that are missing the number
							openings++;
							openRowIndex = r;
							openColumnIndex = c;
						}
				//if there are four or more "checks" from other relevant squares, set the one row/column that does not interface with them equal to the missing Number
				//if (openings == 1/*(holdR.size() == 1 && holdC.size() == 1 )*/) //|| rowChecks + columnChecks >= 2 * (Math.sqrt(totalSquares) - 1))//if it is actually ">" then something went wrong!
				if (openings == 1)//if there is only one place to put the number, then it MUST go there!
				{
					sudokuGrid[holdR.get(openRowIndex)].getSet().set(holdC.get(openColumnIndex), missingNum);
					furtherProgress = true;
					easyFills(holdR.get(openRowIndex), holdC.get(openColumnIndex));//check if there are any easy fills to be made, now the table has been filled more!
				}
		}
		return isFullyFilledIn();

	}
	private boolean isFullyFilledIn()//checks if the table has been fully filled in
	{
		for(int r = 0; r < totalRows; r++)
			if (this.getRow_EmptyNums(r).size() != 0)
				return false;//is not done
		return true;//is done
	}
	
	private void easyFillRow(int row)
	{
		if (getRow_EmptyNums(row).size() == 1)
		{
			int index = getRow_AllNums(row).indexOf(Capped_Set.BLANK);//the index of the column within the row that will be filled!
			sudokuGrid[row].set(index, getRow_EmptyNums(row).get(0));//at the specified location (row, index) assign missing number
			easyFills(row, index);//with each fill in, check if easy fill!  After the newest spot is checked, return to this function and continue to next if statement!
		}
	}
	private void easyFillColumn(int column)
	{
		if (getColumn_EmptyNums(column).size() == 1)
		{
			int index = getColumn_AllNums(column).indexOf(Capped_Set.BLANK);//index of row of final number in column
			sudokuGrid[index].set(column, getColumn_EmptyNums(column).get(0));//at specified location (index, column) assign missing number!
			easyFills(index, column);//with each fill in, check if easy fill!  After the newest spot is checked, return to this function and continue to next if statement!
		}
	}
	private void easyFillSquare(int row, int column)
	{
		if (getSquare_EmptyNums(row, column).size() == 1)
			//get the rows and columns of the square.
			//see which row/column is missing the same number as the missing number of the square
			//assign to the grid at that location!
			for(int r = this.getSquareStart_Row(row);
					r < this.getSquareStop_Row(row);
					r++)
				if (getRow_EmptyNums(r).contains(getSquare_EmptyNums(row, column)))
					for (int c = this.getSquareStart_Column(column);
							c < this.getSquareStop_Column(column);
							c++)
						if (getColumn_EmptyNums(c).contains(getSquare_EmptyNums(row, column)))
						{
							sudokuGrid[r].set(c, getSquare_EmptyNums(row, column).get(0));
							//SET A NEW NUMBER, NEED TO CHECK FOR MORE EASY FILLS!
							easyFills(r, c);
							//easyFills(row, column);//this wont work!!
							return;
						}
	}
	private void easyFills(int row, int column)
	{
		//looks at the newly placed element and checks if the row/column/square have one space left.
		//fill rows with only one number missing
		easyFillRow(row);
		//fill columns with only one number missing
		easyFillColumn(column);
		//fill squares with only one number missing
		easyFillSquare(row, column);
		
	}
	
	
	//***************************
	//**********UTILITY**********
	public boolean nextTileExists(int row, int column)
	{
		if (row < totalRows && column < totalColumns)
			return true;
		return false;
	}
		
	public boolean isValid(int newNum, int newRow, int newColumn)
	{
		if (isInRow(newNum, newRow) ||
			isInColumn(newNum, newColumn) ||
			isInSquare(newNum, newRow, newColumn))
			return false;
		return true;
	}
	public boolean isInRow(int newNum, int rowNum) { return sudokuGrid[rowNum].contains(newNum); }
	public boolean isInColumn(int newNum, int colNum)
	{
		for (int row = 0; row < sudokuGrid.length; row++)
			if (sudokuGrid[row].get(colNum) == newNum)
				return true;
		return false;
	}
	public boolean isInSquare(int someNum, int rowNum, int colNum)
	{
		for (int r = this.getSquareStart_Row(rowNum);
				r < this.getSquareStop_Row(rowNum);
				r++)
			for (int c = this.getSquareStart_Column(colNum);
					c < this.getSquareStop_Column(colNum);
					c++)
				//challengeNum = (int) sudokuGrid[r].get(c);
				if (sudokuGrid[r].get(c) == someNum)
					return true;
		return false;
	}
	
	//**************************
	//**********SETTER**********
	public void set(int num, int row, int column) { sudokuGrid[row].set(column, num);	}
	
	//***************************
	//**********GETTERS**********
	public int getNumAt(int r, int c) {	return sudokuGrid[r].get(c); }
	public int getNumRows()	{ return totalRows;	}
	public ArrayList<Integer> getRow_FilledNums(int rowNum)
	{
		ArrayList<Integer> allNums = getRow_AllNums(rowNum);
		ArrayList<Integer> filledNums = new ArrayList<Integer>();
		for(int i = 0; i < allNums.size(); i++)
			if (allNums.get(i) != Capped_Set.BLANK)
				filledNums.add(allNums.get(i));
		return filledNums;
	}
	public ArrayList<Integer> getRow_EmptyNums(int rowNum)
	{
		ArrayList<Integer> allNums = getRow_AllNums(rowNum);
		ArrayList<Integer> emptyNums = new ArrayList<Integer>();
		for(int i = 0; i < complete.getMaxSize(); i++)
			if (!allNums.contains(complete.get(i)))
				emptyNums.add(complete.get(i));
		return emptyNums;
	}
	public ArrayList<Integer> getRow_AllNums(int rowNum)
	{
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for(int i = 0; i < sudokuGrid[rowNum].getSet().size(); i++)
			nums.add(sudokuGrid[rowNum].getSet().get(i));
		return nums;
	}
	public int getNumColumns() { return totalColumns; }
	public ArrayList<Integer> getColumn_FilledNums(int colNum)
	{
		ArrayList<Integer> allNums = getColumn_AllNums(colNum);
		ArrayList<Integer> filledNums = new ArrayList<Integer>();
		for(int i = 0; i < allNums.size(); i++)
			if (allNums.get(i) != Capped_Set.BLANK)
				filledNums.add(allNums.get(i));
		return filledNums;
	}
	public ArrayList<Integer> getColumn_EmptyNums(int colNum)
	{
		ArrayList<Integer> allNums = getColumn_AllNums(colNum);
		ArrayList<Integer> emptyNums = new ArrayList<Integer>();
		for(int i = 0; i < complete.getMaxSize(); i++)
			if (!allNums.contains(complete.get(i)))
				emptyNums.add(complete.get(i));
		return emptyNums;
	}
	public ArrayList<Integer> getColumn_AllNums(int columnNum)
	{
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for(int i = 0; i < sudokuGrid.length; i++)
			nums.add(sudokuGrid[i].getSet().get(columnNum));
		return nums;
	}

	public ArrayList<Integer> getSquare_FilledNums(int rowNum, int colNum)
	{
		ArrayList<Integer> allNums = getSquare_AllNums(rowNum, colNum);
		ArrayList<Integer> filledNums = new ArrayList<Integer>();
		for(int i = 0; i < allNums.size(); i++)
			if (allNums.get(i) != Capped_Set.BLANK)
				filledNums.add(allNums.get(i));
		return filledNums;
	}
	public ArrayList<Integer> getSquare_EmptyNums(int rowNum, int colNum)
	{
		ArrayList<Integer> allNums = getSquare_AllNums(rowNum, colNum);
		ArrayList<Integer> emptyNums = new ArrayList<Integer>();
		for(int i = 0; i < complete.getMaxSize(); i++)
			if (!allNums.contains(complete.get(i)))
				emptyNums.add(complete.get(i));
		return emptyNums;
	}
	public ArrayList<Integer> getSquare_AllNums(int rowNum, int colNum)
	{
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for (int r = this.getSquareStart_Row(rowNum);
				r < this.getSquareStop_Row(rowNum);
				r++)
			for (int c = this.getSquareStart_Column(colNum);
					c < this.getSquareStop_Column(colNum);
					c++)
				nums.add(sudokuGrid[r].getSet().get(c));
		return nums;
	}
	public ArrayList<Integer> getSquareRow_AllNums(int rowNum, int colNum)
	{
		ArrayList<Integer> nums = new ArrayList<Integer>();
		int startCol = this.getSquareStart_Column(colNum);
		int stopCol = this.getSquareStop_Column(colNum);
		for (int c = startCol; c < stopCol; c++)
			nums.add(sudokuGrid[rowNum].getSet().get(c));
		return nums;
	}
	public ArrayList<Integer> getSquareRow_FilledNums(int rowNum, int colNum)
	{
		ArrayList<Integer> allNums = getSquareRow_AllNums(rowNum, colNum);
		ArrayList<Integer> filledNums = new ArrayList<Integer>();
		for(int i = 0; i < allNums.size(); i++)
			if (allNums.get(i) != Capped_Set.BLANK)
				filledNums.add(allNums.get(i));
		return filledNums;
	}
	public ArrayList<Integer> getSquareColumn_AllNums(int rowNum, int colNum)
	{
		ArrayList<Integer> nums = new ArrayList<Integer>();
		int startRow = this.getSquareStart_Row(rowNum);
		int stopRow = this.getSquareStop_Row(rowNum);
		for (int r = startRow; r < stopRow; r++)
			nums.add(sudokuGrid[r].getSet().get(colNum));
		return nums;
	}
	public ArrayList<Integer> getSquareColumn_FilledNums(int rowNum, int colNum)
	{
		ArrayList<Integer> allNums = getSquareColumn_AllNums(rowNum, colNum);
		ArrayList<Integer> filledNums = new ArrayList<Integer>();
		for(int i = 0; i < allNums.size(); i++)
			if (allNums.get(i) != Capped_Set.BLANK)
				filledNums.add(allNums.get(i));
		return filledNums;
	}
	
	public boolean getSquareRow_IsFull(int rowNum, int colNum) { return (getSquareRow_FilledNums(rowNum, colNum).size() == (int) Math.sqrt(totalRows));	}
	public int getSquareStart_Row(int rowNum)//+_+ NEED TO FIND A BETTER SOLUTION THAN THIS!!!!
	{
		int startRow = rowNum;
		startRow = (int) (startRow / Math.sqrt(totalSquares));
		startRow = (int) (startRow * Math.sqrt(totalSquares));
		return startRow;
	}
	public int getSquareStop_Row(int rowNum) { return (int) (getSquareStart_Row(rowNum) + Math.sqrt(totalSquares)); }
	public boolean getSquareColumn_IsFull(int rowNum, int colNum) {	return (getSquareColumn_FilledNums(rowNum, colNum).size() == (int) Math.sqrt(totalColumns)); }
	public int getSquareStart_Column(int colNum)
	{
		int startCol = colNum;
		startCol = (int) (startCol / Math.sqrt(totalSquares));
		startCol = (int) (startCol * Math.sqrt(totalSquares));
		return startCol;
	}
	public int getSquareStop_Column(int colNum)	{ return (int) (getSquareStart_Column(colNum) + Math.sqrt(totalSquares)); }
	
	//************************
	//*****TEST FUNCTIONS*****
	private void printGrid()
	{
		for (int row = 0; row < 9; row++)
		{
			if (row % 3 == 0)
				System.out.print("\n");
			for (int column = 0; column < 9; column++)
			{
				if (column % 3 == 0)
					System.out.print(" ");
				System.out.print(" ");
				if (sudokuGrid[row].get(column) != Capped_Set.BLANK)
					System.out.print(sudokuGrid[row].get(column));
				else
					System.out.print(0);
			}
			System.out.print("\n");
		}
		System.out.print("\n--------------------------------------------------\n");
	}
}


