package coat;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TestBucle {

    private final File data = new File("test/cr_combo_20150604.txt");


    private final List<String> personas = new ArrayList<>();
    private final Map<String, List<String>> listaDeHijos = new HashMap<>();
    private final Map<String, String> madres = new HashMap<>();
    private final Map<String, String> padres = new HashMap<>();
    public static final boolean LINEAS_PRINCIPALES = false;


    @Test
    public void test() {
        try (BufferedReader reader = new BufferedReader(new FileReader(data))) {
            reader.readLine();
            reader.lines().forEach(line -> {
                        String[] row = line.split("\t");
                        String id = row[1];
                        String mother = row[9];
                        String father = row[8];
                        personas.add(id);
                        if (!mother.equals(".")) madres.put(id, mother);
                        if (!father.equals(".")) padres.put(id, father);
                        List<String> hijosMadre = listaDeHijos.get(mother);
                        if (hijosMadre == null) {
                            hijosMadre = new ArrayList<>();
                            listaDeHijos.put(mother, hijosMadre);
                        }
                        hijosMadre.add(id);
                        List<String> hijosPadre = listaDeHijos.get(father);
                        if (hijosPadre == null) {
                            hijosPadre = new ArrayList<>();
                            listaDeHijos.put(father, hijosPadre);
                        }
                        hijosPadre.add(id);
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String nombre : personas) {
            // Solo lineas principales
            if (!LINEAS_PRINCIPALES || (padres.get(nombre) == null && madres.get(nombre) == null)){
                final List<List<String>> descendientes = getDescendientes(nombre, 0);
                for (List<String> list : descendientes) {
                    if (list.contains(nombre)) System.err.println(nombre + " -> " + list);
                    // else System.out.println(nombre + " -> " + list);
                }
            }
            // Solo lineas principales
            if (!LINEAS_PRINCIPALES || listaDeHijos.get(nombre) == null) {
                final List<List<String>> ascendientes = getAscendientes(nombre, 0);
                for (List<String> list : ascendientes) {
                    if (list.contains(nombre)) System.err.println(nombre + " -> " + list);
                    // else System.out.println(nombre + " <- " + list);
                }
            }
        }
    }

    private List<List<String>> getDescendientes(String nombre, int level) {
        final List<List<String>> descendientes = new ArrayList<>();
        if (level == 30) return descendientes;
        for (String hijo : listaDeHijos.getOrDefault(nombre, new ArrayList<>())) {
            final List<List<String>> nietos = getDescendientes(hijo, level + 1);
            if (nietos.isEmpty()) creaRutaSimple(descendientes, hijo);
            else creaRutaMultiple(descendientes, hijo, nietos);
        }
        return descendientes;
    }

    private void creaRutaSimple(List<List<String>> descendientes, String hijo) {
        final List<String> ruta = new ArrayList<>();
        ruta.add(hijo);
        descendientes.add(ruta);
    }

    private void creaRutaMultiple(List<List<String>> descendientes, String hijo, List<List<String>> nietos) {
        for (List<String> hijoANieto : nietos) {
            final List<String> rutaAlHijo = new ArrayList<>();
            rutaAlHijo.add(hijo);
            rutaAlHijo.addAll(hijoANieto);
            descendientes.add(rutaAlHijo);
        }
    }

    private List<List<String>> getAscendientes(String nombre, int level) {
        List<List<String>> ascendientes = new ArrayList<>();
        if (level == 30) return ascendientes;
        final String madre = madres.get(nombre);
        if (madre != null) {
            List<List<String>> abuelosMaternos = getAscendientes(madre, level + 1);
            if (abuelosMaternos.isEmpty()) creaRutaSimple(ascendientes, madre);
            else creaRutaMultiple(ascendientes, madre, abuelosMaternos);
        }
        final String padre = padres.get(nombre);
        if (madre != null) {
            List<List<String>> abuelosPaternos = getAscendientes(padre, level + 1);
            if (abuelosPaternos.isEmpty()) creaRutaSimple(ascendientes, padre);
            else creaRutaMultiple(ascendientes, padre, abuelosPaternos);
        }
        return ascendientes;
    }
}
