import java.io.*;
import java.util.*;

public class Vigenere {

	final static String freqFileName = "freqs.txt";
	final static int ALPHABETSIZE = 26;
	final static int KEYLENGTHUPPERBOUND = 10;  // Unlikely that key is higher

	public static void main(String args[]) {
		System.out.println("Hi there!");

		String ciphertextExample = "";
		String plaintextExample = "";

		try {
			BufferedReader cipherReader = new BufferedReader(new FileReader("ciphertext.txt"));
			ciphertextExample = cipherReader.readLine(); // Should just be one line of input
			
			BufferedReader plainReader = new BufferedReader(new FileReader("plaintext.txt"));
			plaintextExample = plainReader.readLine(); // Should just be one line of input

			cipherReader.close();
			plainReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} 

		System.out.println("Problem 1");
		crackVigenere(ciphertextExample);
		System.out.println("-----------");

		System.out.println("Problem 2a");
		System.out.println("General variance: "+calcVariance(getExpectedFreqs()));
		System.out.println("-----------");
		
		System.out.println("Problem 2b");
		System.out.println("Plaintext variance: "+calcVariance(getFrequencies(plaintextExample)));
		System.out.println("-----------");

		String keys[] = {"yz", "xyz", "wxyz", "vwxyz", "uvwxyz"};  // Used in 2c and 2d
		System.out.println("Problem 2c");
		for (String key : keys) {
			String ciphertext = encryptVigenere(plaintextExample, key);
			System.out.println("Variance for "+key+": "+calcVariance(getFrequencies(ciphertext)));		
		}
		System.out.println("-----------");

		System.out.println("Problem 2d");
		for (String key : keys) {
			double sum = 0;
			int keyLength = key.length();
			String ciphertext = encryptVigenere(plaintextExample, key);
			for (int i = 1; i <= keyLength; i++) {
				String cipher = getCipher(ciphertext, i, keyLength);
				sum += calcVariance(getFrequencies(cipher));
			}
			double mean = sum / keyLength;

			System.out.println("Mean variance for "+key+": "+mean);
		}
		System.out.println("-----------");

		System.out.println("Problem 2e");
		String ciphertext = encryptVigenere(plaintextExample, "uvwxyz");
		for (int j = 2; j <= 5; j++) {
			double sum = 0;
			for (int i = 1; i <= j; i++) {
				String cipher = getCipher(ciphertext, i, j);
				sum += calcVariance(getFrequencies(cipher));
			}
			double mean = sum / j;

			System.out.println("Mean assuming length "+j+": "+mean);	
		}
	}

	////////////////////////////
	// Problem 1

	// Shifts characters by specified amount
	public static String rot(String plaintext, int shift) {
		String encryptedString = "";

		boolean cap = Character.isUpperCase(plaintext.charAt(0));

		for (int i = 0; i < plaintext.length(); i++) {
			encryptedString += indexToChar(((charToIndex(plaintext.charAt(i))+shift)%ALPHABETSIZE), cap);
		}
		return encryptedString;
	}

	// Gets the chi-squared with expectedFreqs against calculated actualFreqs
	public static double getChiSquared(String input, double expectedFreqs[]) {
		double actualFreqs[] = getFrequencies(input);

		double output = 0;

		for (int i = 0; i < ALPHABETSIZE; i++) {
			output += Math.pow(actualFreqs[i]-expectedFreqs[i],2)/expectedFreqs[i];
		}

		return output;
	}

	// Returns the frequencies of characters in a text
	public static double[] getFrequencies(String input) {
		int totalChars = 0;
		int numOccurs[] = new int[ALPHABETSIZE]; // How many times each character appears

		for (char letter: input.toCharArray()) {
			numOccurs[charToIndex(letter)] += 1;
			totalChars += 1;
		}

		double actualFreqs[] = new double[ALPHABETSIZE];  // Indexed the same way as expectedFreqs
		
		for (int i = 0; i < ALPHABETSIZE; i++) {
			actualFreqs[i] = ((double)(numOccurs[i]))/totalChars;
		}

		return actualFreqs;
	}

	// Reads in and processes the frequency file
	public static double[] getExpectedFreqs() {
		double expectedFreqs[] = new double[ALPHABETSIZE];  // 0 index is a/A, 1 index is b/B, etc...

		try {
			BufferedReader reader = new BufferedReader(new FileReader(freqFileName));
		
			String line = null;

			int expectedFreqCounter = 0;

			while ((line = reader.readLine()) != null) {
				if (expectedFreqCounter >= ALPHABETSIZE) { // Too many 
					break;
				}
				String result[] = line.split("\\s");
				expectedFreqs[expectedFreqCounter] = Double.parseDouble(result[1]);		
				expectedFreqCounter += 1;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return expectedFreqs;
	}

	// This takes in a Caesar-ciphered text and finds the shift amount
	public static int findShiftAmount(String encryptedString) {
		double lowestChiSquared = -1;
		int shift = 0;

		double expectedFreqs[] = getExpectedFreqs();

		for (int i = 0; i < 26; i++) {
			String maybeDecrypt = rot(encryptedString, i);
			double chiSquared = getChiSquared(maybeDecrypt, expectedFreqs);
			if (chiSquared < lowestChiSquared || lowestChiSquared < 0) {
				lowestChiSquared = chiSquared;
				shift = i;
			}
		}	

		return shift;	
	}

	// Encrypts with Vigenere a plaintext with the key
	public static String encryptVigenere(String plaintext, String key) {  // Note this is the same as rot where the key is just one letter
		String encryptedString = "";
		boolean cap = Character.isUpperCase(plaintext.charAt(0));

		for (int i = 0; i < plaintext.length(); i++) {
			int shift = charToIndex(key.charAt(i % key.length()));
			encryptedString += indexToChar(((charToIndex(plaintext.charAt(i))+shift)%ALPHABETSIZE), cap);
		}

		return encryptedString;
	}

	// Decrypts a Vigenere cipher given a key
	public static String decryptVigenere(String encryptedString, String key) {  // Note this is the same as rot where the key is just one letter
		String plaintext = "";
		boolean cap = Character.isUpperCase(encryptedString.charAt(0));

		for (int i = 0; i < encryptedString.length(); i++) {
			int shift = charToIndex(key.charAt(i % key.length()));
			plaintext += indexToChar(((charToIndex(encryptedString.charAt(i))-shift+26)%ALPHABETSIZE), cap);  // +26 to deal with negative remainders... ^^^
		}

		return plaintext;
	}

	// Decrypts a Vigenere cipher without a key
	public static void crackVigenere(String encryptedString) {
		boolean cap = Character.isUpperCase(encryptedString.charAt(0));

		int keyLength = findVigenereKeyLength(encryptedString, KEYLENGTHUPPERBOUND);  // The guessed key length
		String key = "";

		for (int i = 1; i <= keyLength; i++) {
			String cipher = getCipher(encryptedString, i, keyLength);

			int shiftAmount = findShiftAmount(cipher);
			int keyShift = shiftAmount == 0 ? 0 : 26-shiftAmount;

			key += indexToChar(keyShift, cap);
		}

		System.out.println("The key: "+key);
		// System.out.println("The plaintext: "+decryptVigenere(encryptedString, key));
	}

	// Extracts the corresponding Caesar cipher at multiples of position
	public static String getCipher(String encryptedString, int position, int keyLength)  { 
		String output = "";
		for (int i = position-1; i < encryptedString.length(); i += keyLength) {
			output += encryptedString.charAt(i);
		}

		return output;
	}

	// Gets the length of the Vigenere key by finding the highest index of coincidence for shifted text
	public static int findVigenereKeyLength(String encryptedString, int keyLengthUpperBound) {
		double highestIndex = -1;  // The highest index of coincidence found so far, and the corresponding shift
		int shiftForHighestIndex = -1;

		for (int i = 1; i < keyLengthUpperBound; i++) {
			String shiftedString = shiftString(encryptedString, i);

			double indexOfCoincidence = calcIndexOfCoincidence(encryptedString, shiftedString);

			if (indexOfCoincidence > highestIndex || highestIndex < 0) {
				highestIndex = indexOfCoincidence;
				shiftForHighestIndex = i;
			}

		}

		int keyLength = shiftForHighestIndex;
		return keyLength;
	}

	// Calculates how many times the lined up characters of two texts line up, and then feeds it into the index of coincidence formula
	public static double calcIndexOfCoincidence(String text1, String text2) {
		int numOccurs[] = new int[26];  // The number of times the shiftedString and the encryptedString match up for each letter

		for (int i = 0; i < text1.length(); i++) {
			if (text1.charAt(i) == text2.charAt(i)) {
				numOccurs[charToIndex(text1.charAt(i))] += 1;
			}
		}

		double summation = 0;

		for (int i = 0; i < ALPHABETSIZE; i++) {
			summation += numOccurs[i]*(numOccurs[i]-1);
		}
		
		int textLength = text1.length(); //Same as text2.length()
		double indexOfCoincidence = ALPHABETSIZE*summation/(textLength*(textLength-1));

		return indexOfCoincidence;
	}

	// Completely different from rot, the ith letter goes to the (i+shift)th letter
	public static String shiftString(String input, int shift) {
		int inputLength = input.length();
		char output[] = new char[inputLength];

		for (int i = 0; i < inputLength; i++) {
			output[(i+shift)%inputLength] = input.charAt(i);
		}

		return new String(output);
	}

	// Converts a letter to a range 0-25
	public static int charToIndex(char letter) {
		char anchor = Character.isUpperCase(letter) ? 'A' : 'a';  // Figures out whether the string should be treated as upper or lower
		return letter-anchor;
	}

	// Takes in a number 0-25 and returns letter.  Note we have to specifiy whether we want upper or lower.
	public static char indexToChar(int index, boolean cap) {
		char anchor = cap ? 'A' : 'a';  // Figures out whether the string should be treated as upper or lower
		return (char)(index+anchor);
	}

	// End of code for Problem 1
	////////////////////
	// Problem 2

	// Returns the population variance from a given population
	public static double calcVariance(double population[]) {
		double mean = calcMean(population);
		double summation = 0;
		
		for (double x : population) {
			summation += Math.pow(x-mean,2);
		} 
		return summation / population.length;
	}

	// I'm sure there's a function for this, too lazy
	public static double calcMean(double population[]) {
		double total = 0;
		for (double x : population) {
			total += x;
		}
		return total / population.length;
	}
}