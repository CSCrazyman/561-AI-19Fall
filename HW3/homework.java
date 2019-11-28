package csci561;

import java.io.*;
import java.util.*;

public class homework {
	
	private static int queriesNum;
	private static int sentencesNum;
	private static String[] queries;
	private static String[] kb;
	private static Set<Sentence> setAll;
	private static boolean[] results;

	static class Sentence {
		private Set<String> literals;
		private Set<String> variables;
		
		public Sentence() {
			literals = new HashSet<>();
			variables = new HashSet<>();
		}
		
		public void addLiteral(String literal) {
			String literalArgs = literal.substring(literal.indexOf('(') + 1, literal.indexOf(')'));
			String[] args = literalArgs.split(",");
			for (String arg : args) {
				if (arg.length() == 1 && arg.charAt(0) >= 'a' && arg.charAt(0) <= 'z')
					variables.add(arg);
			}
			literals.add(literal);
		}
		
		public boolean removeLiteral(String literal) {
			return literals.remove(literal);
		}
		
		public boolean removeVariables(String var) {
			return variables.remove(var);
		}
		
		public boolean hasVariables() {
			return !variables.isEmpty();
		}
		
		public int variablesNum() {
			return variables.size();
		}
		
		public int literalNum() {
			return literals.size();
		}
		
		public Set<String> getLiterals() {
			return literals;
		}

		public String canEliminate(String query) {
			int idx1 = query.indexOf('(');
			int idx2 = query.indexOf(')');
			boolean negative = query.contains("~");
			boolean found = true;
			String preName = negative ? query.substring(1, idx1) : query.substring(0, idx1);
			String[] args = query.substring(idx1 + 1, idx2).split(",");
			
			for (String literal : literals) {
				Map<Character, String> uniMap1 = new HashMap<>();
				Map<Character, String> uniMap2 = new HashMap<>();
				found = true;
				idx1 = literal.indexOf('(');
				idx2 = literal.indexOf(')');
				boolean subNegative = literal.contains("~");
				String tempPreName = subNegative ? literal.substring(1, idx1) : literal.substring(0, idx1);
				if (preName.equals(tempPreName) && (subNegative != negative)) {
					String[] tempArgs = literal.substring(idx1 + 1, idx2).split(",");
					for (int i = 0 ; i < tempArgs.length ; i++) {
						boolean subFound1 = false;
						boolean subFound2 = false;
						if (args[i].length() == 1) {
							char c = args[i].toCharArray()[0];
							if (c <= 'z' && c >= 'a') {
								if (uniMap1.containsKey(c)) {
									if (!uniMap1.get(c).equals(tempArgs[i])) {
										found = false;
										break;
									}
								}
								else {
									uniMap1.put(c, tempArgs[i]);
									subFound1 = true;
								}
							}
						}
						if (variables.contains(tempArgs[i])) {
							char c = tempArgs[i].toCharArray()[0];
							if (c <= 'z' && c >= 'a') {
								if (uniMap2.containsKey(c)) {
									if (!uniMap2.get(c).equals(args[i])) {
										found = false;
										break;
									}
								}
								else {
									uniMap2.put(c, args[i]);
									subFound2 = true;
								}
							}
						}
						if (subFound1 || subFound2) continue;
						if (!args[i].equals(tempArgs[i])) {
							found = false;
							break;
						}
					}
					if (found) return literal;
				}
			}
			
			return null;
		}
		
		public Map<String, String> canEliminate(Sentence sen) {
			HashMap<String, String> pairs = new HashMap<>();
			for (String str : sen.getLiterals()) {
				String eliminated = canEliminate(str);
				if (eliminated != null) {
					pairs.put(eliminated, str);
					return pairs;
				}
			}
			return pairs;
		}
		
		public Sentence eliminate(Sentence other, Map<String, String> pair) {
			Map<Character, String> uniMap1 = new HashMap<>();	// this sentence -> other
			Map<Character, String> uniMap2 = new HashMap<>();	// other -> this sentence
			List<Character> trackVar = new ArrayList<>();
			String key = "";
			String value = "";
			for (String tempKey : pair.keySet()) {
				key = tempKey;
				value = pair.get(tempKey);
				String[] argKey = key.substring(key.indexOf('(') + 1, key.indexOf(')')).split(",");
				String[] argValue = value.substring(value.indexOf('(') + 1, value.indexOf(')')).split(",");
				int charValue = 'a';
				for (int i = 0 ; i < argKey.length ; i++) {
					if (argKey[i].length() == 1 && argKey[i].charAt(0) >= 'a' && argKey[i].charAt(0) <= 'z'
							&& argValue[i].length() == 1 && argValue[i].charAt(0) >= 'a' && argValue[i].charAt(0) <= 'z') {
						uniMap1.put(argKey[i].charAt(0), ((char)charValue) + "");
						uniMap2.put(argValue[i].charAt(0), ((char)charValue) + "");
						trackVar.add((char)charValue);
						charValue++;
					}
					else if (argKey[i].length() == 1 && argKey[i].charAt(0) >= 'a' && argKey[i].charAt(0) <= 'z') {
						uniMap1.put(argKey[i].charAt(0), argValue[i]);
					}
					else if (argValue[i].length() == 1 && argValue[i].charAt(0) >= 'a' && argValue[i].charAt(0) <= 'z') {
						uniMap2.put(argValue[i].charAt(0), argKey[i]);
					}
				}
			}
			
			int trackCharNum = 'a';
			trackCharNum += trackVar.size();
			Sentence sentence = new Sentence();
			for (String str : literals) {
				if (str.equals(key)) continue;
				StringBuilder sb = new StringBuilder();
				int idx1 = str.indexOf('(') + 1;
				int idx2 = str.indexOf(')');
				sb.append(str.substring(0, idx1));
				String[] argStr = str.substring(idx1, idx2).split(",");
				for (int i = 0 ; i < argStr.length ; i++) {
					if (argStr[i].length() == 1 && argStr[i].charAt(0) >= 'a' && argStr[i].charAt(0) <= 'z') {
						String backup = argStr[i];
						argStr[i] = uniMap1.get(argStr[i].charAt(0));
						if (argStr[i] == null) {
							uniMap1.put(backup.charAt(0), ((char)trackCharNum) + "");
							argStr[i] = ((char)trackCharNum) + "";
							trackCharNum++;
						}
					}
					sb.append(argStr[i]);
					if (i != argStr.length - 1) sb.append(",");
				}
				sb.append(")");
				sentence.addLiteral(sb.toString());
			}
			
			for (String str : other.getLiterals()) {
				if (str.equals(value)) continue;
				StringBuilder sb = new StringBuilder();
				int idx1 = str.indexOf('(') + 1;
				int idx2 = str.indexOf(')');
				sb.append(str.substring(0, idx1));
				String[] argStr = str.substring(idx1, idx2).split(",");
				for (int i = 0 ; i < argStr.length ; i++) {
					if (argStr[i].length() == 1 && argStr[i].charAt(0) >= 'a' && argStr[i].charAt(0) <= 'z') {
						String backup = argStr[i];
						argStr[i] = uniMap2.get(argStr[i].charAt(0));
						if (argStr[i] == null) {
							uniMap2.put(backup.charAt(0), ((char)trackCharNum) + "");
							argStr[i] = ((char)trackCharNum) + "";
							trackCharNum++;
						}
					}
					sb.append(argStr[i]);
					if (i != argStr.length - 1) sb.append(",");
				}
				sb.append(")");
				sentence.addLiteral(sb.toString());
			}
			
			return sentence;
		}
	
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("This sentence has " + variablesNum() + " variables\n");
			int size = literals.size();
			int i = 0;
			for (String literal : literals) {
				sb.append(literal);
				if (i++ != size - 1) sb.append(" | ");
			}
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((literals == null) ? 0 : literals.hashCode());
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
			Sentence other = (Sentence) obj;
			if (literals == null) {
				if (other.literals != null)
					return false;
			} else if (!literals.equals(other.literals))
				return false;
			return true;
		}
			
	}
	
	public static void main(String[] args) throws IOException {
		readFile("testcases/input49.txt");
		showInput();
		generateSentences();
		beginCheck();
		output(results);
		showOutput();
	}
	
	private static void readFile(String filename) {
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter("\n"); 
			
			queriesNum = scanner.nextInt();
			queries = new String[queriesNum];
			for (int i = 0 ; i < queriesNum ; i++) {
				String query = scanner.next();
				queries[i] = query;
			}
			
			sentencesNum = scanner.nextInt();
			kb = new String[sentencesNum];
			for (int i = 0 ; i < sentencesNum ; i++) {
				String sentence = scanner.next();
				kb[i] = sentence;
			}
			
			scanner.close();
		}
		catch (Exception e) {
			System.out.println("Reading failed!");
		}
	}
	
	private static void generateSentences() {
		setAll = new HashSet<>();
		for (String single : kb) {
			generateSingle(single);
 		}
	}
	
	private static void generateSingle(String s) {
		Sentence sentence = new Sentence();
		if (s.contains("=>")) {
			String[] parts = s.split("=>");
			String[] part1s = parts[0].split("&");
			for (String part1 : part1s) {
				part1 = part1.trim();
				if (part1.charAt(0) == '~') part1 = part1.substring(1);
				else part1 = "~" + part1;
				sentence.addLiteral(part1);
			}
			sentence.addLiteral(parts[1].trim());
		}
		else {
			sentence.addLiteral(s.trim());
		}
		setAll.add(sentence);
	}
	
	private static void beginCheck() {
		results = new boolean[queries.length];
		for (int idx = 0 ; idx < queries.length ; idx++)
			beginSingleCheck(queries[idx], idx);
	}
	
	private static void beginSingleCheck(String query, int idx) {
		Sentence initialSentence = new Sentence();
		String contradict = query.contains("~") ? query.substring(1) : "~" + query;
		initialSentence.addLiteral(contradict);
		results[idx] = resolution(initialSentence);
	}
	
	private static boolean resolution(Sentence sen) {
		Set<Sentence> alreadyDetected = new HashSet<>();
		return dfs(sen, alreadyDetected);
	}
	
	private static boolean dfs(Sentence sen, Set<Sentence> visited) {
		if (sen.literalNum() == 0) return true;
		visited.add(sen);
		for (Sentence temp : setAll) {
			Map<String, String> pair = temp.canEliminate(sen);
			if (pair.size() != 0) {
				Sentence resSentence = temp.eliminate(sen, pair);
				if (visited.contains(temp) || visited.contains(resSentence) || resSentence.canEliminate(resSentence).size() != 0) continue;
				visited.add(temp);
				if (dfs(resSentence, visited)) return true;
				visited.remove(temp);
			}
		}
		visited.remove(sen);
		return false;
	}

	private static void output(boolean[] res) throws IOException {
		File file = new File("output.txt");
		if (file.exists()) file.delete();
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt", true));
		for (int i = 0 ; i < res.length ; i++) {
			if (res[i]) out.write("TRUE");
			else out.write("FALSE");
			if (i != res.length - 1) out.write("\n");
		}
		out.close();
	}
	
	/* -------------------------------------------------- */
	/* ------------- Auxiliary Functions ---------------- */
	/* -------------------------------------------------- */
	private static void showInput() {
		System.out.println("Queries: ");
		for (int i = 0 ; i < queriesNum ; i++) {
			System.out.println("Query " + (i + 1) + " : " + queries[i]);
		}
		System.out.println("Knowledge Bases: ");
		for (int i = 0 ; i < sentencesNum ; i++) {
			System.out.println("Sentence " + (i + 1) + " : " + kb[i]);
		}
		System.out.println("==============================");
		System.out.println();
	}
	
	private static void showOutput() {
		System.out.println("+++++++++++++++++++++");
		System.out.println("Outputs: ");
		for (int i = 0 ; i < results.length ; i++) {
			System.out.println((i + 1) + ".  " +queries[i] + " result is ---- " + results[i]);
		}
		System.out.println("+++++++++++++++++++++");
	}

}