package coat.model.mist;

/**
 * ----- Añadido 5/11/2014 (Modificado el 14/11/2014) -----
 * Clase para definir un tipo de objeto llamado MistZone que se utilizará para almacenar 2 campos *
 * extraídos de un fichero MIST, estos campos son el inicio de una intersección exón-región pobre *
 * y el final de una intersección exón-región pobre, donde a la intersección entre el exón y la   *
 * región pobre la llamaremos "zona MIST".                                                        *
 * 
 * @author Jacob Henríquez
 */
class MistZone {
        // Inicio intersección exón-región pobre (inicio zona MIST):
	private int start;
        // Final intersección exón-región pobre (final zona MIST):
	private int end;

	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}

        @Override
	public String toString() {
		return "(" + this.start + ", " + this.end + ")";
	}
}
