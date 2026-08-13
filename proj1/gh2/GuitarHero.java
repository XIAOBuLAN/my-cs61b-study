package gh2;

import deque.ArrayDeque;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static void main(String[] args) {
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        ArrayDeque<GuitarString> stringSounds = new ArrayDeque<>();
        for (int i = 0; i < keyboard.length(); i += 1) {
            double Frequency = 440 * Math.pow(2, (i - 24) / 12.0);
            GuitarString string = new GuitarString(Frequency);
            stringSounds.addLast(string);
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int index = keyboard.indexOf(key);
                if (index >= 0 && index <37){
                    stringSounds.get(index).pluck();
                }
            }

            /* compute the superposition of samples */
            double sample = 0.0;
            for (int i = 0; i < stringSounds.size(); i += 1) {
                sample += stringSounds.get(i).sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < stringSounds.size(); i += 1) {
                stringSounds.get(i).tic();
            }
        }
    }
}

