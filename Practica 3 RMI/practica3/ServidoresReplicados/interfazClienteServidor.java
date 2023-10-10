// Define la interfaz remota Cliente-Servidor
import java.rmi.Remote;
import java.rmi.RemoteException;

// Comentarios acerca de las excepciones RMI:
// RemoteException: Esta excepción se lanza si ocurre un error en la comunicación RMI entre el cliente y el servidor.
// NotBoundException: Esta excepción se lanza si no se encuentra el objeto remoto registrado en el registro RMI.

/**
 * @brief Interfaz accesible para que el cliente se comunique
 * con los servidores replicados a través de las funciones especificadas
 */
public interface interfazClienteServidor extends Remote {
    /**
     * Registra a un nuevo cliente
     * @param id Identificador del cliente, para comprobar si ya está registrado o no
     * @return True si se ha podido registrar al cliente, false en otro caso
     * @throws RemoteException
     */
    public Boolean registrar(int id) throws RemoteException;
    
    /**
     * Registra una donación del cliente 
     * @param id Identificador del cliente
     * @param cantidad cantidad a donar
     * @pre cantidad>0
     * @throws RemoteException
     */
    public Boolean donar (int id, double cantidad) throws RemoteException;
    
    /**
     * Devuelve el total recaudado entre todos los servidores replicados
     * @param id cliente
     * @return total donado
     * @pre el cliente debe estar registrado y haber donado algo
     * @throws RemoteException
     */
    public double totalDonaciones(int id) throws RemoteException;
} 