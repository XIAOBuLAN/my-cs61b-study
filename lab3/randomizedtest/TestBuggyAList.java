package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void randomizedtest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = correct.size();
                int size2 = broken.size();
            } else if (operationNumber == 2) {
                if (correct.size() != 0) {
                    correct.getLast();
                    broken.getLast();
                }
            } else if (operationNumber == 3) {
                if (correct.size() != 0) {
                    correct.removeLast();
                    broken.removeLast();
                }
            }
        }
    }
}
