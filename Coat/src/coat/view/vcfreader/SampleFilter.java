/*
 * Copyright (c) UICHUIMI 2016
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

package coat.view.vcfreader;

import javafx.event.Event;
import javafx.event.EventHandler;
import vcf.Variant;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by uichuimi on 14/10/16.
 */
public class SampleFilter {

    private EnumSet<Zigosity> zigosities = EnumSet.allOf(Zigosity.class);
    private String sample;
    private EventHandler handler;


    public SampleFilter(String sample) {
        this.sample = sample;
        zigosities.addAll(Arrays.asList(Zigosity.values()));
    }

    public boolean filter(Variant variant) {
        final String gt = variant.getSampleInfo().getFormat(sample, "GT");
        final Zigosity zigosity = getZigosity(gt);
        return zigosities.contains(zigosity);
    }

    private Zigosity getZigosity(String gt) {
        if (gt.equals(".")) return Zigosity.VOID;
        final String[] split = gt.split("[/|]");
        final String ref = split[0];
        final String alt = split[1];
        if (ref.equals(".") && alt.equals(".")) return Zigosity.VOID;
        if (ref.equals("0") && alt.equals("0")) return Zigosity.WILD;
        if (ref.equals(alt)) return Zigosity.HOM;
        return Zigosity.HET;
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
