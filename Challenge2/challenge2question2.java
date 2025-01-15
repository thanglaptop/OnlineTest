package challenge2;

public class challenge2question2 {

	public static void main(String[] args) {
		int array[] = {3,7,1,2,6,4};
		System.out.println("Missing number is: " + findMissingNumber(array));
	}

	public static int findMissingNumber(int[] array) {
		int result = 0;
		int n = array.length;
		int expectedSum = (n + 1) * (n + 2) / 2;
		int actualSum = 0;
		for(int i : array) {
			actualSum += i;
		}
		result = expectedSum - actualSum;
		return result;
	}
}
