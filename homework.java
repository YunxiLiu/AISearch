package homework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class homework {
	
	private final static String INPUTFILENAME = "input.txt";
	private final static String OUTPUTFILENAME = "output.txt";
	private static int method; // BFS:0 DFS:1 SA:2
	private static int size;
	private static int lizardNum;
	private static int treeNum;
	private static char[][] output;
	private static boolean isSuccess;
	private static List<int[]> saCurr;
	private static Set<Integer> saCurrSet;
	private static int saNeiIndex;
	private static int[] saNeiPos;
	private static int saCurrCost;
	private static int saNextCost;
	
	private static int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

	
	private static void doBFS() {
		//System.out.println("BFS:");
		Queue<List<Position>> queue = new ArrayDeque<>();
		queue.offer(new ArrayList<>());
		int currentRow = 0;
		int currentCol = 0;
		while (currentRow != size) { // every section
			int end = findEnd(currentRow, currentCol); // section end
			int pathNum = queue.size();
			for (int j = 0; j < pathNum; j++) { // every previous path
				List<Position> path = queue.poll();
				queue.offer(path);
				for (int i = currentCol; i < end; i++) { // every position in the section
					if (isValidBFS(currentRow, i, path)) {
						List<Position> newPath = new ArrayList<>(path);
						newPath.add(new Position(currentRow, i));
						if (newPath.size() == lizardNum) {
							writeOutput(newPath);
							isSuccess = true;
							return;
						}
						queue.offer(newPath);
					}
				}
			}
			// next start
			if (end == size) {
				currentRow++;
				currentCol = 0;
			} else {
				currentCol = end + 1;
			}
		}
		isSuccess = false;
		return;
	}
	
	private static boolean isValidBFS(int row, int col, List<Position> path) {
		Set<Position> set = new HashSet<>();
		for (int i = 0; i < path.size(); i++) {
			set.add(path.get(i));
		}
		for (int i = row - 1; i >= 0; i--) {
			if (output[i][col] == '2') {
				break;
			}
			if (set.contains(new Position(i, col))) {
				return false;
			}
		}
		
		int tempRow = row - 1;
		int tempCol = col - 1;
		while (tempRow >= 0 && tempCol >= 0) {
			if (output[tempRow][tempCol] == '2') {
				break;
			}
			if (set.contains(new Position(tempRow, tempCol))) {
				return false;
			}
			tempRow--;
			tempCol--;
		}
		
		tempRow = row - 1;
		tempCol = col + 1;
		while (tempRow >= 0 && tempCol < size) {
			if (output[tempRow][tempCol] == '2') {
				break;
			}
			if (set.contains(new Position(tempRow, tempCol))) {
				return false;
			}
			tempRow--;
			tempCol++;
		}
		return true;
	}
	
	private static void writeOutput(List<Position> list) {
		for (int i = 0; i < list.size(); i++) {
			Position curr = list.get(i);
			output[curr.x][curr.y] = '1';
		}
	}
	
	
	private static void doDFS() {
		//System.out.println("DFS:");
		dfsHelper(0, 0, lizardNum);
	}
	
	private static boolean dfsHelper(int currentRow, int currentCol, int lizardRemain) { // input(tree pos),
		if (lizardRemain == 0) {
			isSuccess = true;
			return true;
		}
		if (currentRow == size) {
			isSuccess = false;
			return false;
		}
		int end = findEnd(currentRow, currentCol);
		for (int i = currentCol; i < end; i++) {
			if (isValid(currentRow, i)) {
				output[currentRow][i] = '1';
				if (end == size) {
					if (dfsHelper(currentRow + 1, 0, lizardRemain - 1)) {
						return true;
					}
				} else {
					if (dfsHelper(currentRow, end + 1, lizardRemain - 1)) {
						return true;
					}
				}
				output[currentRow][i] = '0';
			}
		}
		
		// put no lizard at this section
		if (end == size) {
			if (dfsHelper(currentRow + 1, 0, lizardRemain)) {
				return true;
			}
		} else {
			if (dfsHelper(currentRow, end + 1, lizardRemain)) {
				return true;
			}
		}
		return false;
	}
	
	private static int findEnd(int row, int col) {
		while (col < size && output[row][col] != '2') {
			col++;
		}
		return col;
	}
	
	private static boolean isValid(int row, int col) {
		if (output[row][col] == '2') {
			return false;
		}
		for (int i = row - 1; i >= 0; i--) {
			if (output[i][col] == '2') {
				break;
			}
			if (output[i][col] == '1') {
				return false;
			}
		}
		
		int tempRow = row - 1;
		int tempCol = col - 1;
		while (tempRow >= 0 && tempCol >= 0) {
			if (output[tempRow][tempCol] == '2') {
				break;
			}
			if (output[tempRow][tempCol] == '1') {
				return false;
			}
			tempRow--;
			tempCol--;
		}
		
		tempRow = row - 1;
		tempCol = col + 1;
		while (tempRow >= 0 && tempCol < size) {
			if (output[tempRow][tempCol] == '2') {
				break;
			}
			if (output[tempRow][tempCol] == '1') {
				return false;
			}
			tempRow--;
			tempCol++;
		}
		
		return true;
	}
	
	
	private static void doSA() {
	    for (int i = 0; i < 100; i++) {
	        initial();
	        
	        if (simulated()) {
	        	for (int[] currPair : saCurr) {
	        		output[currPair[0]][currPair[1]] = '1';
	        	}
	        	isSuccess = true;
	            return;
	        }
	        //System.out.println("i = " + i);
	    }
	}
	
	private static boolean simulated() {
		for (double temperature = 10; (temperature > 0) && (saCurrCost != 0); temperature-=0.0001) {
            if (!saGetNextState()) {
            	return false;
            }
            int delta = saCurrCost - saNextCost;
            double probability = Math.exp(delta / temperature);
            if (delta < 0 && probability > 1) {
            	continue;
            }
            //System.out.print("temp:" + temperature + " ");
            //System.out.print("delta:" + delta + " ");
            //System.out.println("prob:" + probability);
            double rand = Math.random();

            if (delta >= 0 || rand < probability) {
                saCurrCost = saNextCost;
                int neiSetVal = saNeiPos[0] * size + saNeiPos[1];
                int[] currLizard = saCurr.get(saNeiIndex);
                saCurrSet.remove(currLizard[0] * size + currLizard[1]);
                saCurrSet.add(neiSetVal);
                saCurr.set(saNeiIndex, saNeiPos);
            }
        }
		if (saCurrCost == 0) {
			return true;
		}
		return false;
	}
	
	private static boolean saGetNextState() {
		for (int i = 0; i < 1000; i++) {
			int randLizard = (int) (Math.random() * lizardNum); // randomly pick a lizard
			int[] currPos = saCurr.get(randLizard);
			int randDir = (int) (Math.random() * 4);
			int newPosRow = currPos[0] + dirs[randDir][0];
			int newPosCol = currPos[1] + dirs[randDir][1];
			if (newPosRow >= 0 && newPosRow < size && newPosCol >= 0 && newPosCol < size && output[newPosRow][newPosCol] == '0') {
				int newSetVal = newPosRow * size + newPosCol;
				if (!saCurrSet.contains(newSetVal)) { // available nei
					saNeiIndex = randLizard;
					saNeiPos = new int[]{newPosRow, newPosCol};
					
					int[] temp = saCurr.get(saNeiIndex);
					int tempSetVal = currPos[0] * size + currPos[1];
					saCurrSet.remove(tempSetVal);
					saCurrSet.add(newSetVal);
					saCurr.set(saNeiIndex, saNeiPos);
					
					saNextCost = saGetCost();
					
					saCurr.set(saNeiIndex, temp);
					saCurrSet.remove(newSetVal);
					saCurrSet.add(tempSetVal);
					return true;
				}
			}
		}
		return false;
	}
	
	private static int saGetCost() {
		int cost = 0;
		for (int i = 0; i < lizardNum; i++) {
			int[] pair = saCurr.get(i);
			for (int j = pair[0] - 1; j >= 0; j--) { // up
				if (output[j][pair[1]] == '2') {
					break;
				}
				if (saCurrSet.contains(j * size + pair[1])) {
					cost++;
				}
			}
			
			for (int j = pair[0] + 1; j < size; j++) { // down
				if (output[j][pair[1]] == '2') {
					break;
				}
				if (saCurrSet.contains(j * size + pair[1])) {
					cost++;
				}
			}
			
			for (int j = pair[1] - 1; j >= 0; j--) { // left
				if (output[pair[0]][j] == '2') {
					break;
				}
				if (saCurrSet.contains(pair[0] * size + j)) {
					cost++;
				}
			}
			
			for (int j = pair[1] + 1; j < size; j++) { // right
				if (output[pair[0]][j] == '2') {
					break;
				}
				if (saCurrSet.contains(pair[0] * size + j)) {
					cost++;
				}
			}
			
			for (int j = 1; pair[0] - j >= 0 && pair[1] - j >= 0; j++) { // leftUp
				if (output[pair[0] - j][pair[1] - j] == '2') {
					break;
				}
				if (saCurrSet.contains((pair[0] - j) * size + pair[1] - j)) {
					cost++;
				}
			}
			
			for (int j = 1; pair[0] - j >= 0 && pair[1] + j < size; j++) { // leftDown
				if (output[pair[0] - j][pair[1] + j] == '2') {
					break;
				}
				if (saCurrSet.contains((pair[0] - j) * size + pair[1] + j)) {
					cost++;
				}
			}
			
			for (int j = 1; pair[0] - j >= 0 && pair[1] + j < size; j++) { // rightUp
				if (output[pair[0] - j][pair[1] + j] == '2') {
					break;
				}
				if (saCurrSet.contains((pair[0] - j) * size + pair[1] + j)) {
					cost++;
				}
			}
			
			for (int j = 1; pair[0] + j < size && pair[1] + j < size; j++) { // rightDown
				if (output[pair[0] + j][pair[1] + j] == '2') {
					break;
				}
				if (saCurrSet.contains((pair[0] + j) * size + pair[1] + j)) {
					cost++;
				}
			}
		}
		return cost;
	}
	
	private static void initial() {
		saCurr = new ArrayList<>();
		saCurrSet = new HashSet<>();
	    for (int i = 0; i < lizardNum; i++) {
	    	while (true) {
	    		int num = (int)(Math.random() * (size * size));
	    		int newRow = num / size;
	        	int newCol = num % size;
	        	if (output[newRow][newCol] == '2') {
	        		continue;
	        	}
	    		if (!saCurrSet.add(num)) {
	    			continue;
	    		}
	        	
	        	saCurr.add(new int[]{newRow, newCol});
	        	break;
	    	}
	    }
	    saCurrCost = saGetCost();
	}
	
	
	
	private static void parseFile() {
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(INPUTFILENAME);
			br = new BufferedReader(fr);
			
			String line;
			// parse method
			line = br.readLine();
			if (line.equals("BFS")) {
				method = 0;
			} else if (line.equals("DFS")) {
				method = 1;
			} else if (line.equals("SA")) {
				method = 2;
			}
			
			// parse size
			line = br.readLine();
			size = Integer.parseInt(line);
			
			// parse num of lizards
			line = br.readLine();
			lizardNum = Integer.parseInt(line);
			
			// parse nursery
			output = new char[size][size];
			for (int i = 0; (line = br.readLine()) != null; i++) {
				for (int j = 0; j < size; j++) {
					if (line.charAt(j) == '2') {
						treeNum++;
					}
					output[i][j] = line.charAt(j);
				}
			}
			
			// test parse result
			/*
			System.out.println("method:" + method);
			System.out.println("size:" + size);
			System.out.println("lizard:" + lizardNum);
			for (int i = 0; i < output.length; i++) {
				for (int j = 0; j < output[0].length; j++) {
					System.out.print(output[i][j]);
				}
				System.out.println();
			}*/
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void writeFile() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter(OUTPUTFILENAME);
			bw = new BufferedWriter(fw);
			if (!isSuccess) {
				bw.write("FAIL");
				System.out.println("FAIL!");
			} else {
				bw.write("OK\n");
				for (int i = 0; i < size; i++) {
					StringBuilder currLine = new StringBuilder();
					for (int j = 0; j < size; j++) {
						currLine.append(output[i][j]);
						System.out.print(output[i][j] + " ");
					}
					System.out.println();
					bw.write(currLine.toString());
					bw.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		// parse file, get rawNursery, n, p, method
		parseFile();
		
		
		if ((treeNum == 0 && (lizardNum > size || (lizardNum == size && size >=2 && size <= 3))) || (treeNum + lizardNum > size * size)) {
			isSuccess = false;
			writeFile();
			long endTime = System.currentTimeMillis();
			System.out.println("Took "+ (endTime - startTime) + " ms");
			return;
		}
		
		// doBFS, DFS, SA
		switch (method) {
        	case 0:  
        		doBFS();
        		break;
        	case 1: 
        		doDFS();
        		break;
        	case 2:  
        		doSA();
                break;
        	default: 
        		return;
		}
		
		// get a nursery, write file
		writeFile();
		long endTime = System.currentTimeMillis();
		System.out.println("Took "+ (endTime - startTime) + " ms");
	}
	
	private static class Position {
		int x;
		int y;
		Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode( ) {
			return x * 31 + y;
		}
		
		@Override
		public boolean equals(Object that) {
			if (!(that instanceof Position)) {
				return false;
			}
			Position thatPos = (Position) that;
			if (thatPos.x == this.x && thatPos.y == this.y) {
				return true;
			}
			return false;
		}
	}
}
