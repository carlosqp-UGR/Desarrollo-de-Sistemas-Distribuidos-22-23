// Implementa la interfaz remota cliente-servidor
// y la interfaz remota servidor-servidor
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class Servidor extends UnicastRemoteObject implements interfazClienteServidor, interfazServidorServidor{

    // Atributos
    private static int num_servidores = 0;  // Para asignar un identificador único a cada nombre de servidor
    public String name; // nombre del servidor replicado (es el mismo que en el registro RMI)
    private Registry registro;  // Registro RMI del que se obtendrán las referencias al resto de replicas remotas
    private ArrayList<interfazServidorServidor> replicas;   // referencias al resto de servidores replicados
    private Map<Integer, Double> donaciones;    // Estructura que almacena los clientes y sus aportaciones


    // Constructor
    public Servidor(Registry registro)  throws RemoteException {
        // Guardar referencia al registro RMI del que poder invocar
        // las llamadas los servidores replicados
        this.registro = registro;
        
        // Inicializar estructuras
        donaciones = new HashMap<Integer,Double>();
        replicas = new ArrayList<interfazServidorServidor>();
        
        // Asignar un nombre único
        name = "replica" + num_servidores;
        num_servidores++;
    }

    /// Métodos propios de la clase Servidor

    public void addReplica(String name) {
        try {
            if(name!=this.name) {
                // Obtiene una referencia al objeto remoto desde el registro y lo añade a su estructura
                replicas.add((interfazServidorServidor) registro.lookup(name));
            }
        } catch (Exception e) {
            System.err.println("El objeto remoto con el nombre " + name + " no se ha podido añadir.");
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    /// Implementacion de la interfaz servidor-servidor

    public Boolean isRegistered(int id) throws RemoteException {
        return donaciones.containsKey(id);
    }

    public int getNumClientes() throws RemoteException {
        System.out.println("\t" + getName() + ": actualmente tengo " + donaciones.size() + " clientes registrados" );
        return donaciones.size();
    }

    public void addCliente(int id) throws RemoteException {
        donaciones.put(id, 0.0);
        System.out.println("\t" + getName() + ": registrado nuevo cliente (id=" + id + ")");
    }
    
    public void makeDonation(int id, double cantidad) throws RemoteException {
        if(isRegistered(id)) {
            double cantidadAnterior = donaciones.get(id).doubleValue();
            donaciones.put(id, cantidadAnterior + cantidad);
            System.out.println("\t" + getName() + ": registrada donación del cliente " + id + " por valor de " + cantidad);
        }
    }

    public double getSubtotalDonaciones() throws RemoteException {
        double total = 0;
        for (Map.Entry<Integer, Double> entry : donaciones.entrySet()) {
            total += entry.getValue();
        }
        System.out.println("\t" + getName() + ": subtotal " + total + "$ en mi replica ");

        return total;
    }

    public double getTotalDonaciones(int id) throws RemoteException {
        if(isRegistered(id) && donaciones.get(id).doubleValue()>0) {
            System.out.println("\t" + getName() + ": recopilando información de subtotales de otras replicas para responder al cliente " + id);
            double total = getSubtotalDonaciones();
            for(int i=0; i<replicas.size(); ++i)
                total += replicas.get(i).getSubtotalDonaciones();
            return total;
        } else 
            return -1;
    }

    // Implementacion de la interfaz cliente servidor
    
    public Boolean registrar(int id) throws RemoteException {
        
        System.out.println(getName() + ": recibida petición de registro del cliente " + id);
        
        // Comprueba si el cliente está registrado en algún servidor
        Boolean estaRegistrado = false;
        int min_indice = replicas.size();
        int min_clientes = getNumClientes();

        for(int i=0; i<=replicas.size() && !estaRegistrado; ++i) {
            
            interfazServidorServidor rp;
            // Se comprueba a sí mismo en la última iteración (índice=replicas.size())
            if (i==replicas.size())  rp = this;
            else rp = replicas.get(i);

            // Comprueba si es menor y actualiza
            estaRegistrado = rp.isRegistered(id);
            if(rp.getNumClientes()<min_clientes) {
                min_clientes = rp.getNumClientes();
                min_indice = i;
            }
        }

        // Si no esta registrado, lo registramos en la réplica que menos 
        // clientes tenga. Esto lo hacemos por medio del método addCliente
        // de la interfaz servidor-servidor
        if(!estaRegistrado) {
            if(min_indice == replicas.size()) this.addCliente(id);
            else replicas.get(min_indice).addCliente(id);
            return true;
        } else {
            System.out.println(getName() + ": Error, cliente ya registrado" );
            return false;
        }

    }
    
    public Boolean donar (int id, double cantidad) throws RemoteException {
        // Informa por pantalla de que ha recibido una peticion
        System.out.println(getName() + ": recibida petición de donacion del cliente " + id + " por un importe de " + cantidad + "$");

        
        // Busca al cliente de forma local
        if (cantidad>0) {
            if(isRegistered(id)) {
                makeDonation(id, cantidad);
            } else {
                // Busca al cliente en su réplica y efectúa allí la donación
                for(int i=0; i<replicas.size(); ++i) {
                    if(replicas.get(i).isRegistered(id)) {
                        replicas.get(i).makeDonation(id, cantidad);
                        break;
                    }
                }
            }
            return true;
        } else {
            System.out.println( getName() + ": no se puede donar una cantidad menor o igual que 0");
            return false;
        }
    }
    
    public double totalDonaciones(int id) throws RemoteException {
        // Informa por pantalla de que ha recibido una peticion
        System.out.println(getName() + ": recibida petición de total donaciones del cliente " + id );
        
        // Busca al servidor donde está alojado el cliente y llama a la operación de ese servidor
        if(isRegistered(id))
            return getTotalDonaciones(id);
        else {
            for(int i=0; i<replicas.size(); ++i) {
                if(replicas.get(i).isRegistered(id)) 
                    return replicas.get(i).getTotalDonaciones(id);
            }
        }
        // Si llega hasta este punto es porque no está registrado
        return -1;
    }


    // Main del servidor
    public static void main(String[] args) {
        // Crea e instala el gestor de seguridad
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    
        try {
            int n_replicas = Integer.parseInt(args[0]);
            if(n_replicas<=0) System.exit(-1);

            Servidor[] v_server = new Servidor[n_replicas];
            System.out.println("creando registro RMI ...");
            // Crear registro RMI para interacción servidor-servidor y cliente-servidor
            Registry registry0=LocateRegistry.createRegistry(1099);

            System.out.println("Instanciando " + n_replicas + " replicas...");
            // Crear los servidores y pasarles el registro
            for (int i=0;i<n_replicas;i++) {
                // Creamos una réplica
                v_server[i] = new Servidor(registry0);

                // La hacemos accesible
                Naming.rebind(v_server[i].getName(), v_server[i]);
                System.out.println("\treplica "+i+" lista");
            }

            // Ahora, a cada réplica le pasamos el resto de réplicas para
            // que almacenen referencias a ellas
            for (int i=0;i<n_replicas;i++) {
                for (int j=0;j<n_replicas;j++) {
                    if (i!=j) v_server[i].addReplica(v_server[j].getName());
                }
            }
            System.out.println("¡Todas las replicas se han conectado entre sí!");
            System.out.println("¡Servidores replicados listos para responder peticiones de clientes!");

            System.out.println("\nServidor RemoteException | MalformedURLExceptiondor preparado\n");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}