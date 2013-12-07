/*
 * Corey Holt - cmh09h
 * COP4531
 * 12/10/2013
 * Programming Assignment 3
 * Shortest Paths
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Scanner;


public class ShortestPaths
{
	ArrayList<Node> nodeSet;
	ArrayList<Node> finalSet;
	String sourceId;
	int kVal;
	int distTable[][];
	Hashtable<String, Integer> idHash;

	ShortestPaths(ArrayList<Node> ns, String sid, int k)
	{
		nodeSet = ns;
		finalSet = new ArrayList<Node>();
		sourceId = sid;
		kVal = k;
		distTable = new int[10][10];
		idHash = new Hashtable<String, Integer>();
	}

	public ArrayList<Node> findShortestPaths()
	{
		// Use a priority queue for Dijkstra's
		//  This will be a min priority queue according to how
		//  Node implements Comparable
		PriorityQueue<Node> nodeQueue =
				new PriorityQueue<Node>(10);
		// Node u will hold each node that is extracted from the
		//  priority queue in each iteration
		Node u;

		// First initialize all of the nodes in the nodeSet
		initializeNodes(sourceId);

		// Add all of the nodes to the queue
		for (Node v : nodeSet)
		{
			nodeQueue.add(v);
		}

		// While the priority queue is not empty
		while (nodeQueue.size() > 0)
		{
			// Extract the smallest dist node from
			//  the priority queue (i.e. first one)
			u = nodeQueue.poll();
			// This node's dist is finalized, so add it to the
			//  finalSet
			finalSet.add(u);
			// Find the nodes in u's adjList and relax each one
			for (Edge e : u.adjList)
			{
				String vId = e.nodeId2;
				Node v = null;
				for (Node qNode : nodeQueue)
				{
					if (qNode.nodeId.equals(vId))
					{
						v = qNode;
					}
				}
				if (v != null)
				{
					// Remove v from the priority queue, relax it,
					//  then place it back in the queue to cause
					//  proper resorting of nodes in queue
					nodeQueue.remove(v);
					relax(u, v, e.weight);
					nodeQueue.add(v);
				}
			}
		}

		return finalSet;
	}

	public ArrayList<Node> findReliablePaths()
	{
		// Re-initialize nodes
		initializeNodes(sourceId);
		// Initialize the memoization table
		initializeTable();

		// Build up the memoization table for each number of hops
		//  up to and including the k value
		for (int i = 1; i <= kVal; ++i)
		{
			// Need to calculate the shortest reliable path for
			//  every node other than the source node as the #
			//  of hops is updated
			for (Node v : nodeSet)
			{
				if (!v.nodeId.equals(sourceId))
				{
					// Calculate the shortest reliable path and
					//  store the value in the memo table
					v.dist = calcReliablePath(v, i);
					distTable[v.tableId][i] = v.dist;
				}
			}
		}

		return nodeSet;
	}

	private int calcReliablePath(Node destNode, int hops)
	{
		int minDist = Integer.MAX_VALUE;
		int uDist;

		// Need to look at all nodes u that have edges to
		//  the current destination node. These edges are
		//  stored in destNode's inEdges list
		for (Edge e : destNode.inEdges)
		{
			// Use earlier values in memo table to calculate the new
			//  value. The idHash allows for constant time retrieval
			//  of the node u's memo tableId using its nodeId
			uDist = distTable[idHash.get(e.nodeId1)][hops - 1];
			// If the retrieved distance is MAX_VALUE, then just skip
			//  it, because it cannot replace the minDist initialized
			//  value anyway
			if (uDist != Integer.MAX_VALUE)
			{
				// If the distance to u retrieved from the memo table plus
				//  the weight of the edge (u,v) is less than the value
				//  currently in minDist, then this is the new minDist value
				//  to reach the current destination from the source node
				if ((uDist + e.weight) < minDist)
				{
					minDist = uDist + e.weight;
				}
			}
		}

		// Return minDist to update v's dist attribute value in the final
		//  nodeSet and v's entry in the memoization table
		return minDist;
	}

	private void initializeNodes(String sourceId)
	{
		// Initializing nodes consists of setting the dist attribute
		//  of the source node to 0 and to Integer.MAX_VALUE (infinity)
		//  for every other node. It also entails assigning indices to
		//  each node for the memoization table. Source node always has
		//  table index 0
		int tableIndex = 1;

		for (Node v : nodeSet)
		{
			if (sourceId.equals(v.nodeId))
			{
				v.dist = 0;
				v.tableId = 0;
			}
			else
			{
				v.dist = Integer.MAX_VALUE;
				v.tableId = tableIndex++;
			}
			// Add a new hash element that pairs the node's id and its
			//  table id
			idHash.put(v.nodeId, v.tableId);
		}
	}

	private void relax(Node n1, Node n2, int weight)
	{
		// If n1.dist is MAX_VALUE, then adding to it will cause an overflow,
		//  so just do nothing
		if ((n2.dist > n1.dist + weight) && (n1.dist != Integer.MAX_VALUE))
		{
			n2.dist = n1.dist + weight;
		}
	}

	private void initializeTable()
	{
		// Dimension 1 of the memoization table refers to the index
		//  of the vertex. Dimension 2 refers to the number of hops.
		// Since node 0 is the source, the dist is 0 irrespective of
		//  number of hops, because no hops are needed to visit the
		//  source when starting at source
		for (int i = 0; i < nodeSet.size(); ++i)
		{
			distTable[0][i] = 0;
		}

		// If the dest vertex index is anything other than 0 (the source)
		//  while the number of hops is 0, then the dest vertex cannot
		//  be reached, which is represented by a dist of MAX_VALUE
		for (int j = 1; j < nodeSet.size(); ++j)
		{
			distTable[j][0] = Integer.MAX_VALUE;
		}
	}

	public static void main(String[] args)
	{
		// Check for proper number of command line args
		if (args.length != 3)
		{
			System.out.println("Usage: java ShortestPaths <input_file_name>" +
							   " <source_node> <k_value>");
			System.exit(1);
		}

		try {
			Scanner fileScanner = new Scanner(new File(args[0]));
			String sourceNode = args[1];
			int kValue = Integer.parseInt(args[2]);
			int nodeCount = 0;
			boolean isDirected = false;
			boolean hasNegativeEdges = false;
			String directedIndicator;
			String nodeId1;
			String nodeId2;
			Node node1 = null;
			Node node2 = null;
			int edgeWeight;
			ArrayList<Node> nodeSet = new ArrayList<Node>();
			// Output will go to out.txt
			PrintWriter outfile = new PrintWriter("out.txt");

			// Skip past any comments at top of file
			while (fileScanner.hasNext("#.*"))
			{
				fileScanner.nextLine();
			}

			// Read whether graph is directed or undirected
			directedIndicator = new String(fileScanner.next());

			// Check for valid directed or undirected specifier
			if (directedIndicator.equals("D") || directedIndicator.equals("d"))
			{
				isDirected = true;
			}
			else if (directedIndicator.equals("UD") ||
					 directedIndicator.equals("ud"))
			{
				isDirected = false;
			}
			else
			{
				outfile.println("Error: Invalid directed/undirected " +
								   "specifier");
				outfile.close();
				System.exit(1);
			}

			// Loop over the rest of input file (edges)
			while (fileScanner.hasNext())
			{
				boolean isNewNode1 = true;
				boolean isNewNode2 = true;
				nodeId1 = fileScanner.next();
				nodeId2 = fileScanner.next();
				edgeWeight = fileScanner.nextInt();

				// Dijkstra's does not work with negative edge weights,
				//  so report this if negative edge found
				if (edgeWeight < 0)
				{
					hasNegativeEdges = true;
				}

				// See if the nodes of the parsed edge are new nodes
				for (Node v : nodeSet)
				{
					if (v.nodeId.equals(nodeId1))
					{
						// Add the edge from node1 to node2 to node1's
						//  adjacency list
						node1 = v;
						node1.adjList.add(new Edge(nodeId1, nodeId2, edgeWeight));
						isNewNode1 = false;
					}

					if (v.nodeId.equals(nodeId2))
					{
						node2 = v;
						isNewNode2 = false;
					}
				}

				// If node1 is new, need to create a new Node object for it
				//  and add the edge to node2 to its adj list
				if (isNewNode1)
				{
					node1 = new Node(nodeId1);
					node1.adjList.add(new Edge(nodeId1, nodeId2, edgeWeight));
					nodeSet.add(node1);
					++nodeCount;
				}

				// If node2 is new, just create a new node for it
				if (isNewNode2)
				{
					node2 = new Node(nodeId2);
					nodeSet.add(node2);
					++nodeCount;
				}

				// Add the edge from node1 to node2 to node2's inEdges list
				node2.inEdges.add(new Edge(nodeId1, nodeId2, edgeWeight));

				// If the graph is undirected, the edge from node2 to node1
				//  also exists, so add it to node2's adj list
				if (!isDirected)
				{
					// node2 will never be null here, but Java wants this check
					if (node2 != null)
					{
						node2.adjList.add(new Edge(nodeId2, nodeId1, edgeWeight));
					}
					// If undirected, the edge from node1 to node2 should count
					//  as an edge into node1 from node2 as well, so add it to
					//  node1's inEdges list
					node1.inEdges.add(new Edge(nodeId2, nodeId1, edgeWeight));
				}
			}

			fileScanner.close();

			// Check if provided source node is actually in the graph
			boolean invalidSource = true;
			for (Node v : nodeSet)
			{
				if (v.nodeId.equals(sourceNode))
				{
					invalidSource = false;
					break;
				}
			}
			if (invalidSource)
			{
				outfile.println("Source node not found in graph");
				outfile.close();
				System.exit(1);
			}

			ShortestPaths shortestPaths =
					new ShortestPaths(nodeSet, sourceNode, kValue);

			if (hasNegativeEdges)
			{
				outfile.println("Dijkstra does not work with negative edges");
			}
			else
			{
				// findShortestPaths() returns a list of nodes and their
				//  distances as calculated by Dijkstra's algorithm
				ArrayList<Node> dijkstraNodes = shortestPaths.findShortestPaths();

				// Sort the printing of the set of nodes according to node ids
				// Need to implement a new Comparator here because the natural
				//  ordering of nodes is by the dist attribute
				Collections.sort(dijkstraNodes, new Comparator<Node>() {
					@Override
					public int compare(Node n1, Node n2)
					{
						return n1.nodeId.compareTo(n2.nodeId);
					}
				});
				
				// Output each node's shortest path distance from the source
				//  according to Dijkstra's to out.txt
				outfile.println("Dijkstra");
				outfile.println("Source : " + sourceNode);
				for (Node v : dijkstraNodes)
				{
					outfile.print("NODE " + v.nodeId + " : ");
					// If a node's distance is Integer.MAX_VALUE, then it is
					//  unreachable from the source, so print "NR"
					if (v.dist == Integer.MAX_VALUE)
					{
						outfile.println("NR");
					}
					else
					{
						outfile.println(v.dist);
					}
				}
				outfile.println("End Dijkstra");
			}

			// Check if provided k value is invalid
			if (kValue > nodeCount - 1)
			{
				outfile.println("Error: Value of k cannot be greater" +
								" than num vertices minus 1");
				outfile.close();
				System.exit(1);
			}
			else if (kValue < 0)
			{
				outfile.println("Error: Value of k cannot be negative");
				outfile.close();
				System.exit(1);
			}
			// findReliablePaths() returns a set of nodes with distances
			//  calculated according to the shortest reliable paths
			//  dynamic programming algorithm
			ArrayList<Node> reliablePaths = shortestPaths.findReliablePaths();

			// Sort the printing of the set of nodes according to node ids
			// Need to implement a new Comparator here because the natural
			//  ordering of nodes is by the dist attribute
			Collections.sort(reliablePaths, new Comparator<Node>() {
				@Override
				public int compare(Node n1, Node n2)
				{
					return n1.nodeId.compareTo(n2.nodeId);
				}
			});
			
			// Output the shortest path in # of hops (i.e. reliable path) for
			//  each node according to the shortest reliable paths algorithm
			outfile.println("Shortest Reliable Paths Algorithm");
			outfile.println("Integer k : " + kValue + " Source : " +
							sourceNode);
			for (Node v : reliablePaths)
			{
				outfile.print("NODE " + v.nodeId + " : ");
				// If a node's distance is Integer.MAX_VALUE, then it is
				//  unreachable from the source in the specified max number
				//  of hops (k), so print "NR"
				if (v.dist == Integer.MAX_VALUE)
				{
					outfile.println("NR");
				}
				else
				{
					outfile.println(v.dist);
				}
			}
			outfile.println("End Shortest Reliable Paths Algorithm");
			outfile.close();

		} catch (FileNotFoundException fe) {
			System.err.println("Error: Could not open the file.");
			System.exit(1);
		}
	}
}
