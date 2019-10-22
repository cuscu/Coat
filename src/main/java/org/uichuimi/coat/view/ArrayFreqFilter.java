package org.uichuimi.coat.view;

import htsjdk.variant.variantcontext.VariantContext;
import org.uichuimi.coat.view.lightreader.LightVcfFilter;

import java.util.ArrayList;
import java.util.List;

public class ArrayFreqFilter extends LightVcfFilter {

	private final String key;
	private final Connector connector;
	private final double value;

	public ArrayFreqFilter(String column, String key, Connector connector, Object value) {
		super(column, key, connector, value);
		this.key = key;
		this.connector = connector;
		this.value = (double) value;
	}

	public ArrayFreqFilter(String key, Connector connector, double value) {
		super("INFO", key, connector, value);
		this.key = key;
		this.connector = connector;
		this.value = value;
	}

	@Override
	public boolean filter(VariantContext variant) {
		final Object attribute = variant.getAttribute(getKey(), null);
		if (attribute == null) return true;
		final List<Double> values = new ArrayList<>();
		if (attribute instanceof Iterable) {
			final Iterable vls = (Iterable) attribute;
			for (Object element : vls) addValues((String) element, values);
		} else addValues((String) attribute, values);
		switch (connector) {
			case MORE_THAN:
				return values.stream().mapToDouble(value1 -> value1).min().orElse(-1) > value;
			case EQUALS:
				return values.stream().allMatch(aDouble -> aDouble == value);
			case CONTAINS:
				return false;
			case TRUE:
				return !values.isEmpty();
			case FALSE:
				return values.isEmpty();
			case IS_NOT:
				return values.stream().noneMatch(aDouble -> aDouble != value);
			case LESS_THAN:
				return values.stream().mapToDouble(value -> value).max().orElse(1) < value;
			default:
				return true;
		}
	}

	private void addValues(String attribute, List<Double> values) {
		final String[] stringValues = attribute.split("\\|");
		for (String stringValue : stringValues)
			if (!stringValue.equals("."))
				values.add(Double.parseDouble(stringValue));
	}
}
