package csci561;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class homework {

	private static String method;
	private static int col;
	private static int row;
	private static int[] landing;
	private static int threshold;
	private static int targetNum;
	private static List<int[]> targets;
	private static int[][] geo;
	
	static class Node {	
		int r;
		int c;
		int depth;
		long cost;
		long h;
		Node parent;
		Node(int r, int c, int depth, long cost, long h , Node parent) {
			this.r = r;
			this.c = c;
			this.depth = depth;
			this.cost = cost;
			this.h = h;
			this.parent = parent;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + c;
			result = prime * result + r;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (c != other.c)
				return false;
			if (r != other.r)
				return false;
			return true;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		readFile("testcases/input4.txt");
		long start = System.currentTimeMillis();
		switch (method) {
			case "BFS": BFS() ; break;
			case "UCS": UCS() ; break;
			case "A*": A() ; break;
			default: break;
		}
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println("========================");
		System.out.println("========================");
		System.out.println("time:" + (end - start)  + " ms");
		
	}
	
	private static void readFile(String filename) {
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
		    method = scanner.next();
		    col = scanner.nextInt();
		    row = scanner.nextInt();
		    landing = new int[2];
		    landing[1] = scanner.nextInt();
		    landing[0] = scanner.nextInt();
		    threshold = scanner.nextInt();
		    targetNum = scanner.nextInt();
		  
		    System.out.println("Method: " + method);
		    System.out.println("col: " + col);
		    System.out.println("row: " + row);
		    System.out.println("landing: " + landing[0] + ", " + landing[1]);
		    System.out.println("threshold: " + threshold);
		    System.out.println("targetNum: " + targetNum);
		    
		    targets = new ArrayList<>();
		    for (int i = 0 ; i < targetNum ; i++) {
		    	int[] target = new int[2];
		    	target[1] = scanner.nextInt();
		    	target[0] = scanner.nextInt();
		    	targets.add(target);
		    	System.out.println("target: " + target[0] + ", " + target[1]);
		    }
		    
		    geo = new int[row][col];
		    for (int i = 0 ; i < row ; i++) {
		    	System.out.print("geo" + i + ": ");
		    	for (int j = 0 ; j < col ; j++) {
		    		geo[i][j] = scanner.nextInt();
		    		System.out.print(geo[i][j] + ", ");
		    	}
		    	System.out.print("\n");
		    }
		    
		    System.out.print("\n");
		    System.out.print("\n");
		    System.out.print("========================================\n");
		}
		catch (Exception e) {
			System.out.println("Reading input.txt failed!");
		}
	}

	private static void BFS() throws IOException {
		Queue<Node> q = new LinkedList<>();
		boolean[][] visited = new boolean[row][col];
		int finished = 0;
		Map<Integer, Node> destinations = new HashMap<>();
		q.offer(new Node(landing[0], landing[1], 1, 1, 0, null));
		
		while (!q.isEmpty()) {
			if (finished == targetNum) break;
			Node curr = q.poll();
			int level = curr.depth;
			int nodeRow = curr.r;
			int nodeCol = curr.c;
			int geoVal = geo[nodeRow][nodeCol];
			if (visited[nodeRow][nodeCol]) continue;
			else visited[nodeRow][nodeCol] = true;
			for (int i = 0 ; i < targetNum ; i++) {
				int[] target = targets.get(i);
				if (target[0] == nodeRow && target[1] == nodeCol) {
					finished++;
					destinations.put(i, curr);
				}
			}
			boolean up = nodeRow - 1 < 0 ? false : true;
			boolean down = nodeRow + 1 >= row ? false : true;
			boolean left = nodeCol - 1 < 0 ? false : true;
			boolean right = nodeCol + 1 >= col ? false : true;
			if (up && Math.abs((long)geo[nodeRow - 1][nodeCol] - geoVal) <= threshold)
				q.offer(new Node(nodeRow - 1, nodeCol, level + 1, 1, 0, curr));
			if (down && Math.abs((long)geo[nodeRow + 1][nodeCol] - geoVal) <= threshold)
				q.offer(new Node(nodeRow + 1, nodeCol, level + 1, 1, 0, curr));
			if (left && Math.abs((long)geo[nodeRow][nodeCol - 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow, nodeCol - 1, level + 1, 1, 0, curr));
			if (right && Math.abs((long)geo[nodeRow][nodeCol + 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow, nodeCol + 1, level + 1, 1, 0, curr));
			if (up && left && Math.abs((long)geo[nodeRow - 1][nodeCol - 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow - 1, nodeCol - 1, level + 1, 1, 0, curr));	
			if (down && left && Math.abs((long)geo[nodeRow + 1][nodeCol - 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow + 1, nodeCol - 1, level + 1, 1, 0, curr));
			if (up && right && Math.abs((long)geo[nodeRow - 1][nodeCol + 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow - 1, nodeCol + 1, level + 1, 1, 0, curr));
			if (down && right && Math.abs((long)geo[nodeRow + 1][nodeCol + 1] - geoVal) <= threshold)
				q.offer(new Node(nodeRow + 1, nodeCol + 1, level + 1, 1, 0, curr));
			
		}
		output(destinations);
	}
	
	private static void UCS() throws IOException {
		LinkedList<Node> open = new LinkedList<>();
		Set<Node> closed = new HashSet<>();
		Map<Integer, Node> destinations = new HashMap<>();
		int finished = 0;
		
		open.offer(new Node(landing[0], landing[1], 1, 0, 0, null));
		while (!open.isEmpty()) {
			if (finished == targetNum) break;
			Node curr = open.poll();
			Queue<Node> children = new LinkedList<>();
			int nodeRow = curr.r;
			int nodeCol = curr.c;
			int level = curr.depth;
			long g = curr.cost;
			System.out.println(nodeRow + ", " + nodeCol + " - cost: " + g);
			int geoVal = geo[nodeRow][nodeCol];
			for (int i = 0 ; i < targetNum ; i++) {
				int[] target = targets.get(i);
				if (target[0] == nodeRow && target[1] == nodeCol) {
					destinations.put(i, curr);
					finished++;
				}
			}
			boolean up = nodeRow - 1 < 0 ? false : true;
			boolean down = nodeRow + 1 >= row ? false : true;
			boolean left = nodeCol - 1 < 0 ? false : true;
			boolean right = nodeCol + 1 >= col ? false : true;
			if (up && Math.abs((long)geo[nodeRow - 1][nodeCol] - geoVal) <= threshold)
				children.offer(new Node(nodeRow - 1, nodeCol, level + 1, g + 10, 0, curr));
			if (down && Math.abs((long)geo[nodeRow + 1][nodeCol] - geoVal) <= threshold)
				children.offer(new Node(nodeRow + 1, nodeCol, level + 1, g + 10, 0, curr));
			if (left && Math.abs((long)geo[nodeRow][nodeCol - 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow, nodeCol - 1, level + 1, g + 10, 0, curr));
			if (right && Math.abs((long)geo[nodeRow][nodeCol + 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow, nodeCol + 1, level + 1, g + 10, 0, curr));
			if (up && left && Math.abs((long)geo[nodeRow - 1][nodeCol - 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow - 1, nodeCol - 1, level + 1, g + 14, 0, curr));	
			if (down && left && Math.abs((long)geo[nodeRow + 1][nodeCol - 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow + 1, nodeCol - 1, level + 1, g + 14, 0, curr));
			if (up && right && Math.abs((long)geo[nodeRow - 1][nodeCol + 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow - 1, nodeCol + 1, level + 1, g + 14, 0, curr));
			if (down && right && Math.abs((long)geo[nodeRow + 1][nodeCol + 1] - geoVal) <= threshold)
				children.offer(new Node(nodeRow + 1, nodeCol + 1, level + 1, g + 14, 0, curr));
			while (!children.isEmpty()) {
				Node child = children.poll();
				boolean openHas = false;
				boolean closedHas = false;
				boolean added = false;
				Node openRemoved = null;
				Node closedRemoved = null;
				for (Node node : open) {
					if (node.equals(child)) {
						openHas = true;
						if (child.cost < node.cost) {
							openRemoved = node;
							added = true;
						}
					}
				}
				if (!openHas) {
					for (Node node : closed) {
						if (node.equals(child)) {
							closedHas = true;
							if (child.cost < node.cost) {
								closedRemoved = node;
								added = true;
							}
						}
					}
				}
				if ((!openHas && !closedHas) || added) open.offer(child);
				if (openRemoved != null) open.remove(openRemoved);
				if (closedRemoved != null) closed.remove(closedRemoved);
			}
			closed.add(curr);
			Collections.sort(open, new Comparator<Node>() {
				@Override
				public int compare(Node o1, Node o2) {
					return Long.compare(o1.cost, o2.cost);
				}	
			});
		}
		output(destinations);
	}

	private static void A() throws IOException {
		Map<Integer, Node> destinations = new HashMap<>();
		for (int num = 0 ; num < targetNum ; num++) {
			
			LinkedList<Node> open = new LinkedList<>();
			Set<Node> closed = new HashSet<>();
			Node dest = null;
			
			int[] target = targets.get(num);
			long gInitial = h(landing[0], landing[1], target);
			open.offer(new Node(landing[0], landing[1], 1, 0, gInitial, null));
			
			while (!open.isEmpty()) {
				Node curr = open.poll();
				Queue<Node> children = new LinkedList<>();
				int nodeRow = curr.r;
				int nodeCol = curr.c;
				int level = curr.depth;
				long g = curr.cost;
				int geoVal = geo[nodeRow][nodeCol];
				System.out.println(nodeRow + ", " + nodeCol + " - total: " + (g + curr.h));
				System.out.println(nodeRow + ", " + nodeCol + " - cost: " + g);
				if (target[0] == nodeRow && target[1] == nodeCol) {
					dest = curr;
					break;
				}
				boolean up = nodeRow - 1 < 0 ? false : true;
				boolean down = nodeRow + 1 >= row ? false : true;
				boolean left = nodeCol - 1 < 0 ? false : true;
				boolean right = nodeCol + 1 >= col ? false : true;
				if (up && Math.abs((long)geo[nodeRow - 1][nodeCol] - geoVal) <= threshold)
					children.offer(new Node(nodeRow - 1, nodeCol, level + 1, 
							g + 10 + Math.abs((long)geo[nodeRow - 1][nodeCol] - geoVal), 
							h(nodeRow - 1, nodeCol, target), curr));
				
				if (down && Math.abs((long)geo[nodeRow + 1][nodeCol] - geoVal) <= threshold)
					children.offer(new Node(nodeRow + 1, nodeCol, level + 1, 
							g + 10 + Math.abs((long)geo[nodeRow + 1][nodeCol] - geoVal),
							h(nodeRow + 1, nodeCol, target), curr));
				
				if (left && Math.abs((long)geo[nodeRow][nodeCol - 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow, nodeCol - 1, level + 1, 
							g + 10 + Math.abs((long)geo[nodeRow][nodeCol - 1] - geoVal),
							h(nodeRow, nodeCol - 1, target), curr));
				
				if (right && Math.abs((long)geo[nodeRow][nodeCol + 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow, nodeCol + 1, level + 1, 
							g + 10 + Math.abs((long)geo[nodeRow][nodeCol + 1] - geoVal),
							h(nodeRow, nodeCol + 1, target), curr));
				
				if (up && left && Math.abs((long)geo[nodeRow - 1][nodeCol - 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow - 1, nodeCol - 1, level + 1, 
							g + 14 + Math.abs((long)geo[nodeRow - 1][nodeCol - 1] - geoVal),
							h(nodeRow - 1, nodeCol - 1, target), curr));
				
				if (down && left && Math.abs((long)geo[nodeRow + 1][nodeCol - 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow + 1, nodeCol - 1, level + 1, 
							g + 14 + Math.abs((long)geo[nodeRow + 1][nodeCol - 1] - geoVal),
							h(nodeRow + 1, nodeCol - 1, target), curr));
				
				if (up && right && Math.abs((long)geo[nodeRow - 1][nodeCol + 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow - 1, nodeCol + 1, level + 1, 
							g + 14 + Math.abs((long)geo[nodeRow - 1][nodeCol + 1] - geoVal),
							h(nodeRow - 1, nodeCol + 1, target), curr));
				
				if (down && right && Math.abs((long)geo[nodeRow + 1][nodeCol + 1] - geoVal) <= threshold)
					children.offer(new Node(nodeRow + 1, nodeCol + 1, level + 1, 
							g + 14 + Math.abs((long)geo[nodeRow + 1][nodeCol + 1] - geoVal),
							h(nodeRow + 1, nodeCol + 1, target), curr));
				
				while (!children.isEmpty()) {
					Node child = children.poll();
					boolean openHas = false;
					boolean closedHas = false;
					boolean added = false;
					Node openRemoved = null;
					Node closedRemoved = null;
					for (Node node : open) {
						if (node.equals(child)) {
							openHas = true;
							if (child.cost + child.h < node.cost + node.h) {
								openRemoved = node;
								added = true;
							}
						}
					}
					if (!openHas) {
						for (Node node : closed) {
							if (node.equals(child)) {
								closedHas = true;
								if (child.cost + child.h < node.cost + node.h) {
									closedRemoved = node;
									added = true;
								}
							}
						}
					}
					if ((!openHas && !closedHas) || added) open.offer(child);
					if (openRemoved != null) open.remove(openRemoved);
					if (closedRemoved != null) closed.remove(closedRemoved);
				}
				
				closed.add(curr);
				Collections.sort(open, new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						return Long.compare(o1.cost + o1.h, o2.cost + o2.h);
					}	
				});
			}
			if (dest != null) destinations.put(num, dest);
		}
		output(destinations);
	}
	
	private static long h(int sRow, int sCol, int[] target) {
		int eRow = target[0];
		int eCol = target[1];
		long cost = 0;
		int dig = Math.min(Math.abs(sRow - eRow), Math.abs(sCol - eCol));
		for (int i = 0 ; i < dig ; i++) {
			if (sRow > eRow && sCol > eCol) {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow - 1][sCol - 1]);
				sRow--;
				sCol--;
			}
			else if (sRow > eRow && sCol < eCol) {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow - 1][sCol + 1]);
				sRow--;
				sCol++;
			}
			else if (sRow < eRow && sCol < eCol) {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow + 1][sCol + 1]);
				sRow++;
				sCol++;
			}
			else {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow + 1][sCol - 1]);
				sRow++;
				sCol--;
			}
			cost += 14;
		}
		
		while (sRow != eRow) {
			if (sRow > eRow) {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow - 1][sCol]);
				sRow--;
			}
			else {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow + 1][sCol]);
				sRow++;
			}
			cost += 10;
		}
		
		while (sCol != eCol) {
			if (sCol > eCol) {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow][sCol - 1]);
				sCol--;
			}
			else {
				cost += Math.abs(geo[sRow][sCol] - geo[sRow][sCol + 1]);
				sCol++;
			}
			cost += 10;
		}
		
		return cost;
	}
	
	private static void output(Map<Integer, Node> destinations) throws IOException {
		File file = new File("output.txt");
		if (file.exists()) file.delete();
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt", true));
		
		for (int key = 0 ; key < targetNum ; key++) {
			Node dest = destinations.getOrDefault(key, null);
			if (dest == null) {
				System.out.print("FAIL");
				out.write("FAIL");
			}
			else if (dest.r == landing[0] && dest.c == landing[1]) {
				System.out.print(dest.c + "," + dest.r);
				System.out.print(" ");
				System.out.print(dest.c + "," + dest.r);
				out.write(dest.c + "," + dest.r + " " + dest.c + "," + dest.r);
			}
			else {
				Stack<Node> stack = new Stack<>();
				while (dest != null) {
					stack.push(dest);
					dest = dest.parent;
				}
				
				while (!stack.isEmpty()) {
					Node n = stack.pop();
					System.out.print(n.c + "," + n.r);
					out.write(n.c + "," + n.r);
					if (stack.size() > 0) {
						System.out.print(" ");
						out.write(" ");
					}
				}
			}
			if (key != targetNum - 1) {
				System.out.print("\n");
				out.write("\n");
			}
		}
		out.close();
	}
	
}