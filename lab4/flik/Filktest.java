package flik;

import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;

import static org.junit.Assert.assertEquals;

public class Filktest {
    @Test
    public void test() {
            boolean broken = Flik.isSameNumber(128, 128);
            boolean expected = true;
            assertEquals(expected, broken);
    }

}
