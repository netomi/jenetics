/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.jenetics.internal.math.arithmetic.pow;
import static org.jenetics.internal.math.base.ulpDistance;
import static org.jenetics.internal.util.IndexSorter.sort;
import static org.jenetics.internal.util.array.revert;
import static org.jenetics.internal.util.array.swap;

import java.util.Random;

import org.jenetics.internal.math.statistics;

import org.jenetics.util.RandomRegistry;

/**
 * Probability selectors are a variation of fitness proportional selectors and
 * selects individuals from a given population based on it's selection
 * probability <i>P(i)</i>.
 * <p>
 * <img src="doc-files/FitnessProportionalSelection.svg" width="400" alt="Selection">
 * <p>
 * Fitness proportional selection works as shown in the figure above. The
 * runtime complexity of the implemented probability selectors is
 * <i>O(n+</i>log<i>(n))</i> instead of <i>O(n<sup>2</sup>)</i> as for the naive
 * approach: <i>A binary (index) search is performed on the summed probability
 * array.</i>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 2.0 &mdash; <em>$Date: 2014-08-26 $</em>
 */
public abstract class ProbabilitySelector<
	G extends Gene<?, G>,
	C extends Comparable<? super C>
>
	implements Selector<G, C>
{
	private static final int SERIAL_INDEX_THRESHOLD = 35;

	private static final long MAX_ULP_DISTANCE = pow(10, 10);

	protected ProbabilitySelector() {
	}

	@Override
	public Population<G, C> select(
		final Population<G, C> population,
		final int count,
		final Optimize opt
	) {
		requireNonNull(population, "Population");
		requireNonNull(opt, "Optimization");
		if (count < 0) {
			throw new IllegalArgumentException(format(
				"Selection count must be greater or equal then zero, but was %s.",
				count
			));
		}

		final Population<G, C> selection = new Population<>(count);

		if (count > 0) {
			final double[] prob = probabilities(population, count, opt);
			assert (population.size() == prob.length) :
				"Population size and probability length are not equal.";
			assert (sum2one(prob)) : "Probabilities doesn't sum to one.";

			incremental(prob);

			final Random random = RandomRegistry.getRandom();
			selection.fill(
				() -> population.get(indexOf(prob, random.nextDouble())),
				count
			);
		}

		return selection;
	}

	/**
	 * This method takes the probabilities from the
	 * {@link #probabilities(Population, int)} method and inverts it if needed.
	 *
	 * @param population The population.
	 * @param count The number of phenotypes to select.
	 * @param opt Determines whether the individuals with higher fitness values
	 *        or lower fitness values must be selected. This parameter
	 *        determines whether the GA maximizes or minimizes the fitness
	 *        function.
	 * @return Probability array.
	 */
	protected final double[] probabilities(
		final Population<G, C> population,
		final int count,
		final Optimize opt
	) {
		return requireNonNull(opt) == Optimize.MINIMUM ?
			sortAndRevert(probabilities(population, count)) :
			probabilities(population, count);
	}

	// Package private for testing.
	static double[] sortAndRevert(final double[] array) {
		final int[] indexes = sort(array);

		final double[] result = new double[array.length];
		for (int i = 0; i < result.length; ++i) {
			result[indexes[result.length - 1 - i]] = array[indexes[i]];
		}

		return result;
	}

	/**
	 * <p>
	 * Return an Probability array, which corresponds to the given Population.
	 * The probability array and the population must have the same size. The
	 * population is not sorted. If a subclass needs a sorted population, the
	 * subclass is responsible to sort the population.
	 * </p>
	 * The implementer always assumes that higher fitness values are better. The
	 * base class inverts the probabilities ({@code p = 1.0 - p }) if the GA is
	 * supposed to minimize the fitness function.
	 *
	 * @param population The <em>unsorted</em> population.
	 * @param count The number of phenotypes to select. <i>This parameter is not
	 *        needed for most implementations.</i>
	 * @return Probability array. The returned probability array must have the
	 *         length {@code population.size()} and <strong>must</strong> sum to
	 *         one. The returned value is checked with
	 *         {@code assert(Math.abs(math.sum(probabilities) - 1.0) < 0.0001)}
	 *         in the base class.
	 */
	protected abstract double[] probabilities(
		final Population<G, C> population,
		final int count
	);

	/**
	 * Check if the given probabilities sum to one.
	 *
	 * @param probabilities the probabilities to check.
	 * @return {@code true} if the sum of the probabilities are within the error
	 *         range, {@code false} otherwise.
	 */
	static boolean sum2one(final double[] probabilities) {
		final double sum = statistics.sum(probabilities);
		return abs(ulpDistance(sum, 1.0)) < MAX_ULP_DISTANCE;
	}

	static int indexOf(final double[] incr, final double v) {
		return incr.length <= SERIAL_INDEX_THRESHOLD ?
			indexOfSerial(incr, v) :
			indexOfBinary(incr, v);
	}

	/**
	 * Perform a binary-search on the summed probability array.
	 */
	static int indexOfBinary(final double[] incr, final double v) {
		int imin = 0;
		int imax = incr.length;
		int index = -1;

		while (imax > imin && index == -1) {
			final int imid = (imin + imax) >>> 1;

			if (imid == 0 || (incr[imid] >= v && incr[imid - 1] < v)) {
				index = imid;
			} else if (incr[imid] <= v) {
				imin = imid + 1;
			} else if (incr[imid] > v) {
				imax = imid;
			}
		}

		return index;
	}

	/**
	 * Perform a serial-search on the summed probability array.
	 */
	static int indexOfSerial(final double[] incr, final double v) {
		int index = -1;
		for (int i = 0; i < incr.length && index == -1; ++i) {
			if (incr[i] >= v) {
				index = i;
			}
		}

		return index;
	}

	/**
	 * In-place summation of the probability array.
	 */
	static double[] incremental(final double[] values) {
		for (int i = 1; i < values.length; ++i) {
			values[i] = values[i - 1] + values[i];
		}
		return values;
	}

}
