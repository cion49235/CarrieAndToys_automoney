package carrie.toy.friends.automoney.util;

public interface SliderStrategy {
    /**
     * Converts from zeroed double with a minimum offset to the nearest rounded slider
     * equivalent integer
     * */
    int progressOf(final double value);

    /**
     * Converts from slider integer value to an equivalent double value with a given
     * minimum offset
     * */
    double valueOf(final int progress);

    // TODO: also implement linear strategy when needed

    final class Quadratic implements SliderStrategy {
        private final double leftGap;
        private final double rightGap;
        private final double center;

        private final int centerProgress;

        /**
         * Quadratic slider strategy that scales the value of a slider given how far the slider
         * progress is from the center of the slider. The further away from the center,
         * the faster the interpreted value changes, and vice versa.
         *
         * @param minimum the minimum value of the interpreted value of the slider.
         * @param maximum the maximum value of the interpreted value of the slider.
         * @param center center of the interpreted value between the minimum and maximum, which
         *               will be used as the center value on the slider progress. Doesn't need
         *               to be the average of the minimum and maximum values, but must be in
         *               between the two.
         * @param maxProgress the maximum possible progress of the slider, this is the
         *                    value that is shown for the UI and controls the granularity of
         *                    the slider. Should be as large as possible to avoid floating
         *                    point round-off error. Using odd number is recommended.
         * */
        public Quadratic(double minimum, double maximum, double center, int maxProgress) {
            if (center < minimum || center > maximum) {
                throw new IllegalArgumentException("Center must be in between minimum and maximum");
            }

            this.leftGap = minimum - center;
            this.rightGap = maximum - center;
            this.center = center;

            this.centerProgress = maxProgress / 2;
        }

        @Override
        public int progressOf(double value) {
            final double difference = value - center;
            final double root = difference >= 0 ?
                    Math.sqrt(difference / rightGap) :
                    -Math.sqrt(Math.abs(difference / leftGap));
            final double offset = Math.round(root * centerProgress);

            return (int) (centerProgress + offset);
        }

        @Override
        public double valueOf(int progress) {
            final int offset = progress - centerProgress;
            final double square = Math.pow(((double) offset) / ((double) centerProgress), 2);
            final double difference = square * (offset >= 0 ? rightGap : leftGap);

            return difference + center;
        }
    }
}
