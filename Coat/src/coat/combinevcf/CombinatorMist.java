package coat.combinevcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ----- Añadido el 19/11/2014 -----
 * Clase en la que nos encontramos con las funciones necesarias para que dado un fichero en formato .vcf se obtenga     *
 * su fichero en formato MIST correspondiente (ambos ficheros deben existir en el mismo directorio con el mismo nombre  *
 * pero distinta extensión, por ejemplo: "niv_19.vcf" y "niv_19.mist") para generar un map en el que se tenga una lista *
 * con todas las zonas MIST existentes para ese fichero y poder comprobar cúantas posiciones obtenidas del .vcf se      *
 * encuentran en una zona MIST.                                                                                         *
 * 
 * @author Jacob Henríquez
 */
public class CombinatorMist {
    //----- Añadido el 17/11/2014:
    // Vector (array) de map en el que cada posición del array será un map correspondiente a un archivo MIST que 
    // tendrá como claves los cromosomas y como valor asociado a cada clave una lista de todas las intersecciones 
    // exón-región pobre (zona MIST) que se encuentren para cada cromosoma: 
    public static Map<String, ArrayList<MistZone>>[] map_array;
                
    // Variable utilizada para llevar el recuento de todas las líneas encontradas en un fichero .vcf que están 
    // en una zona MIST:
    private static int count = 0;
    
    
    public CombinatorMist() {
    }
        
    void start() throws FileNotFoundException, IOException{
        // Ficheros de entrada (en formato .vcf):
        File[] vcf_files = {
            new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_19.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_032.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_60.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_062.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_084.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_85.vcf"),
            //new File("/home/uai02/Investigacion_Jacob/ficheros_vcf_mist/niv_102.vcf"),
        };
        
        intersectVcfMist(vcf_files);
    }

    /**
     * ----- Añadido el 6/11/2014 -----
     * Función que se utiliza para comprobar cuántas posiciones leídas de un fichero .vcf se encuentran en una zona MIST.
     * @param vcf_files : Vector con los ficheros de entrada en formato .vcf.
     * @throws IOException 
     * @throws FileNotFoundException
     */
    private static void intersectVcfMist (File[] vcf_files) throws IOException, FileNotFoundException {                       
        //----- Añadido el 17/11/2014:
        // Le damos como tamaño al array que almacenará los map el número de ficheros de entrada:
        map_array = new HashMap[vcf_files.length];
                        
        // Se leen todos los ficheros de entrada uno por uno:
        for (int i = 0; i < vcf_files.length; i++){ 
            // Se muestra por pantalla el nombre del fichero de entrada .vcf que se está leyendo:
            System.out.println ("Fichero de entrada .vcf: " + vcf_files[i].getAbsolutePath());   
            
            //----- Añadido el 18/11/2014:
            // Parte 1: Se genera un map de zonas MIST para cada uno de los ficheros de entrada .vcf:
            map_array[i] = generateMistMapFromVcf(vcf_files[i]);
                      
            // Parte 2: Se recorre el fichero de entrada .vcf y se comprueba el número de posiciones 
            // que se encuentran en una zona MIST.                               
            try (BufferedReader vcf_file = new BufferedReader(new FileReader(vcf_files[i]))) {
                
                // Se lee la primera línea del fichero .vcf:
                String vcf_line = vcf_file.readLine();
                
                while (vcf_line != null){
                    // Se lee el fichero hasta encontrar la primera línea que no sea de
                    // cabecera (en el caso de los ficheros en formato .vcf las líneas de 
                    // cabecera empiezan por el símbolo "#").
                    if (!(vcf_line.startsWith("#"))){
                        // Obtenemos los campos de la línea leída:
                        String[] vcf_fields = vcf_line.split("\t");
                        
                        //----- Añadido el 18/11/2014:
                        // Comprobamos si la línea leída se encuentra en una zona MIST y en caso
                        // afirmativo se muestra esa línea por pantalla:
                        if (checkMistZone(vcf_fields, map_array[i]) == true){
                            System.out.println(vcf_line);
                        }
            
                    }
                                           
                    // Se lee la siguiente línea del fichero .vcf:
                    vcf_line = vcf_file.readLine();
                }
            
                // Muestra por pantalla el número total de posiciones que se encuentran en una zona MIST para un 
                // determinado fichero de entrada (en formato .vcf):    
                System.out.println ("Número de posiciones que se encuentran en una zona MIST = " + count + "\n");
                
                // Cerramos el fichero de entrada .vcf:
                vcf_file.close();
                                
                // Se inicializa a cero el contador de posiciones encontradas en zonas MIST para poder utilizarlo
                // con el siguiente fichero de entrada .vcf:
                count = 0;
                
            }catch (FileNotFoundException e) {
                System.out.println(e);
            }catch (Exception e1) {
                e1.printStackTrace();
            }
        }                
    }
                
    /**
     * ----- Añadido el 18/11/2014 -----
     * Función a la que se le pasa un fichero .vcf como parámetro de entrada del que obtendrá el fichero MIST correspondiente, 
     * y a partir del fichero MIST generará un map que contiene todas sus zonas MIST ordenadas y sin zonas repetidas.
     * @param vcf_file : Fichero de entrada en formato .vcf.
     */
    public static Map generateMistMapFromVcf (File vcf_file){
        // map que devolverá la función en el que están incluidas todas las zonas MIST correspondientes al archivo MIST asociado
        // al fichero .vcf que se pasa como parámetro de entrada:
        Map<String, ArrayList<MistZone>> mist_map = new HashMap();
        
        // Fichero de entrada MIST obtenido a partir de su correspondiente archivo .vcf:
        File mist_file_name = new File(getMistFromVcf(vcf_file));
        
        try (BufferedReader mist_file = new BufferedReader(new FileReader(mist_file_name))) {            
            // Se lee la línea de cabecera del fichero MIST (es una línea con el nombre de todos los campos):
            mist_file.readLine();
            // Se lee la primera línea con datos del fichero MIST:
            String mist_line = mist_file.readLine();
                       
            // Se recorre el fichero MIST:
            while (mist_line != null){
                // Obtenemos los campos para cada línea leída:
                String[] mist_fields = mist_line.split("\t");
                
                // Se le pasan los campos obtenidos de la línea leída del fichero MIST y el map que deberá devolver 
                // a la función que se encarga de construir el map en el que están incluidas las listas ordenadas 
                // y sin zonas repetidas de zonas MIST para cada cromosoma:
                createMistMapOrdered(mist_fields, mist_map);
                    
                // Se lee la siguiente línea del fichero MIST:
                mist_line = mist_file.readLine();
            }
            
            // Cerramos el fichero de entrada MIST:
            mist_file.close();
                                                        
        }catch (FileNotFoundException e) {
            System.out.println(e);
        }catch (Exception e1) {
            e1.printStackTrace();
        } 
        
        return mist_map;
    }
    
    /**
     * ----- Añadido el 11/11/2014 -----
     * Función para obtener el fichero MIST correspondiente al fichero .vcf que se le pasa como parámetro de entrada, 
     * teniendo en cuenta que ambos ficheros deben existir en el mismo directorio y tener el mismo nombre con extensión 
     * diferente, por ejemplo: "niv_19.vcf" y "niv_19.mist". 
     * @param vcf_file : Archivo .vcf del que se obtendrá el nombre de su fichero MIST correspondiente.
     * @return : Devuelve la ruta completa y el nombre del fichero MIST correspondiente al .vcf.
     */
    private static String getMistFromVcf (File vcf_file){
        // Se obtiene la ruta absoluta del fichero .vcf:
        String vcf_path = vcf_file.getAbsolutePath();
        // Quitamos la extensión del archivo .vcf:
        String path = vcf_path.substring(0, vcf_path.lastIndexOf("."));
        // Le añadimos la extensión .mist y tenemos la ruta del archivo MIST correspondiente al .vcf:
        String mist_file = path + ".mist";
                
        return mist_file;
    }
    
    /**
     * ----- Añadido el 6/11/2014 (Modificado el 17/11/2014) -----
     * Función que se utiliza para construir el map asociado a un fichero MIST que contendrá los distintos objetos de 
     * tipo MistZone, es decir, cada cromosoma (clave del map) tendrá asociada una lista en la que se incluyen todas 
     * sus intersecciones exón-región pobre (zona MIST) ordenadas y sin zonas repetidas.
     * @param mist_fields : Vector de string en el que cada elemento es un campo de la línea leída del fichero MIST.
     * @param mist_map : Map que deberá rellenarse con sus correspondientes listas de zonas MIST para cada cromosoma.
     */
    private static void createMistMapOrdered (String[] mist_fields, Map <String,ArrayList<MistZone>> mist_map){
        // Si el map para un determinado fichero de entrada tiene el valor "null", se genera ese map con su clave correspondiente 
        // (el cromosoma) y se añade a la lista de zonas MIST el primer elemento:
        if (mist_map == null){
            mist_map = new HashMap<>();
            
            ArrayList<MistZone> mist_zone_list = new ArrayList<>();
            MistZone mist_zone_aux = createMistZone(mist_fields);
            mist_zone_list.add(mist_zone_aux);
            
            mist_map.put(mist_fields[0], mist_zone_list);
                  
        }
        // Si el map para un determinado fichero de entrada no tiene el valor "null":
        else{
            // Para cada clave del map (el cromosoma) se irán añadiendo a su lista correspondiente las diferentes intersecciones 
            // exón-región pobre (zonas MIST):
            ArrayList<MistZone> mist_zone_list = mist_map.get(mist_fields[0]);
            // Variable auxiliar de tipo MistZone que contiene la nueva zona MIST que queremos añadir a la lista:
            MistZone mist_zone_aux = createMistZone(mist_fields);
            
            // Variable auxiliar que utilizaremos como índice para saber en qué posición de la lista se debe insertar la nueva zona 
            // MIST para mantener la lista de zonas MIST ordenada:
            int list_index = 0;
            
            // Variable de tipo boolean que utilizaremos para controlar que en la lista no se inserten zonas MIST repetidas:
            boolean repeat = false;

            // Caso 1: No existe la clave para un determinado cromosoma, es decir, nos encontramos ante la primera inserción
            // en la lista de una zona MIST para un determinado cromosoma:
            if (mist_zone_list == null) {
                mist_zone_list = new ArrayList<>();
                mist_map.put(mist_fields[0], mist_zone_list);
            }
        
            // Caso 2: Para una determinada clave (cromosoma) ya existen uno o más elementos en la lista de zonas MIST, por 
            // lo que la nueva zona MIST se insertará en la posición correspondiente para mantener la lista ordenada:
            else{            
                // Se lee el map de zonas MIST para localizar la posición en la que debemos insertar el nuevo elemento en 
                // la lista: 
                mist_map.get(mist_fields[0]);          
                Iterator<MistZone> itMistZone = mist_zone_list.iterator(); 
                
                while(itMistZone.hasNext()){
                    MistZone mist_zone = itMistZone.next();                
                    // Si nos encontramos en la lista una zona MIST en la que el valor de su campo "start" es mayor que el valor del 
                    // campo "start" de la zona MIST que queremos insertar en la lista, en la variable auxiliar "list_index" tendremos 
                    // la posición en la que debemos insertar el nuevo elemento para mantener la lista ordenada:
                    if (mist_zone.getStart() > mist_zone_aux.getStart()){
                        break;
                    }
                
                    // Si nos encontramos en la lista una zona MIST en la que el valor de su campo "start" es igual al de la zona MIST que 
                    // queremos insertar, comprobaremos el valor de los campos "end":
                    if (mist_zone.getStart() == mist_zone_aux.getStart()){
                        // Si los valores del campo "end" de ambas zonas MIST son iguales, significará que la zona MIST que queremos insertar 
                        // ya se encuentra en la lista y por lo tanto la variable auxiliar "repeat" tendrá como valor "true":
                        if (mist_zone.getEnd() == mist_zone_aux.getEnd()){
                            repeat = true;
                            break; 
                        }
                        
                        // Si el valor de "end" de la zona MIST que ya está en la lista es mayor que el valor de "end" de la zona MIST que 
                        // queremos insertar, en la variable auxiliar "list_index" tendremos la posición en la que debemos insertar el nuevo
                        // elemento para mantener la lista ordenada:
                        if (mist_zone.getEnd() > mist_zone_aux.getEnd()){
                            break;
                        }
                    }
                
                    // Incrementamos el valor de la variable auxiliar: 
                    list_index++;
                }
            }
            
            // Si la zona MIST no se encontraba repetida, la añadimos al map:
            if (repeat == false){
                mist_zone_list.add(list_index, mist_zone_aux);    
            }
        }
    }
    
    /**
     * ----- Añadido el 6/11/2014 (Modificado el 14/11/2014) -----
     * Función que se encarga de construir los objetos de tipo MistZone para cada línea leída del fichero MIST. 
     * @param mist_fields : Vector de string en el que cada elemento es un campo de la línea leída del fichero MIST.
     * @return : Devuelve un objeto de tipo MistZone. 
     */
    private static MistZone createMistZone (String[] mist_fields) {
        // Se crea un objeto de tipo MistZone que será el que devuelva la función:
        MistZone mist_zone = new MistZone();
                                
        // Obtenemos la posición de inicio de la intersección exón-región pobre (zona MIST), es decir, el valor mayor 
        // entre los campos "exon_start" y "poor_start" del fichero MIST (segundo y cuarto campo respectivamente):
        if (Integer.parseInt(mist_fields[1]) >= Integer.parseInt(mist_fields[3])){
            mist_zone.setStart(Integer.parseInt(mist_fields[1]));
        }
        else {
            mist_zone.setStart(Integer.parseInt(mist_fields[3]));
        }
        
        // Obtenemos la posición de final de la intersección exón-región pobre (zona MIST), es decir, el valor menor 
        // entre los campos "exon_end" y "poor_end" del fichero MIST (tercer y quinto campo respectivamente):
        if (Integer.parseInt(mist_fields[2]) <= Integer.parseInt(mist_fields[4])){
            mist_zone.setEnd(Integer.parseInt(mist_fields[2]));
        }
        else {
            mist_zone.setEnd(Integer.parseInt(mist_fields[4]));
        }
        
        // Devuelve el objeto de tipo MistZone originado:
        return mist_zone;
    }
    
    /**
     * ----- Añadido el 6/11/2014 (Modificado el 19/11/2014) -----
     * Función que se encarga de comprobar si una determinada posición leída de un fichero .vcf se encuentra en una zona MIST. 
     * @param vcf_fields : Campos correspondientes a la línea leída del fichero .vcf
     * @param mist_map : Map con la lista de zonas MIST asociado al fichero .vcf que contiene la línea que se va a comprobar.
     * @return "true" si la posición se encuentra en una zona MIST y "false" en caso contrario.
     */
    public static boolean checkMistZone (String[] vcf_fields, Map <String,ArrayList<MistZone>> mist_map){
        //Recorremos el map que contiene las intersecciones exón-región pobre (zonas MIST) para un determinado cromosoma:
        ArrayList<MistZone> mist_zone_list = mist_map.get(vcf_fields[0]);
        if (mist_zone_list != null){            
            Iterator<MistZone> itMistZone = mist_zone_list.iterator(); 
                while(itMistZone.hasNext()){
                MistZone mist_zone = itMistZone.next();
                int pos = Integer.parseInt(vcf_fields[1]);
                
                // La lista de zonas MIST en el map se encuentra ordenada, por lo tanto desde que encontremos una posición 
                // de "start" que sea mayor a la posición (POS) que estamos comparando, paramos la búsqueda: 
                if (mist_zone.getStart() > pos){
                    return false;
                }
                
                // Comprobamos si la posición se encuentra en una zona MIST: 
                if ((pos >= mist_zone.getStart()) && (pos <= mist_zone.getEnd())){
                    // Se incrementa el contador de posiciones encontradas en zonas MIST:
                    count++;  
                    
                    return true;
                }
            } 
        }
        return false;
    }
   
}
