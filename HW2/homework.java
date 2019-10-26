import java.io.*;
import java.util.*;

public class homework {
	
	private static String method;
	private static String role;
	private static float gameTime;
	private static int[][] board;
	private static Board initialBoard;
	private static String step;
	
	public static final int EMPTY = 0;
	public static final int BLACK = 1;
	public static final int WHITE = 2;
	public static final int SIZE = 16;
	public static final int[][] MOVE_DIRECTIONS = {
			{-1, -1}, {-1, 0}, {-1, 1}, {0, 1},
			{1, 1}, {1, 0}, {1, -1}, {0, -1}
	};
	public static final int[][] JUMP_DIRECTIONS = {
			{-2, -2}, {-2, 0}, {-2, 2}, {0, 2},
			{2, 2}, {2, 0}, {2, -2}, {0, -2}
	};
	
	static class Board {
		int depth;
		int[][] board;
		int fromRow;
		int fromCol;
		int toRow;
		int toCol;
		Board jumpParent;
		Board(int[][] board, int depth, int fromRow, int fromCol, int toRow, int toCol, Board jumpParent) {
			this.board = new int[SIZE][SIZE];
			for (int i = 0 ; i < SIZE ; i++) {
				for (int j = 0 ; j < SIZE ; j++) {
					this.board[i][j] = board[i][j];
				}
			}
			this.depth = depth;
			this.fromRow = fromRow;
			this.fromCol = fromCol;
			this.toRow = toRow;
			this.toCol = toCol;
			this.jumpParent = jumpParent;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.deepHashCode(board);
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
			Board other = (Board) obj;
			if (!Arrays.deepEquals(board, other.board))
				return false;
			return true;
		}
	}
	
	public static void main(String[] args) throws IOException {
		readFile("input.txt");
		int r = role.equals("WHITE") ? WHITE : BLACK;
		int next = role.equals("WHITE") ? BLACK : WHITE;
		int singleLimit = 2, gameLimit = 2;
		boolean isInitial = initial(initialBoard);

		if (method.equals("SINGLE")) {
			if (gameTime >= 200.0) singleLimit = 5;
			else if (gameTime >= 50.0) singleLimit = 4;
			else if (gameTime >= 12.0) singleLimit = 3;
		}

		long start = System.currentTimeMillis();
		switch (method) {
			case "SINGLE": single(r, next, singleLimit, isInitial) ; break;
			case "GAME": game(r, next, gameLimit, isInitial) ; break;
			default: break;
		}
		long end = System.currentTimeMillis();
		output(step);
		System.out.println("time:" + (end - start)  + " ms");
	}
	
	private static void readFile(String filename) {
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			board = new int[SIZE][SIZE];
		    method = scanner.next();
		    role = scanner.next();
		    gameTime = scanner.nextFloat();
		    for (int i = 0 ; i < SIZE ; i++) {
		    	String row = scanner.next();
		    	String[] status = row.split("");
		    	for (int j = 0 ; j < status.length ; j++) {
		    		if (status[j].equals("B")) board[i][j] = BLACK;
		    		else if (status[j].equals("W")) board[i][j] = WHITE;
		    		else board[i][j] = EMPTY;
		    	}
		    }
		    initialBoard = new Board(board, 0, -1, -1, -1, -1, null);
		}
		catch (Exception e) {
			System.out.println("Reading input.txt failed!");
		}
	}

	private static void single(int role, int next, int dpLimit, boolean isInitial) throws IOException {
		minValue(initialBoard, role, role, next, dpLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, isInitial);
	}
	
	private static void game(int role, int next, int dpLimit, boolean isInitial) throws IOException {
		minValue(initialBoard, role, role, next, dpLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, isInitial);
	}

	private static int maxValue(Board b, int player, int curP, int nextP, int limit, int max, int min, boolean isInitial) {
		if (b.depth > limit) return eval(b, player, b.depth);
		if (eval(b, player, 0) == 0 && !isInitial) return eval(b, player, b.depth);
		int value = Integer.MIN_VALUE;
		int counts = 0;
		boolean[] insideMove = insideLegal(b, curP);
		if (curP == BLACK) {
			for (int k = 0; k < SIZE * 2 - 1; k++) {
				for (int j = 0; j <= k; j++) {
					if (counts == 19) break;
					int i = k - j;
					if (i < SIZE && j < SIZE) {
						if (b.board[i][j] == curP) {
							counts++;
							for (int m = 0; m < 8; m++) {
								if (canMove(b, m, i, j)) {
									int newR = i + MOVE_DIRECTIONS[m][0];
									int newC = j + MOVE_DIRECTIONS[m][1];
									Board temp = new Board(b.board, b.depth + 1, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									if (isLegal(curP, insideMove, i, j, newR, newC)) {
										value = Math.max(value, minValue(temp, player, nextP, curP, limit, max, min, isInitial));
										if (value >= min) return value;
										max = Math.max(max, value);
									}
								}

								if (canJump(b, m, i, j)) {
									Set<Board> visited = new HashSet<>();
									visited.add(b);
									int newR = i + JUMP_DIRECTIONS[m][0];
									int newC = j + JUMP_DIRECTIONS[m][1];
									int newDepth = b.depth + 1;
									Board temp = new Board(b.board, newDepth, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									Queue<Board> q = new LinkedList<>();
									q.offer(temp);
									while (!q.isEmpty()) {
										Board t = q.poll();
										if (visited.contains(t)) continue;
										visited.add(t);
										int toR = t.toRow, toC = t.toCol;
										if (isLegal(curP, insideMove, i, j, toR, toC)) {
											value = Math.max(value, minValue(t, player, nextP, curP, limit, max, min, isInitial));
											if (value >= min) return value;
											max = Math.max(max, value);
										}
										for (int tm = 0; tm < 8; tm++) {
											if (canJump(t, tm, toR, toC)) {
												newR = toR + JUMP_DIRECTIONS[tm][0];
												newC = toC + JUMP_DIRECTIONS[tm][1];
												Board child = new Board(t.board, newDepth, toR, toC, newR, newC, t);
												child.board[toR][toC] = EMPTY;
												child.board[newR][newC] = curP;
												q.offer(child);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else {
			for (int k = 2 * SIZE - 2 ; k >= 0 ; k--) {
				for (int j = k ; j >= 0 ; j--) {
					if (counts == 19) break;
					int i = k - j;
					if (i < SIZE && j < SIZE) {
						if (b.board[i][j] == curP) {
							counts++;
							for (int m = 0; m < 8; m++) {
								if (canMove(b, m, i, j)) {
									int newR = i + MOVE_DIRECTIONS[m][0];
									int newC = j + MOVE_DIRECTIONS[m][1];
									Board temp = new Board(b.board, b.depth + 1, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									if (isLegal(curP, insideMove, i, j, newR, newC)) {
										value = Math.max(value, minValue(temp, player, nextP, curP, limit, max, min, isInitial));
										if (value >= min) return value;
										max = Math.max(max, value);
									}
								}

								if (canJump(b, m, i, j)) {
									Set<Board> visited = new HashSet<>();
									visited.add(b);
									int newR = i + JUMP_DIRECTIONS[m][0];
									int newC = j + JUMP_DIRECTIONS[m][1];
									int newDepth = b.depth + 1;
									Board temp = new Board(b.board, newDepth, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									Queue<Board> q = new LinkedList<>();
									q.offer(temp);
									while (!q.isEmpty()) {
										Board t = q.poll();
										if (visited.contains(t)) continue;
										visited.add(t);
										int toR = t.toRow, toC = t.toCol;
										if (isLegal(curP, insideMove, i, j, toR, toC)) {
											value = Math.max(value, minValue(t, player, nextP, curP, limit, max, min, isInitial));
											if (value >= min) return value;
											max = Math.max(max, value);
										}
										for (int tm = 0; tm < 8; tm++) {
											if (canJump(t, tm, toR, toC)) {
												newR = toR + JUMP_DIRECTIONS[tm][0];
												newC = toC + JUMP_DIRECTIONS[tm][1];
												Board child = new Board(t.board, newDepth, toR, toC, newR, newC, t);
												child.board[toR][toC] = EMPTY;
												child.board[newR][newC] = curP;
												q.offer(child);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return value;
	}

	private static int minValue(Board b, int player, int curP, int nextP, int limit, int max, int min, boolean isInitial) {
		if (b.depth > limit) return eval(b, player, b.depth);
		if (eval(b, player, 0) == 0 && !isInitial) return -1000000;
		int value = Integer.MAX_VALUE;
		int counts = 0;
		boolean[] insideMove = insideLegal(b, curP);
		if (curP == BLACK) {
			for (int k = 0; k < SIZE * 2 - 1; k++) {
				for (int j = 0; j <= k; j++) {
					if (counts == 19) break;
					int i = k - j;
					if (i < SIZE && j < SIZE) {
						if (b.board[i][j] == curP) {
							counts++;
							for (int m = 0; m < 8; m++) {
								if (canMove(b, m, i, j)) {
									int newR = i + MOVE_DIRECTIONS[m][0];
									int newC = j + MOVE_DIRECTIONS[m][1];
									Board temp = new Board(b.board, b.depth + 1, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									if (isLegal(curP, insideMove, i, j, newR, newC)) {
										int tempValue = maxValue(temp, player, nextP, curP, limit, max, min, isInitial);
										if (tempValue < value) {
											value = tempValue;
											if (b.depth == 0) step = "E " + j + "," + i + " " + newC + "," + newR;
										}
										if (value <= max) return value;
										min = Math.min(min, value);
									}
								}

								if (canJump(b, m, i, j)) {
									Set<Board> visited = new HashSet<>();
									visited.add(b);
									int newR = i + JUMP_DIRECTIONS[m][0];
									int newC = j + JUMP_DIRECTIONS[m][1];
									int newDepth = b.depth + 1;
									Board temp = new Board(b.board, newDepth, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									Queue<Board> q = new LinkedList<>();
									q.offer(temp);
									while (!q.isEmpty()) {
										Board t = q.poll();
										if (visited.contains(t)) continue;
										visited.add(t);
										int toR = t.toRow, toC = t.toCol;
										if (isLegal(curP, insideMove, i, j, toR, toC)) {
											int tempValue = maxValue(t, player, nextP, curP, limit, max, min, isInitial);
											if (tempValue < value) {
												value = tempValue;
												if (b.depth == 0) {
													Board ttemp = t;
													Stack<String> stack = new Stack<>();
													while (ttemp != null) {
														stack.push("J " + ttemp.fromCol + "," + ttemp.fromRow + " " + ttemp.toCol + "," + ttemp.toRow);
														ttemp = ttemp.jumpParent;
													}
													StringBuilder sb = new StringBuilder();
													while (!stack.isEmpty()) {
														sb.append(stack.pop());
														if (!stack.isEmpty()) sb.append("\n");
													}
													step = sb.toString();
												}
											}
											if (value <= max) return value;
											min = Math.min(min, value);
										}
										for (int tm = 0; tm < 8; tm++) {
											if (canJump(t, tm, toR, toC)) {
												newR = toR + JUMP_DIRECTIONS[tm][0];
												newC = toC + JUMP_DIRECTIONS[tm][1];
												Board child = new Board(t.board, newDepth, toR, toC, newR, newC, t);
												child.board[toR][toC] = EMPTY;
												child.board[newR][newC] = curP;
												q.offer(child);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else {
			for (int k = 2 * SIZE - 2 ; k >= 0 ; k--) {
				for (int j = k ; j >= 0 ; j--) {
					if (counts == 19) break;
					int i = k - j;
					if (i < SIZE && j < SIZE) {
						if (b.board[i][j] == curP) {
							counts++;
							for (int m = 0; m < 8; m++) {
								if (canMove(b, m, i, j)) {
									int newR = i + MOVE_DIRECTIONS[m][0];
									int newC = j + MOVE_DIRECTIONS[m][1];
									Board temp = new Board(b.board, b.depth + 1, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									if (isLegal(curP, insideMove, i, j, newR, newC)) {
										int tempValue = maxValue(temp, player, nextP, curP, limit, max, min, isInitial);
										if (tempValue < value) {
											value = tempValue;
											if (b.depth == 0) step = "E " + j + "," + i + " " + newC + "," + newR;
										}
										if (value <= max) return value;
										min = Math.min(min, value);
									}
								}

								if (canJump(b, m, i, j)) {
									Set<Board> visited = new HashSet<>();
									visited.add(b);
									int newR = i + JUMP_DIRECTIONS[m][0];
									int newC = j + JUMP_DIRECTIONS[m][1];
									int newDepth = b.depth + 1;
									Board temp = new Board(b.board, newDepth, i, j, newR, newC, null);
									temp.board[i][j] = EMPTY;
									temp.board[newR][newC] = curP;
									Queue<Board> q = new LinkedList<>();
									q.offer(temp);
									while (!q.isEmpty()) {
										Board t = q.poll();
										if (visited.contains(t)) continue;
										visited.add(t);
										int toR = t.toRow, toC = t.toCol;
										if (isLegal(curP, insideMove, i, j, toR, toC)) {
											int tempValue = maxValue(t, player, nextP, curP, limit, max, min, isInitial);
											if (tempValue < value) {
												value = tempValue;
												if (b.depth == 0) {
													Board ttemp = t;
													Stack<String> stack = new Stack<>();
													while (ttemp != null) {
														stack.push("J " + ttemp.fromCol + "," + ttemp.fromRow + " " + ttemp.toCol + "," + ttemp.toRow);
														ttemp = ttemp.jumpParent;
													}
													StringBuilder sb = new StringBuilder();
													while (!stack.isEmpty()) {
														sb.append(stack.pop());
														if (!stack.isEmpty()) sb.append("\n");
													}
													step = sb.toString();
												}
											}
											if (value <= max) return value;
											min = Math.min(min, value);
										}
										for (int tm = 0; tm < 8; tm++) {
											if (canJump(t, tm, toR, toC)) {
												newR = toR + JUMP_DIRECTIONS[tm][0];
												newC = toC + JUMP_DIRECTIONS[tm][1];
												Board child = new Board(t.board, newDepth, toR, toC, newR, newC, t);
												child.board[toR][toC] = EMPTY;
												child.board[newR][newC] = curP;
												q.offer(child);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return value;
	}

	private static int eval(Board b, int role, int depth) {
		int value = 0;
		if (role == BLACK) {
			for (int i = 0 ; i < SIZE ; i++) {
				for (int j = 0 ; j < SIZE ; j++) {
					if (b.board[i][j] == BLACK) {
						value += longestDist(b, BLACK, i, j);
					}
					if (b.board[i][j] == WHITE) {
						value -= longestDist(b, WHITE, i, j);
					}
				}
			}
		}
		else {
			for (int i = 0 ; i < SIZE ; i++) {
				for (int j = 0 ; j < SIZE ; j++) {
					if (b.board[i][j] == WHITE) {
						value += longestDist(b, WHITE, i, j);
					}
					if (b.board[i][j] == BLACK) {
						value -= longestDist(b, BLACK, i, j);
					}
				}
			}
		}
		return value + depth;
	}
	
	private static int longestDist(Board b, int role, int x, int y) {
		int longest = 0;
		if (role == BLACK) {
			for (int i = 11 ; i < 16 ; i++) {
				if (b.board[15][i] == EMPTY) {
					int dist = (int)(Math.pow(15 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
				if (b.board[14][i] == EMPTY) {
					int dist = (int)(Math.pow(14 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
			for (int i = 12 ; i < 16 ; i++) {
				if (b.board[13][i] == EMPTY) {
					int dist = (int)(Math.pow(13 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
			for (int i = 13 ; i < 16 ; i++) {
				if (b.board[12][i] == EMPTY) {
					int dist = (int)(Math.pow(12 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
			for (int i = 14 ; i < 16 ; i++) {
				if (b.board[11][i] == EMPTY) {
					int dist = (int)(Math.pow(11 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
		}
		else {
			for (int i = 0 ; i < 5 ; i++) {
				if (b.board[0][i] == EMPTY) {
					int dist = (int)(Math.pow(0 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
				if (b.board[1][i] == EMPTY) {
					int dist = (int)(Math.pow(1 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}	
			}
			for (int i = 0 ; i < 4 ; i++) {
				if (b.board[2][i] == EMPTY) {
					int dist = (int)(Math.pow(2 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
			for (int i = 0 ; i < 3 ; i++) {
				if (b.board[3][i] == EMPTY) {
					int dist = (int)(Math.pow(3 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
			for (int i = 0 ; i < 2 ; i++) {
				if (b.board[4][i] == EMPTY) {
					int dist = (int)(Math.pow(4 - x, 2) + Math.pow(i - y, 2));
					longest = Math.max(longest, dist);
				}
			}
		}
		return longest;
	}

	private static boolean initial(Board b) {
		for (int i = 0 ; i < 5 ; i++) {
			if (b.board[0][i] != BLACK) return false;
			if (b.board[1][i] != BLACK) return false;
		}
		for (int i = 0 ; i < 4 ; i++) {
			if (b.board[2][i] != BLACK) return false;
		}
		for (int i = 0 ; i < 3 ; i++) {
			if (b.board[3][i] != BLACK) return false;
		}
		for (int i = 0 ; i < 2 ; i++) {
			if (b.board[4][i] != BLACK) return false;
		}
		for (int i = 11 ; i < 16 ; i++) {
			if (b.board[15][i] != WHITE) return false;
			if (b.board[14][i] != WHITE) return false;
		}
		for (int i = 12 ; i < 16 ; i++) {
			if (b.board[13][i] != WHITE) return false;
		}
		for (int i = 13 ; i < 16 ; i++) {
			if (b.board[12][i] != WHITE) return false;
		}
		for (int i = 14 ; i < 16 ; i++) {
			if (b.board[11][i] != WHITE) return false;
		}
		return true;
	}
	
	private static boolean canMove(Board b, int idx, int currR, int currC) {
		int newR = MOVE_DIRECTIONS[idx][0] + currR;
		int newC = MOVE_DIRECTIONS[idx][1] + currC;
		if (newR < 0 || newR >= SIZE || newC < 0 || newC >= SIZE || b.board[newR][newC] != EMPTY) return false;
		return true;
	}
	
	private static boolean canJump(Board b, int idx, int currR, int currC) {
		int neighborR = MOVE_DIRECTIONS[idx][0] + currR;
		int neighborC = MOVE_DIRECTIONS[idx][1] + currC;
		if (neighborR < 0 || neighborR >= SIZE || neighborC < 0 || neighborC >= SIZE || b.board[neighborR][neighborC] == EMPTY) return false;
		neighborR = JUMP_DIRECTIONS[idx][0] + currR;
		neighborC = JUMP_DIRECTIONS[idx][1] + currC;
		if (neighborR < 0 || neighborR >= SIZE || neighborC < 0 || neighborC >= SIZE || b.board[neighborR][neighborC] != EMPTY) return false;
		return true;
	}
	
	private static boolean isLegal(int role, boolean[] insideMove, int currR, int currC, int newR, int newC) {
		boolean outside = insideMove[0], inside = insideMove[1];
		if (role == BLACK) {
			if (outside) {
				if (!isInsideB(currR, currC) || isInsideB(newR, newC)) return false;
			}
			else if (inside) {
				if (!isInsideB(currR, currC) || newR < currR || newC < currC) return false;
			}
			else {
				if (!isInsideB(currR, currC) && isInsideB(newR, newC)) return false;
				if (isInsideW(currR, currC) && !isInsideW(newR, newC)) return false;
			}
		}
		else {
			if (outside) {
				if (!isInsideW(currR, currC) || isInsideW(newR, newC)) return false;
			}
			else if (inside) {
				if (!isInsideW(currR, currC) || newR > currR || newC > currC) return false;
			}
			else {
				if (!isInsideW(currR, currC) && isInsideW(newR, newC)) return false;
				if (isInsideB(currR, currC) && !isInsideB(newR, newC)) return false;
			}
		}
		return true;
	}
	
	private static boolean isInsideB(int row, int col) {
		if (row == 0 && col >= 0 && col <= 4) return true;
		if (row == 1 && col >= 0 && col <= 4) return true;
		if (row == 2 && col >= 0 && col <= 3) return true;
		if (row == 3 && col >= 0 && col <= 2) return true;
		if (row == 4 && col >= 0 && col <= 1) return true;
		return false;
	}
	
	private static boolean isInsideW(int row, int col) {
		if (row == 15 && col >= 11 && col <= 15) return true;
		if (row == 14 && col >= 11 && col <= 15) return true;
		if (row == 13 && col >= 12 && col <= 15) return true;
		if (row == 12 && col >= 13 && col <= 15) return true;
		if (row == 11 && col >= 14 && col <= 15) return true;
		return false;
	}
	
	private static boolean[] insideLegal(Board b, int role) {
		boolean[] insideMove = new boolean[2];
		if (role == BLACK) {
			for (int i = 0 ; i < 5 ; i++) {
				if (b.board[0][i] == BLACK) {
					boolean[] temp = checkAround(b, role, 0, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
				if (b.board[1][i] == BLACK) {
					boolean[] temp = checkAround(b, role, 1, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}	
			}
			for (int i = 0 ; i < 4 ; i++) {
				if (b.board[2][i] == BLACK) {
					boolean[] temp = checkAround(b, role, 2, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
			for (int i = 0 ; i < 3 ; i++) {
				if (b.board[3][i] == BLACK) {
					boolean[] temp = checkAround(b, role, 3, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
			for (int i = 0 ; i < 2 ; i++) {
				if (b.board[4][i] == BLACK) {
					boolean[] temp = checkAround(b, role, 4, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
		}
		else {
			for (int i = 11 ; i < 16 ; i++) {
				if (b.board[15][i] == WHITE) {
					boolean[] temp = checkAround(b, role, 15, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
				if (b.board[14][i] == WHITE) {
					boolean[] temp = checkAround(b, role, 14, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
			for (int i = 12 ; i < 16 ; i++) {
				if (b.board[13][i] == WHITE) {
					boolean[] temp = checkAround(b, role, 13, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
			for (int i = 13 ; i < 16 ; i++) {
				if (b.board[12][i] == WHITE) {
					boolean[] temp = checkAround(b, role, 12, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
			for (int i = 14 ; i < 16 ; i++) {
				if (b.board[11][i] == WHITE) {
					boolean[] temp = checkAround(b, role, 11, i);
					if (temp[0]) return temp;
					if (!insideMove[1]) insideMove[1] = temp[1];
				}
			}
		}
		return insideMove;
	}
	
	private static boolean[] checkAround(Board b, int role, int i, int j) {
		boolean[] insideMove = new boolean[2];
		for (int m = 0 ; m < 8 ; m++) { 
			if (canMove(b, m, i, j)) { 
				int newR = i + MOVE_DIRECTIONS[m][0];
				int newC = j + MOVE_DIRECTIONS[m][1];
				if (role == BLACK) {
					if (!isInsideB(newR, newC)) insideMove[0] = true;
					if (isInsideB(newR, newC) && (newR >= i && newC >= j)) insideMove[1] = true;
				}
				else {
					if (!isInsideW(newR, newC)) insideMove[0] = true;
					if (isInsideW(newR, newC) && (newR <= i && newC <= j)) insideMove[1] = true;
				}
				if (insideMove[0]) return insideMove;
			}
				
			if (canJump(b, m, i, j)) {
				Set<Board> visited = new HashSet<>();
				visited.add(b);
				int newR = i + JUMP_DIRECTIONS[m][0];
				int newC = j + JUMP_DIRECTIONS[m][1];
				int newDepth = b.depth + 1;
				Board temp = new Board(b.board, newDepth, i, j, newR, newC, null);
				temp.board[i][j] = EMPTY;
				temp.board[newR][newC] = role;
				Queue<Board> q = new LinkedList<>();
				q.offer(temp);
				while (!q.isEmpty()) {
					Board t = q.poll();
					if (visited.contains(t)) continue;
					visited.add(t);
					int toR = t.toRow, toC = t.toCol;
					if (role == BLACK) {
						if (!isInsideB(toR, toC)) insideMove[0] = true;
						if (isInsideB(toR, toC) && (toR >= i && toC >= j)) insideMove[1] = true;
					}
					else {
						if (!isInsideW(toR, toC)) insideMove[0] = true;
						if (isInsideW(toR, toC) && (toR <= i && toC <= j)) insideMove[1] = true;
					}
					if (insideMove[0]) return insideMove;
					for (int tm = 0 ; tm < 8 ; tm++) {
						if (canJump(t, tm, toR, toC)) {
							newR = toR + JUMP_DIRECTIONS[tm][0];
							newC = toC + JUMP_DIRECTIONS[tm][1];
							Board child = new Board(t.board, newDepth, toR, toC, newR, newC, t);
							child.board[toR][toC] = EMPTY;
							child.board[newR][newC] = role;
							q.offer(child);
						}
					}
				}
			}
		}
		return insideMove;
	}
		
	private static void output(String ans) throws IOException {
		File file = new File("output.txt");
		if (file.exists()) file.delete();
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt", true));
		System.out.println(ans);
		out.write(ans);
		out.close();
	}

}