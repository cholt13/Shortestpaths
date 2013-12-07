/*
 * Corey Holt - cmh09h
 * COP4531
 * 12/10/2013
 * Programming Assignment 3
 * Shortest Paths
 */

public class Edge
{
	public String nodeId1;
	public String nodeId2;
	public int weight;

	public Edge(String n1, String n2, int w)
	{
		nodeId1 = n1;
		nodeId2 = n2;
		weight = w;
	}
}
