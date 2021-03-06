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
package org.jenetics.internal.collection;

import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.jenetics.internal.math.random;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 */
public class ArrayProxyListTest {

	@Test
	public void size() {
		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		final List<Integer> list = new ArrayProxyList<>(proxy);

		Assert.assertEquals(list.size(), proxy.length);
	}

	@Test
	public void get() {
		final long seed = random.seed();
		final Random random = new Random(seed);

		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextInt());
		}

		final List<Integer> list = new ArrayProxyList<>(proxy);

		for (int i = 0; i < proxy.length; ++i) {
			final Integer actual = list.get(i);
			final Integer expected = proxy.get(i);
			Assert.assertEquals(actual, expected);
		}
	}

	@Test
	public void sliceGet() {
		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, i);
		}

		final int sliceStart = 50;
		final int sliceEnd = 500;
		final ArrayProxy<Integer, ?, ?> slice = proxy.slice(sliceStart, sliceEnd);
		final List<Integer> list = new ArrayProxyList<>(slice);

		for (int i = 0; i < list.size(); ++i) {
			Assert.assertEquals(slice.get(i), Integer.valueOf(i + sliceStart));
		}
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void set() {
		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		final List<Integer> list = new ArrayProxyList<>(proxy);

		list.set(34, 23);
	}

	@Test
	public void indexOf() {
		long seed = 12341234;
		final Random random = new Random(seed);

		final ArrayProxy<Long, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextLong());
		}

		final List<Long> list = new ArrayProxyList<>(proxy);

		random.setSeed(seed);
		for (int i = 0; i < proxy.length; ++i) {
			final int index = list.indexOf(random.nextLong());
			Assert.assertEquals(index, i);
		}
	}

	@Test
	public void lastIndexOf() {
		long seed = 12341234;
		final Random random = new Random(seed);

		final ArrayProxy<Long, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextLong());
		}

		final List<Long> list = new ArrayProxyList<>(proxy);

		random.setSeed(seed);
		for (int i = 0; i < proxy.length; ++i) {
			final int index = list.lastIndexOf(random.nextLong());
			Assert.assertEquals(index, i);
		}
	}

	@Test
	public void sliceIndexOf() {
		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, i);
		}

		final int sliceStart = 50;
		final int sliceEnd = 500;
		final ArrayProxy<Integer, ?, ?> slice = proxy.slice(sliceStart, sliceEnd);
		final List<Integer> list = new ArrayProxyList<>(slice);

		for (int i = 0; i < list.size(); ++i) {
			final int index = list.indexOf(sliceStart + i);
			Assert.assertEquals(index, i);
		}
	}

	@Test
	public void sliceLastIndexOf() {
		final ArrayProxy<Integer, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, i);
		}

		final int sliceStart = 50;
		final int sliceEnd = 500;
		final ArrayProxy<Integer, ?, ?> slice = proxy.slice(sliceStart, sliceEnd);
		final List<Integer> list = new ArrayProxyList<>(slice);

		for (int i = 0; i < list.size(); ++i) {
			final int index = list.lastIndexOf(sliceStart + i);
			Assert.assertEquals(index, i);
		}
	}

	@Test
	public void contains() {
		long seed = random.seed();
		final Random random = new Random(seed);

		final ArrayProxy<Long, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextLong());
		}

		final List<Long> list = new ArrayProxyList<>(proxy);

		random.setSeed(seed);
		for (int i = 0; i < proxy.length; ++i) {
			Assert.assertTrue(
				list.contains(random.nextLong()),
				"Must contain the given value."
			);
		}
	}

	@Test
	public void toArray() {
		long seed = random.seed();
		final Random random = new Random(seed);

		final ArrayProxy<Long, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextLong());
		}

		final List<Long> list = new ArrayProxyList<>(proxy);
		final Object[] array = list.toArray();

		for (int i = 0; i < array.length; ++i) {
			Assert.assertEquals(array[i], proxy.get(i));
		}
	}

	@Test
	public void toArrayLong() {
		long seed = random.seed();
		final Random random = new Random(seed);

		final ArrayProxy<Long, ?, ?> proxy = new ObjectArrayProxy<>(1000);
		for (int i = 0; i < proxy.length; ++i) {
			proxy.set(i, random.nextLong());
		}

		final List<Long> list = new ArrayProxyList<>(proxy);
		final Long[] array = list.toArray(new Long[0]);

		for (int i = 0; i < array.length; ++i) {
			Assert.assertEquals(array[i], proxy.get(i));
		}
	}

}
