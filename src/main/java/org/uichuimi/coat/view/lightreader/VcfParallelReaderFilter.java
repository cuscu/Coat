/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.uichuimi.coat.view.lightreader;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by uichuimi on 5/05/17.
 */
abstract class VcfParallelReaderFilter implements Closeable, Iterable<VariantContext>, Iterator<VariantContext> {
    private final AtomicLong stamp = new AtomicLong();
    private final AtomicInteger toTake = new AtomicInteger();
    private final Queue<VariantContext> queue = new ArrayDeque<>();
    private final List<Thread> threads = new LinkedList<>();
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong passed = new AtomicLong();
    private VCFFileReader reader;

    public VcfParallelReaderFilter(File file) {
        reader = new VCFFileReader(file, false);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() - 1; i++)
            threads.add(new VariantFilter(this));
        for (Thread thread : threads) thread.start();
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
            for (Thread thread : threads) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getTotal() {
        return total.get();
    }

    public long getPassed() {
        return passed.get();
    }

    @Override
    public boolean hasNext() {
        return reader.iterator().hasNext();
    }

    @Override
    public VariantContext next() {
        total.incrementAndGet();
        while (queue.isEmpty()) {
        }
        return queue.poll();
    }

    @NotNull
    @Override
    public Iterator<VariantContext> iterator() {
        return this;
    }

    private void put(long stamp, VariantContext element) {
        passed.incrementAndGet();
        while (stamp > toTake.get()) {
        }
        synchronized (toTake) {
            queue.add(element);
            toTake.incrementAndGet();
        }
    }

    private synchronized Tupla<VariantContext> take() {
        if (reader.iterator().hasNext()) {
            final VariantContext next = reader.iterator().next();
            return new Tupla<>(stamp.getAndIncrement(), next);
        } return null;
    }

    abstract boolean filter(VariantContext variantContext);

    private class VariantFilter extends Thread {
        private VcfParallelReaderFilter readerFilter;

        public VariantFilter(VcfParallelReaderFilter readerFilter) {
            this.readerFilter = readerFilter;
        }

        @Override
        public void run() {
            Tupla<VariantContext> tupla;
            while ((tupla = readerFilter.take()) != null) {
                final boolean filter = filter(tupla.element);
                if (filter) readerFilter.put(tupla.stamp, tupla.element);
            }
        }

    }

    class Tupla<T> {
        long stamp;
        T element;

        Tupla(long stamp, T element) {
            this.stamp = stamp;
            this.element = element;
        }
    }
}
