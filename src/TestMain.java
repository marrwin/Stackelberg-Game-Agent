package comp34120.ex2;

import java.util.Arrays;
import comp34120.ex2.FollowerEstimator;
import comp34120.ex2.Record;
public class TestMain{
	public static void main(String[] args) {
		Record[] history = new Record[100];
		for (int i = 0; i < 100; i++) {
			history[i] = new Record(i+1, i, i, 1);
		}

		FollowerEstimator followerEstimator = new FollowerEstimator(0.99, history);
		System.out.println(Arrays.toString(followerEstimator.getFollowerParameters()));

		for (int i = 0; i < 100; i++) {
			followerEstimator.update(i , i+1);
		}
		System.out.println(Arrays.toString(followerEstimator.getFollowerParameters()));

	}
}
