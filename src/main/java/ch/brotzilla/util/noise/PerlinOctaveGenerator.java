package ch.brotzilla.util.noise;

import java.util.Random;

import ch.brotzilla.util.MersenneTwister;

/**
 * Creates perlin noise through unbiased octaves
 */
public class PerlinOctaveGenerator extends OctaveGenerator {

    /**
     * Creates a perlin octave generator for the given world
     *
     * @param seed Seed to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public PerlinOctaveGenerator(long seed, int octaves) {
        this(new MersenneTwister(seed), octaves);
    }

    /**
     * Creates a perlin octave generator for the given {@link Random}
     *
     * @param rand Random object to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public PerlinOctaveGenerator(Random rand, int octaves) {
        super(createOctaves(rand, octaves));
    }

    /**
     * Creates a perlin octave generator for the given {@link MersenneTwister}
     *
     * @param rand MersenneTwister object to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public PerlinOctaveGenerator(MersenneTwister rand, int octaves) {
        super(createOctaves(rand, octaves));
    }

    private static NoiseGenerator[] createOctaves(Random rand, int octaves) {
        NoiseGenerator[] result = new NoiseGenerator[octaves];

        for (int i = 0; i < octaves; i++) {
            result[i] = new PerlinNoiseGenerator(rand);
        }

        return result;
    }

    private static NoiseGenerator[] createOctaves(MersenneTwister rand, int octaves) {
        NoiseGenerator[] result = new NoiseGenerator[octaves];

        for (int i = 0; i < octaves; i++) {
            result[i] = new PerlinNoiseGenerator(rand);
        }

        return result;
    }
}
