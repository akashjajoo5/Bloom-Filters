import java.util.*;
import java.io.FileWriter;
import java.io.BufferedWriter;

class BloomFilters {
    public static void main (String args[]) {
        Scanner sc = new Scanner (System.in);
        
        System.out.println("\nEnter parameters for Bloom and Counting Bloom Filter:\n");
        System.out.println("Enter number of entries to be encoded for Bloom/Counting Bloom Filter");
        int numElem = sc.nextInt();
        System.out.println("Enter number of bits in the filter for Bloom/Counting Bloom Filter");
        int numBits = sc.nextInt();
        System.out.println("Enter number of hashes for Bloom/Counting Bloom Filter");
        int numHashes = sc.nextInt();
        System.out.println("Enter number of elements to be removed for Counting Bloom Filter");
        int numRemove = sc.nextInt();
        System.out.println("Enter number of elements to be added for Counting Bloom Filter");
        int numAdd = sc.nextInt();

        System.out.println("\nEnter parameters for Coded Bloom Filter:\n");
        System.out.println("Enter number of sets");
        int numSets = sc.nextInt();
        System.out.println("Enter number of elements in each set");
        int numElemC = sc.nextInt();
        System.out.println("Enter number of filters");
        int numFilters = sc.nextInt();
        System.out.println("Enter number of bits in each filter");
        int numBitsC = sc.nextInt();
        System.out.println("Enter number of hashes");
        int numHashesC = sc.nextInt();

        int[] bitArray = new int[numBits];
        int[] hash = new int[numHashes];

        Set<Integer> duplicateHashes = new HashSet<>();
        int i = 0;
        while (i < numHashes) {
            int randomNum = getRandom();
            if(!duplicateHashes.contains(randomNum)) {
                hash[i++] = randomNum;
                duplicateHashes.add(randomNum);
            }
        }

        bloomFilter(bitArray, hash, numElem);
        Arrays.fill(bitArray, 0);
        countingBloomFilter(bitArray, hash, numElem, numRemove, numAdd);
        codedBloomFilter(numSets, numElemC, numFilters, numBitsC, numHashesC);
    }

    public static void bloomFilter (int[] bitArray, int[] hash, int numElem) {
        int i = 0;
        int count = 0;

        while (i < numElem) {
            int randomNum = getRandom();
            for (int j = 0; j < hash.length; j++) {
                int hashedVal = (randomNum ^ hash[j]) % bitArray.length;
                if (bitArray[hashedVal] == 0) {
                    bitArray[hashedVal] = 1;
                }
            }
            i++;
        }

        i = 0;
        while (i < numElem) {
            int randomNum = getRandom();
            boolean flag = false;
            for (int j = 0; j < hash.length; j++) {
                int hashedVal = (randomNum ^ hash[j]) % bitArray.length;
                if (bitArray[hashedVal] == 0) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                count++;
            }
            i++;
        }

        try {
            FileWriter file = new FileWriter("bloomf_output.txt");
            BufferedWriter output = new BufferedWriter(file);
            output.write("Elements of Set A found after look-up: "+numElem);
            output.write("\nElements of Set B found after look-up: "+count);
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }      
    }

    public static void countingBloomFilter (int[] bitArray, int[] hash, int numElem, int numRemove, int numAdd) {
        int i = 0, count = 0;
        Set<Integer> setA = new HashSet<>();

        while (i < numElem) {
            int randomNum = getRandom();
            if(!setA.contains(randomNum)) {
                setA.add(randomNum);
                for (int j = 0; j < hash.length; j++) {
                    int hashedVal = (randomNum ^ hash[j]) % bitArray.length;
                    bitArray[hashedVal]++;
                }
                i++;
            }
        }

        i = 0;
        for (int elem: setA) {
            if(i == numRemove) {
                break;
            }
            for (int j = 0; j < hash.length; j++) {
                int hashedVal = (elem ^ hash[j]) % bitArray.length;
                bitArray[hashedVal]--;
            }
            i++;
        }

        i = 0;
        Set<Integer> duplicate = new HashSet<>();
        while (i < numAdd) {
            int randomNum = getRandom();
            if(!duplicate.contains(randomNum)) {
                duplicate.add(randomNum);
                for (int j = 0; j < hash.length; j++) {
                    int hashedVal = (randomNum ^ hash[j]) % bitArray.length;
                    bitArray[hashedVal]++;
                }
                i++;
            }
        }

        i = 0;
        for (int elem: setA) {
            boolean flag = false;
            for (int j = 0; j < hash.length; j++) {
                int hashedVal = (elem ^ hash[j]) % bitArray.length;
                if (bitArray[hashedVal] == 0) {
                    flag = true;
                    break;
                }
            }
            if(!flag) {
                count++;
            }
            i++;
        }

        try {
            FileWriter file = new FileWriter("countingbf_output.txt");
            BufferedWriter output = new BufferedWriter(file);
            output.write("Elements of Set A found after look-up: "+count);
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static void codedBloomFilter(int numSets, int numElemC, int numFilters, int numBitsC, int numHashesC) {
        int[][] bitArray = new int[numFilters][numBitsC];
        int[] hash = new int[numHashesC];
        int count = 0;

        List<Set<Integer>> sets = new ArrayList<>();
        sets.add(new HashSet<>()); //Setting the 0th element of the list as empty

        //Generating the elements
        for (int i = 0; i < numSets; i++) {
            Set<Integer> elements = new HashSet<>();
            while (elements.size() < numElemC) {
                int randomNum = getRandom();
                elements.add(randomNum);
            }
            sets.add(elements);
        }

        //Generating the hashes
        Set<Integer> duplicateHashes = new HashSet<>();
        int j = 0;
        while (j < numHashesC) {
            int randomNum = getRandom();
            if(!duplicateHashes.contains(randomNum)) {
                hash[j++] = randomNum;
                duplicateHashes.add(randomNum);
            }
        }

        //Encoding the elements of all the sets in the filters
        for (int i = 1; i < sets.size(); i++) {
            Set<Integer> elements = sets.get(i);
            int[] filterCode = getBinary(i, numFilters);
            for (int elem: elements) {
                for (int k = 0; k < filterCode.length; k++) {
                    if(filterCode[k] == 1) {
                        for (int h = 0; h < hash.length; h++) {
                            int hashedVal = (elem ^ hash[h]) % bitArray[k].length;
                            bitArray[k][hashedVal] = 1;
                        }
                    }
                }
            }
        }

        //Look-up elements in the filters
        for (int i = 1; i < sets.size(); i++) {
            Set<Integer> elements = sets.get(i);
            int[] code = getBinary(i, numFilters);
            for (int elem: elements) {
                int[] generatedCode = new int[numFilters];
                for (int k = 0; k < numFilters; k++) {
                    boolean flag = false;
                    for (int h = 0; h < hash.length; h++) {
                        int hashedVal = (elem ^ hash[h]) % bitArray[k].length;
                        if (bitArray[k][hashedVal] == 0) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        generatedCode[k] = 1;
                    }
                }
                if (checkIfSame(generatedCode,code)) {
                    count++;
                }
            }
        }

        try {
            FileWriter file = new FileWriter("codedbf_output.txt");
            BufferedWriter output = new BufferedWriter(file);
            output.write("Number of elements whose lookup results are correct: "+count);
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static int getRandom() {
        Random r = new Random();
        return r.nextInt(Integer.MAX_VALUE);
    }

    public static int[] getBinary(int n, int bits) {
        int[] arr = new int[bits];
        int j = 0;
        while (n >= 1) {
           arr [bits-j-1] = n % 2;
           n /= 2;
           j++;
        }
        return arr;
    }

    public static boolean checkIfSame(int[] a, int[] b) {
        if(a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}