package coat.view.vcfcombiner;

import java.io.File;

public class Sample {
	private final File file;
	private final String name;
	private final long size;
	private File mistFile;
	private Genotype genotype;

	public Sample(File file, String name, long size) {
		this.file = file;
		this.name = name;
		this.size = size;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public Genotype getGenotype() {
		return genotype;
	}

	public File getMistFile() {
		return mistFile;
	}

	public void setMistFile(File mistFile) {
		this.mistFile = mistFile;
	}

	public void setGenotype(Genotype genotype) {
		this.genotype = genotype;
	}
}
