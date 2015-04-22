package coat.combinevcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Clase en la que se encuentran todas las funciones necesarias para hacer que dados dos grupos de *
 * ficheros de entrada en formato .vcf (include y exclude) nos devuelva en un fichero de salida, *
 * también en formato .vcf, las líneas del fichero de referencia (primer fichero del grupo include)
 * * siempre y cuando esas líneas pertenezcan a grupos de líneas obtenidas de los diferentes
 * ficheros * include para las que existan coincidencias respecto a los campos cromosoma (CHROM) y
 * posición * (POS), y que además sean posiciones que no estén presentes en ninguno de los ficheros
 * del grupo * exclude; Por otro, se comprueba si al menos una línea incluida en ese grupo de líneas
 * coincidentes * está en una zona MIST, y en caso afirmativo se añade la etiqueta "MistZone" al
 * campo INFO de la * línea de referencia que se escribirá en el fichero de salida; Además, en el
 * fichero de salida se * escribirán los subcampos AF y DP del campo INFO, descartando el resto de
 * subcampos; Los campos * FORMAT y SM también se descartan en el fichero de salida. *
 *
 * @author Jacob Henríquez
 */
public class CombinatorVcf {

    //----- Añadido el 20/10/2014:
    // Vector de Buffered Reader para parametrizar el número de ficheros de entrada (ficheros
    // include y ficheros exclude):
    private static BufferedReader[] vcf_buffreader;

    // Vector de string donde se almacenarán las líneas de cada fichero (include y exclude) que
    // utilizaremos para hacer las comparaciones:
    private static String[] vcf_lines;

    //----- Añadido el 21/10/2014:
    // Vector de string que representa los cromosomas y que nos servirá para buscar y comparar
    // posiciones cuando se recorran los ficheros:
    public static final String[] chromosomes = {
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "X", "Y", "MT"};

    //----- Añadido el 22/10/2014:
    // Vector para guardar las líneas coincidentes respecto a cromosoma (CHROM) y posición (POS)
    // de todos los ficheros de entrada (include y exclude):
    private static String[] matches;

    // Variable para llevar un recuento de las coincidencias y no coincidencias encontradas cuando
    // se lleven a cabo las comparaciones:
    private int count = 0;

    public CombinatorVcf() {
    }

    /**
     * ----- Añadido el 16/10/2014 (Modificado el 28/11/2014) ----- Función que, dados dos grupos de
     * ficheros de entrada (include y exclude), volcará en un fichero de salida todas aquellas
     * líneas del fichero de referencia (primer fichero del grupo include) que pertenezcan a un
     * grupo de líneas obtenidas de los ficheros include en los que coincidan los campos cromosoma
     * (CHROM) y posición (POS), y que a su vez no se encuentren coincidencias de cromosoma y
     * posición en ninguno de los ficheros del grupo exclude; Para las líneas de los ficheros
     * include que se escriban en el fichero de salida, añadirá una etiqueta adicional llamada
     * "MistZone" (cuando corresponda) en el campo INFO (Campo 8 en los ficheros .vcf) para indicar
     * que al menos una de las líneas pertenecientes a ese grupo de líneas con coincidencias se
     * encuentra en una zona MIST; Además, del campo INFO nos quedaremos con los subcampos AF y DP,
     * descartando el resto de subcampos; También se descartan los campos FORMAT y SM.
     *
     * @param include : Vector con los ficheros de entrada del grupo include.
     * @param exclude : Vector con los ficheros de entrada del grupo exclude.
     * @param output : Fichero de salida en el que se guardarán los resultados de la ejecución.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void combine(File[] include, File[] exclude, File output) throws IOException {
        //----- Añadido el 20/10/2014:
        // Se inicializan los Buffered Reader para los ficheros de entrada (include y exclude):
        initializeBufferedReader(include, exclude);

        // Fichero de salida en el que se escribirán los resultados de las comparaciones (en el fichero de salida únicamente
        // se escribirán líneas de los ficheros de entrada include):
        FileWriter output_file = new FileWriter(output);
        PrintWriter print_out = new PrintWriter(output_file);

        //----- Añadido el 28/11/2014:
        // Se generan las líneas de cabecera para el fichero de salida (copiará las líneas de cabeza del fichero de referencia,
        // que es el primer fichero del grupo include, añadirá los nombres de los ficheros include y exclude, y también escribirá
        // una línea de cabecera con el nombre de los campos obtenidos de los .vcf que tendremos en el fichero de salida):
        generateOutputFileHeader(include, exclude, print_out);

        // Nos posicionamos en la primera línea que no sea de cabecera en cada fichero de entrada (include y exclude):
        skipHeader(vcf_buffreader);

        // Al vector de string matches le damos como tamaño el número de ficheros de entrada (número de ficheros include
        // + número de ficheros exclude):
        matches = new String[vcf_lines.length];

        //----- Añadido el 18/11/2014:
        // Al array que almacenará los map con las listas de zonas MIST le damos como tamaño el número de ficheros include:
        CombinatorMist.map_array = new HashMap[include.length];
        // Generar los map con las listas de zonas MIST para cada uno de los ficheros de entrada include:
        for (int j = 0; j < include.length; j++)
            CombinatorMist.map_array[j] = CombinatorMist.generateMistMapFromVcf(include[j]);

        // RECORRIDO DE LOS FICHEROS DE ENTRADA.
        // Bucle externo para recorrer el primero de los ficheros de entrada include, que será nuestro fichero de referencia
        // a la hora de buscar coincidencias:
        while (vcf_lines[0] != null) {
            // De la línea leída obtenemos todos sus campos (CHROM, POS, ID, REF, ALT,...) para poder utilizar en las
            // comparaciones aquellos que nos interesan:
            String[] vcf_fields1 = vcf_lines[0].split("\t");

            // En cada iteración inicializamos el vector matches a "null" para ir almacenando en él las coincidencias
            // encontradas respecto a cromosoma (CHROM) y posición (POS). En el caso de que alguna posición quede con el
            // valor "null" significará que para el fichero correspondiente a ese elemento del vector matches no se ha
            // encontrado ninguna coincidencia respecto a cromosoma (CHROM) y posición (POS) con el fichero de referencia.
            for (int i = 0; i < matches.length; i++)
                matches[i] = null;

            // Se recorrerán el resto de ficheros de entrada (include y exclude) buscando coincidencias con el fichero de
            // referencia (primer fichero del grupo include) respecto al cromosoma (CHROM) y la posición (POS):
            for (int i = 1; i < vcf_lines.length; i++)
                while (vcf_lines[i] != null) {
                    // De la línea leída obtenemos todos sus campos (CHROM, POS, ID, REF, ALT,...) para poder utilizar en las
                    // comparaciones aquellos que nos interesan:
                    String[] vcf_fields2 = vcf_lines[i].split("\t");

                    // BÚSQUEDA DE COINCIDENCIAS RESPECTO A CROMOSOMA (CHROM) Y POSICIÓN (POS).
                    // Caso 1: Si el cromosoma (CHROM) en ambos ficheros coincide, se comprobará la posición (POS) dentro del cromosoma.
                    if (posOfChrom(vcf_fields1[0]) == posOfChrom(vcf_fields2[0]))
                        // Caso 1.1: Si la posición (POS) del fichero a comprobar es mayor que la del fichero de referencia, avanzamos
                        // una posición en el fichero de referencia (es decir, se lee la siguiente línea del fichero de referencia).
                        if ((Integer.parseInt(vcf_fields2[1])) > (Integer.parseInt(vcf_fields1[1])))
                            break; // Caso 1.2: Si la posición (POS) del fichero a comprobar es menor que la del fichero de referencia, avanzamos
                        // una posición en el fichero a comparar (es decir, se lee la siguiente línea del fichero a comparar).
                        else if ((Integer.parseInt(vcf_fields2[1])) < (Integer.parseInt(vcf_fields1[1])))
                            vcf_lines[i] = vcf_buffreader[i].readLine(); // Caso 1.3: Si las posiciones (POS) coinciden, almacenamos la línea del fichero de referencia y la del fichero
                        // a comparar en el vector matches.
                        else {
                            matches[0] = vcf_lines[0];
                            matches[i] = vcf_lines[i];

                            // Se lee la siguiente línea en el fichero a comparar (se avanza a la siguiente posición (POS)):
                            vcf_lines[i] = vcf_buffreader[i].readLine();
                        } // Caso 2: Si el cromosoma (CHROM) es diferente en ambos ficheros.
                    else
                        // Caso 2.1: Si el cromosoma (CHROM) del fichero de referencia es menor que el del fichero a comparar, avanzamos
                        // en el fichero de referencia (se lee la siguiente línea del fichero de referencia).
                        if (posOfChrom(vcf_fields1[0]) < posOfChrom(vcf_fields2[0]))
                            break; // Caso 2.2: Si el cromosoma (CHROM) del fichero de referencia es mayor que el del fichero a comparar, avanzamos
                        // en el fichero a comparar (se lee la siguiente línea del fichero a comparar).
                        else if (posOfChrom(vcf_fields1[0]) > posOfChrom(vcf_fields2[0]))
                            vcf_lines[i] = vcf_buffreader[i].readLine();
                }

            //----- Añadido el 27/10/2014 (Modificado el 26/11/2014):
            // Comprobamos que las coincidencias encontradas entre las líneas (respecto a cromosoma (CHROM) y posición (POS)) se hayan dado
            // para todos los ficheros include y que no existan coincidencias con ninguno de los ficheros exclude. En caso de coincidencia,
            // se escribe en el fichero de salida la línea del fichero de referencia después de haberla tratado para quedanos con los campos
            // que nos interesen:
            checkMatches(matches, include.length, print_out);

            // Avanzamos a la siguiente línea en el fichero de referencia (se lee la siguiente línea del fichero de referencia):
            vcf_lines[0] = vcf_buffreader[0].readLine();
        }

        //----- Añadido el 26/11/2014:
        // Se muestra por pantalla el número de coincidencias encontradas por la función "checkMatches":
        System.out.println("Número de líneas que coinciden en el grupo de ficheros include y no están en el grupo exclude (respecto a cromosoma y posición): " + count);

        // Se cierran los ficheros de entrada (include y exclude) y el fichero de salida:
        try {
            for (BufferedReader reader : vcf_buffreader)
                reader.close();
            output_file.close();
        } catch (Exception e) {
            System.out.println("Excepción encontrada al cerrar los ficheros: " + e);
            e.printStackTrace();
        }
    }

    /**
     * ----- Añadido el 20/10/2014 (Modificado el 24/10/2014) ----- Función para inicializar el
     * vector con los Buffered Reader para los distintos ficheros de entrada (include y exclude) y
     * para inicializar el vector de string que contendrá la primera línea de cada uno de los
     * ficheros; El vector de Buffered Reader será compartido por los ficheros de los grupos include
     * y exclude.
     *
     * @param include : Vector con los ficheros de entrada del grupo include.
     * @param exclude : Vector con los ficheros de entrada del grupo exclude.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void initializeBufferedReader(File[] include, File[] exclude) throws IOException {
        try {
            // Al vector de Buffered Reader le damos como tamaño el número de ficheros de entrada (número de ficheros include +
            // número de ficheros exclude):
            vcf_buffreader = new BufferedReader[(include.length + exclude.length)];

            // Al vector de string le damos como tamaño el número de ficheros de entrada (número de ficheros include + número de
            // ficheros exclude):
            vcf_lines = new String[include.length + exclude.length];

            // Inicialización (parte 1): Se inicializan los Buffered Reader correspondientes a los ficheros de entrada include:
            for (int i = 0; i < include.length; i++) {
                vcf_buffreader[i] = new BufferedReader(new FileReader(include[i]));
                // Se lee la primera línea de cada fichero:
                vcf_lines[i] = vcf_buffreader[i].readLine();
            }

            //----- Modificación del 24/10/2014:
            // Variable auxiliar que se emplea como índice para poder acceder a los diferentes elementos del vector que contiene
            // los ficheros del grupo exclude.
            int exclude_index = 0;

            // Inicialización (parte 2): Se inicializan los Buffered Reader correspondientes a los ficheros de entrada exclude:
            for (int i = include.length; i < (include.length + exclude.length); i++) {
                vcf_buffreader[i] = new BufferedReader(new FileReader(exclude[exclude_index]));
                // Se lee la primera línea de cada fichero:
                vcf_lines[i] = vcf_buffreader[i].readLine();
                // Se incrementa el índice para poder acceder al siguiente fichero del grupo exclude:
                exclude_index++;
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * ----- Añadido el 21/10/2014 ----- Función para leer en los ficheros de entrada (include y
     * exclude) hasta que nos situemos en una línea que no sea de cabecera, siendo las líneas de
     * cabecera (en el caso de los ficheros .vcf) aquellas que empiezan por el símbolo "#".
     *
     * @param array_buffreader : Buffered Reader de los ficheros de entrada (include y exclude).
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void skipHeader(BufferedReader[] array_buffreader) throws IOException {
        try {
            // Se recorren todos los ficheros de entrada (include y exclude):
            for (int i = 0; i < array_buffreader.length; i++)
                // Si la línea leída empieza por el símbolo "#" pasamos a la siguiente línea:
                while (vcf_lines[i] != null)
                    if (vcf_lines[i].startsWith("#"))
                        vcf_lines[i] = vcf_buffreader[i].readLine(); // La función termina una vez que se encuentra con la primera línea que no sea de cabecera
                    // (es decir, una línea que no empiece por "#"):
                    else
                        break;
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * ----- Añadido el 21/10/2014 ----- Función que nos indica la posición de cada cromosoma en el
     * vector de string que representa los cromosomas y que utilizaremos como referencia a la hora
     * de comparar los cromosomas para poder avanzar en el recorrido de los ficheros.
     *
     * @param chromosome : Parámetro de entrada que indica el cromosoma.
     * @return : Nos devuelve la posición correspondiente en el vector de cromosomas del cromosoma
     * que se le ha pasado como parámetro de entrada. Si no lo encuentra nos devuelve "-1".
     */
    public static int posOfChrom(String chromosome) {
        for (int i = 0; i < chromosomes.length; i++)
            if (chromosomes[i].equals(chromosome))
                return i;
        return -1;
    }

    /**
     * ----- Añadido el 27/10/2014 (Modificado el 26/11/2014) ----- Función que se utiliza para
     * comprobar que las coincidencias encontradas de cromosoma (CHROM) y posición (POS) se den en
     * todos los ficheros include y no existan coincidencias con los ficheros exclude; Para las
     * posiciones coincidentes de los ficheros include se comprueba si se encuentran en una zona
     * MIST; Para esas coincidencias de líneas, se escribe la línea correspondiente al fichero de
     * referencia (primer fichero de include) después de haberla tratado para quedarnos con la
     * información que nos interesa.
     *
     * @param matches : En este parámetro de entrada se encuentran almacenadas las líneas de los
     * distintos ficheros (include y exclude) en las que se han encontrado coincidencias (respecto
     * al cromosoma (CHROM) y la posición (POS)) con el fichero de referencia (primer fichero del
     * grupo include). Si no se ha encontrado una coincidencia para un determinado fichero, su
     * elemento correspondiente en el vector matches tendrá el valor "null".
     * @param start_exclude : Este parámetro se emplea como delimitador para conocer la posición del
     * vector de string matches a partir de la que se encuentran las líneas de los ficheros exclude.
     * @param print_out : Fichero de salida en el que se escribirán los resultados finales de las
     * comparaciones.
     */
    private void checkMatches(String[] matches, int start_exclude, PrintWriter print_out) {
        // Variable auxiliar cuyo valor será verdadero (true) cuando exista una coincidencia respecto a cromosoma (CHROM) y
        // posición (POS) entre el fichero de referencia (primer fichero del grupo include) y uno del grupo exclude:
        boolean flag_exclude = false;

        // Comprobamos que no se hayan encontrado coincidencias con los ficheros exclude, es decir, que sus líneas correspondientes
        // en matches sean nulas (null), ya que de lo contrario significará que ha encontrado al menos una coincidencia con uno de
        // los ficheros de este grupo:
        for (int i = start_exclude; i < matches.length; i++)
            if (matches[i] != null)
                flag_exclude = true;

        //----- Añadido el 19/11/2014:
        // Comprobamos las coincidencias para los ficheros de entrada include siempre que no se hayan encontrado coincidencias para
        // uno o más ficheros del grupo exclude:
        if (flag_exclude == false) {
            //----- Añadido el 26/11/2014:
            // Variable auxiliar para ver si alguna de las líneas leídas de los ficheros include está en zona MIST:
            boolean isMistZone = false;

            // Caso 1: Solamente tenemos un fichero de entrada del grupo include y uno o varios exclude:
            if (start_exclude == 1) {
                // Obtenemos los campos de la línea leída del fichero del grupo include:
                String[] vcf_fields = vcf_lines[0].split("\t");
                // Descartamos todos aquellos cromosomas que no sean del 1 al 22, X, Y o MT para que no se escriban sus líneas
                // correspondientes en el fichero de salida:
                if ((posOfChrom(vcf_fields[0])) > -1) {
                    //----- Añadido el 26/11/2014:
                    // Si la línea leída se encuentra en una zona MIST la variable auxiliar isMistZone tendrá como valor "true":
                    if (CombinatorMist.checkMistZone(vcf_fields, CombinatorMist.map_array[0]) == true)
                        isMistZone = true;

                    //----- Añadido el 26/11/2014:
                    // Escribimos en el fichero de salida la línea del fichero de referencia después de haberla tratado para que
                    // quede como nos interesa:
                    print_out.println(createOutputLine(vcf_lines[0], isMistZone));

                    // Se lleva a cabo un recuento de las coincidencias y no coincidencias encontradas:
                    count++;
                }
            } // Caso 2: Tenemos más de un fichero de entrada include y uno o varios exclude:
            else {
                // Variable auxiliar para controlar si las coincidencias de cromosoma (CHROM) y posición (POS) se dan en todos los
                // ficheros include. Si al finalizar las comprobaciones tiene como valor el número de ficheros de entrada include
                // significará que ha encontrado coincidencias para una determinada posición en todos los ficheros de este grupo:
                int matches_count = 0;

                // Si el primer elemento de matches (correspondiente al primer fichero de include o fichero de referencia) no es nulo,
                // tendremos que al menos ha encontrado una coincidencia con otro de los ficheros include:
                if (matches[0] != null)
                    // Comprobamos que se haya encontrado una coincidencia con todos los ficheros include:
                    for (int i = 1; i < start_exclude; i++)
                        if (matches[i] != null)
                            matches_count++;

                // Si se han encontrado coincidencias para todos los ficheros include (coincidencias que además no se dan en ninguno de
                // los ficheros exclude) se escribirán las líneas correspondientes de los ficheros include en el fichero de salida:
                if (matches_count == (start_exclude - 1)) {
                    for (int i = 0; i < start_exclude; i++) {
                        //----- Añadido el 18/11/2014:
                        // Obtenemos los campos de la línea del fichero del grupo include:
                        String[] matches_fields = matches[i].split("\t");
                        //----- Añadido el 26/11/2014:
                        // Si la línea leída se encuentra en una zona MIST la variable auxiliar isMistZone tendrá como valor "true":
                        if (CombinatorMist.checkMistZone(matches_fields, CombinatorMist.map_array[i]) == true) {
                            isMistZone = true;
                            break;
                        }
                    }

                    //----- Añadido el 26/11/2014:
                    // Escribimos en el fichero de salida la línea del fichero de referencia después de haberla tratado para que
                    // quede como nos interesa:
                    print_out.println(createOutputLine(matches[0], isMistZone));

                    // Se lleva a cabo un recuento de las coincidencias y no coincidencias encontradas:
                    count++;
                }
            }
        }
    }

    /**
     * ----- Añadido el 26/11/2014 (Modificado el 28/11/2014) ----- Función que dada una línea de un
     * fichero en formato .vcf, la trata para obtener los campos que nos interesan; En un principio
     * se mantendrán los campos originales salvo en el campo INFO (campo 8 de un fichero .vcf) donde
     * nos quedaremos con los subcampos AF y DP, y añadiremos la etiqueta "MistZone" en aquellos
     * casos en los que corresponda; También se descartan los campos FORMAT y SM.
     *
     * @param vcf_line : Línea leída de un fichero en formato .vcf.
     * @return : Devuelve una string que contiene la línea pasada como parámetro de entrada
     * modificada para que contenga la información de interés.
     */
    private String createOutputLine(String vcf_line, boolean isMistZone) {
        // Obtenemos los campos de la línea pasada por parámetro:
        String[] vcf_fields = vcf_lines[0].split("\t");

        // Se construye la string que devolverá la función con los campos que nos interesan (los 8 primeros campos del fichero .vcf):
        String output_line = vcf_fields[0];
        //----- Añadido el 28/11/2014: Se modificó el bucle "for" para tratar los 8 primeros campos del fichero .vcf y descartar el resto.
        for (int i = 1; i < 8; i++)
            // Se trata el campo INFO para obtener los subcampos que nos interesan:
            if (i == 7) {
                String[] info_fields = vcf_fields[i].split(";");
                String new_info = "";
                // Si alguna de las líneas en el grupo de líneas coincidentes respecto a cromosoma (CHROM) y posición (POS) se encuentra
                // en un zona MIST, añadimos la etiqueta "MistZone" al campo INFO:
                if (isMistZone == true)
                    new_info += "MistZone:";
                // Se recorren los subcampos del campo INFO para quedarnos con aquellos que son de interés:
                for (int j = 0; j < info_fields.length; j++) {
                    // Campo AF:
                    if (info_fields[j].startsWith("AF="))
                        new_info += (info_fields[j] + ":");
                    // Campo DP:
                    if (info_fields[j].startsWith("DP="))
                        new_info += (info_fields[j] + ":");
                }

                // En un principio se desconoce si los subcampos del campo INFO pueden presentar un orden específico o estar desordenados,
                // por lo que se organizan para poder mantener el formato de los subcampos separados por punto y coma (;):
                info_fields = new_info.split(":");
                new_info = ("\t" + info_fields[0]);
                for (int j = 1; j < info_fields.length; j++)
                    new_info += (";" + info_fields[j]);
                output_line += new_info;
            } // Para todos los campos que no sean el campo INFO, los añadimos a la string de salida ya que no les haremos cambios:
            else
                output_line += ("\t" + vcf_fields[i]);

        // Devuelve la línea generada:
        return output_line;
    }

    /**
     * ----- Añadido el 28/11/2014 (Modificado el 3/12/2014) ----- Función que se encarga de
     * escribir las líneas de cabecera del fichero de salida (fichero en formato .vcf).
     *
     * @param include : Ficheros de entrada include.
     * @param exclude : Fichero de entrada exclude.
     * @param print_out : Fichero de salida.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void generateOutputFileHeader(File[] include, File[] exclude, PrintWriter print_out) throws IOException {
        try {
            // Se recorre el fichero de referencia copiando en el fichero de salida las líneas de cabecera que nos interesen (en un fichero
            // en formato .vcf, las líneas de cabecera empiezan por "#"):
            while (vcf_lines[0] != null) {
                // Leemos hasta la última línea de cabecera:
                while (!(vcf_lines[0].startsWith("#CHROM")))
                    //----- Añadido el 3/12/2014:
                    // No escribimos las líneas de cabecera correspondientes a los campos de genotipo ni tampoco las líneas de cabecera para
                    // aquellos cromosomas que no sean del 1 al 22, X, Y o MT.
                    if (vcf_lines[0].startsWith("##FORMAT") || vcf_lines[0].startsWith("##contig=<ID=GL"))
                        vcf_lines[0] = vcf_buffreader[0].readLine(); // Para el campo INFO, tendremos las cabeceras correspondientes a los subcampos MistZone, AF y DP:
                    else if (vcf_lines[0].startsWith("##INFO")) {
                        print_out.println("##INFO=<ID=MistZone,Type=Flag,Description=\"If present, indicates that the position is in an MIST Zone\">");
                        print_out.println("##INFO=<ID=AF,Number=A,Type=Float,Description=\"Allele Frequency, for each ALT allele, in the same order as listed\">");
                        print_out.println("##INFO=<ID=DP,Number=1,Type=Integer,Description=\"Approximate read depth; some reads may have been filtered\">");
                        vcf_lines[0] = vcf_buffreader[0].readLine();
                        // Las líneas de cabecera para el resto de los subcampos del campo INFO no se escriben en el fichero de salida:
                        while (vcf_lines[0].startsWith("##INFO"))
                            vcf_lines[0] = vcf_buffreader[0].readLine();
                    } // El resto de líneas de cabecera se copian del fichero de referencia (primer fichero de include):
                    else {
                        print_out.println(vcf_lines[0]);
                        vcf_lines[0] = vcf_buffreader[0].readLine();
                    }
                break;
            }

            // Escribimos las líneas de cabecera que indican los nombres de todos los ficheros include y exclude involucrados:
            for (int i = 0; i < include.length; i++)
                if (i == 0)
                    print_out.println("##Reference_Include=File:" + include[i].getPath());
                else
                    print_out.println("##Include=File:" + include[i].getPath());
            for (int i = 0; i < exclude.length; i++)
                print_out.println("##Exclude=File:" + exclude[i].getPath());

            // Se escriben en el fichero de salida los nombres de los campos del fichero .vcf que están presentes:
            print_out.println("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
