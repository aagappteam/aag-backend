package aagapp_backend.services.ludo;

import org.springframework.stereotype.Service;

/**
 * This class implements a Permuted Congruential Generator (PCG) for generating random numbers.
 * It utilizes a Linear Congruential Generator (LCG) for the state update, followed by bit-shifting, XOR operations,
 * and bitwise rotation to introduce randomness and avoid predictable patterns.
 * The random numbers generated are mapped to the range of 1 to 6, simulating a dice roll.
 *
 * This implementation uses the current system time, nano time, and thread ID as the seed, providing good randomness
 * that changes with every program execution.
 */
@Service
public class PCGService {

    private long state;
    private long increment;

    private static final long MULTIPLIER = 6364136223846793005L;

    public PCGService() {
        long seed = System.currentTimeMillis() ^ System.nanoTime() ^ Thread.currentThread().getId();

        this.state = seed;
        this.increment = (seed << 1) | 1;
    }

    public int nextInt() {
        // Step 1: Update state using LCG formula (Linear Congruential Generator)
        long oldState = state;
        state = oldState * MULTIPLIER + increment;

        // Step 2: Generate the random number by permuting the state (bit shift and XOR)
        long xorshifted = ((oldState >> 18) ^ oldState) >> 27;
        int rotation = (int) (oldState >> 59);

        // Step 3: Right rotate the result to get the final random number
        int result = (int) ((xorshifted >> rotation) | (xorshifted << (-rotation & 31)));

        // Step 4: Map the result to a range of 1 to 6 (simulating dice roll)
        return Math.abs(result % 6) + 1;
    }
}

