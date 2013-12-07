/*
 * Corey Holt - cmh09h
 * COP4531
 * 12/10/2013
 * Programming Assignment 3
 * Shortest Paths
 */

import java.util.LinkedList;


public class Node implements Comparable<Node>
{
	public String nodeId;
	public LinkedList<Edge> adjList;
	public LinkedList<Edge> inEdges;
	public int dist;
	public int tableId;

	public Node(String id)
	{
		nodeId = id;
		adjList = new LinkedList<Edge>();
		inEdges = new LinkedList<Edge>();
	}

	@Override
	public int compareTo(Node o)
	{
		int compareRet = 0;

		if (dist < o.dist)
		{
			// Set compareRet to reflect that the member component
			//  is less than the passed component
			compareRet = -1;
		}
		else if (dist == o.dist)
		{
			// Set compareRet to reflect that the components are
			//  equal
			compareRet = 0;
		}
		else
		{
			// Set compareRet to reflect that the member component
			//  is greater than the passed component
			compareRet = 1;
		}

		return compareRet;
	}
}
