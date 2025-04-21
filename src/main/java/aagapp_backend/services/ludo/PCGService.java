package aagapp_backend.services.ludo;
import org.springframework.stereotype.Service;

/**
 * Author: Anil Kant Mishra
 *
 * This class implements a Permuted Congruential Generator (PCG) for generating random numbers.
 * It combines a Linear Congruential Generator (LCG) for state updates and applies bitwise operations
 * (such as XOR and rotation) to produce random numbers that are both unpredictable and non-repeating.
 * The generated numbers are mapped to the range of 1 to 6, simulating a dice roll.
 *
 * The RNG is initialized with a seed based on the current system time, nano-time, and thread ID,
 * providing randomness that changes with each program execution. Additionally, dynamic re-seeding
 * is implemented to prevent patterns and ensure continued randomness over long periods of usage.
 *
 * This RNG is designed for use in high-scale applications such as games (e.g., Ludo, Snake and Ladder),
 * where fairness and unpredictability are crucial.
 */

@Service
public class PCGService {
    private long state;
    private long increment;

    private static final long MULTIPLIER = 6364136223846793005L;

    // Re-seed threshold (after how many calls to the RNG should we re-seed)
    private static final int RESEED_THRESHOLD = 100000; // Re-seed after 100,000 random number generations
    private int randomNumberCount = 0; // Counter to track how many random numbers have been generated

    /**
     * Initializes the RNG with a dynamic seed based on system time, nano-time, and thread ID.
     * This ensures that each execution of the program produces a unique seed, preventing predictable patterns.
     */
    public PCGService() {
        long seed = System.currentTimeMillis() ^ System.nanoTime() ^ Thread.currentThread().getId();

        this.state = seed;
        this.increment = (seed << 1) | 1;
    }

    /**
     * Generates a random number between 1 and 6 using a Permuted Congruential Generator (PCG) approach.
     * This method ensures fairness and unpredictability by updating the state using an LCG formula
     * and applying bitwise operations (XOR and rotations) to scramble the result.
     *
     * @return a random number between 1 and 6, simulating a dice roll.
     */
    public int generateRandomNumber() {
        // Step 1: Check if we need to re-seed based on the threshold
        if (++randomNumberCount >= RESEED_THRESHOLD) {
            reSeed();
        }

        // Step 2: Update state using LCG formula (Linear Congruential Generator)
        long oldState = state;
        state = oldState * MULTIPLIER + increment;

        // Step 3: Generate the random number by permuting the state (bit shift and XOR)
        long xorshifted = ((oldState >> 18) ^ oldState) >> 27;
        int rotation = (int) (oldState >> 59);

        // Step 4: Right rotate the result to get the final random number
        int result = (int) ((xorshifted >> rotation) | (xorshifted << (-rotation & 31)));

        // Step 5: Map the result to a range of 1 to 6 (simulating dice roll)
        return Math.abs(result % 6) + 1;
    }

    /**
     * Re-seeds the RNG to maintain unpredictability and prevent patterns.
     * This method generates a new seed based on the current system time, nano-time, and thread ID,
     * and resets the state and increment values to ensure continued randomness.
     */
    private void reSeed() {
        // Generate a new seed from the current system time, nano-time, and thread ID
        long newSeed = System.currentTimeMillis() ^ System.nanoTime() ^ Thread.currentThread().getId();

        // Re-initialize state and increment using the new seed
        this.state = newSeed;
        this.increment = (newSeed << 1) | 1;

        // Reset the random number count after re-seeding
        randomNumberCount = 0;
    }
}
