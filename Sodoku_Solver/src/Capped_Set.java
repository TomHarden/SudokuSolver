package src;

import java.util.ArrayList;

public class Capped_Set
{
	public ArrayList<Integer> list;
	private int maxSize = 1;
	public static int BLANK = -1;
	public Capped_Set(int inMaxSize)
	{
		list = new ArrayList<Integer>();
		maxSize = inMaxSize;
	}
	
	public void add(int e)
	{
		if (e != BLANK)
			if(list.contains(e) || list.size() >= maxSize || e < 0)
				return;
		list.add(e);
	}
	public void set(int index, int num)
	{
		if (index < 0)
			index = BLANK;
		list.set(index, num);
	}
	public int get(int i)
	{
		return list.get(i);
	}
	public int getMaxSize()
	{
		return maxSize;
	}
	public ArrayList<Integer> getSet()
	{
		return list;
	}
	public boolean contains(int i)
	{
		return list.contains(i);
	}
}
