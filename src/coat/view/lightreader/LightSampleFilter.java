/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.lightreader;

import coat.view.vcfreader.Zigosity;
import htsjdk.variant.variantcontext.VariantContext;
import javafx.event.Event;
import javafx.event.EventHandler;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by uichuimi on 14/10/16.
 */
public class LightSampleFilter {

    private EnumSet<Zigosity> zigosities = EnumSet.allOf(Zigosity.class);
    private String sample;
    private EventHandler handler;


    public LightSampleFilter(String sample) {
        this.sample = sample;
        zigosities.addAll(Arrays.asList(Zigosity.values()));
    }

    public boolean filter(VariantContext variant) {
        final Zigosity zigosity = getZigosity(variant);
        return zigosities.contains(zigosity);
    }

    private Zigosity getZigosity(VariantContext variant) {
        if (!variant.getGenotype(sample).isAvailable()) return Zigosity.NO_CALL;
        if (variant.getGenotype(sample).isHet()) return Zigosity.HET;
        if (variant.getGenotype(sample).isHomVar()) return Zigosity.HOM;
        if (variant.getGenotype(sample).isHomRef()) return Zigosity.WILD;
        return Zigosity.NO_CALL;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public void set(Zigosity zigosity) {
        zigosities.add(zigosity);
        handler.handle(new Event(Event.ANY));
    }

    public void unset(Zigosity zigosity) {
        zigosities.remove(zigosity);
        handler.handle(new Event(Event.ANY));
    }

    public boolean has(Zigosity zigosity) {
        return zigosities.contains(zigosity);
    }

    public void setOnChange(EventHandler onChange) {
        this.handler = onChange;
    }
}
