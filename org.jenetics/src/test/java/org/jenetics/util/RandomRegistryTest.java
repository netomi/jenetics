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
package org.jenetics.util;

import static java.util.stream.Collectors.toList;
import static org.jenetics.util.RandomRegistry.using;

import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.jenetics.internal.util.Concurrency;

import org.jenetics.DoubleChromosome;
import org.jenetics.DoubleGene;
import org.jenetics.Genotype;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 */
public class RandomRegistryTest {

	@Test
	public void setDefault() {
		RandomRegistry.reset();
		final Random devault = RandomRegistry.getRandom();
		Assert.assertNotNull(devault);

		RandomRegistry.setRandom(new Random());
		Assert.assertNotNull(RandomRegistry.getRandom());
		RandomRegistry.reset();

		Assert.assertSame(RandomRegistry.getRandom(), devault);
	}

	@Test
	public void setRandom() {
		final Random random = new Random();
		RandomRegistry.setRandom(random);

		Assert.assertSame(RandomRegistry.getRandom(), random);
	}

	@Test
	public void setThreadLocalRandom() {
		final LCG64ShiftRandom.ThreadLocal random =
			new LCG64ShiftRandom.ThreadLocal();
		RandomRegistry.setRandom(random);

		Assert.assertSame(RandomRegistry.getRandom(), random.get());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void setNullRandom() {
		RandomRegistry.setRandom((Random)null);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void setNullTLRandom() {
		RandomRegistry.setRandom((ThreadLocal<Random>)null);
	}

	@Test
	public void localContext() {
		final Random random = RandomRegistry.getRandom();

		final Random random1 = new Random();
		using(random1, r1 -> {
			final Random random2 = new Random();
			using(random2, r2 -> {
				Assert.assertSame(RandomRegistry.getRandom(), random2);
			});

			Assert.assertSame(RandomRegistry.getRandom(), random1);
		});

		Assert.assertSame(RandomRegistry.getRandom(), random);
	}

	@Test(invocationCount = 10)
	public void concurrentLocalContext() {
		try (Concurrency c = Concurrency.withCommonPool()) {
			for (int i = 0; i < 25; ++i) {
				c.execute(new ContextRunnable());
			}
		}
	}

	private static final class ContextRunnable implements Runnable {
		@Override
		public void run() {
			using(new Random(), r -> {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				Assert.assertSame(r, RandomRegistry.getRandom());

				final Random random2 = new Random();
				using(random2, r2 -> {
					Assert.assertSame(RandomRegistry.getRandom(), random2);
					Assert.assertSame(r2, random2);

					final Random random2_2 = new Random();
					RandomRegistry.setRandom(random2_2);
					Assert.assertSame(RandomRegistry.getRandom(), random2_2);

					final Random random3 = new Random();
					using(random3, r3 -> {
						Assert.assertSame(RandomRegistry.getRandom(), random3);
						Assert.assertSame(r3, random3);
					});

					Assert.assertSame(RandomRegistry.getRandom(), random2_2);
					Assert.assertNotEquals(r, RandomRegistry.getRandom());
				});

				Assert.assertSame(r, RandomRegistry.getRandom());
			});
		}
	}

	@Test
	public void withScope() {
		final List<Genotype<DoubleGene>> genotypes1 =
			RandomRegistry.with(new LCG64ShiftRandom(123), random ->
				Genotype.of(DoubleChromosome.of(0, 10)).instances()
					.limit(100)
					.collect(toList())
			);
		final List<Genotype<DoubleGene>> genotypes2 =
			RandomRegistry.with(new LCG64ShiftRandom(123), random ->
				Genotype.of(DoubleChromosome.of(0, 10)).instances()
					.limit(100)
					.collect(toList())
			);

		Assert.assertEquals(genotypes1, genotypes2);
	}

}
