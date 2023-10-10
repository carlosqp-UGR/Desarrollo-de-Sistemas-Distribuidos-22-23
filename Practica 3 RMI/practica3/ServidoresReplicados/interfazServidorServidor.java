// Define la interfaz remota Servidor-Servidor
import java.rmi.Remote;
import java.rmi.RemoteException;

// Comentarios acerca de las excepciones RMI:
// RemoteException: Esta excepción se lanza si ocurre un error en la comunicación RMI entre el cliente y el servidor.
// NotBoundException: Esta excepción se lanza si no se encuentra el objeto remoto registrado en el registro RMI.

/**
 * @brief Interfaz accesible para que los servidores se comuniquen entre sí
 */
public interface interfazServidorServidor extends Remote {
    
    /**
     * Devuelve true si un cliente está registrado en el servidor o no
     * @param id
     * @return
     * @throws RemoteException
     */

    public Boolean isRegistered(int id) throws RemoteException;

    /**
     * Devuelve el total de clientes registrados en el servidor
     * @return
     * @throws RemoteException
     */
    public int getNumClientes() throws RemoteException;

    /**
     * Devuelve el nombre del objeto
     * @return
     * @throws RemoteException
     */
    // public String getName() throws RemoteException;

    /**
     * Acción de añadir un cliente al registro del servidor
     * @param id
     * @throws RemoteException
     */
    public void addCliente(int id) throws RemoteException;

    /**
     * Registra/actualiza la nueva donación en el servidor. No confundir
     * con la operación de donar de la otra interfaz
     * @param id
     * @param cantidad
     * @throws RemoteException
     */
    public void makeDonation(int id, double cantidad) throws RemoteException;

    /**
     * Devuelve el subtotal de donaciones recibidas por el servidor
     * @return
     * @throws RemoteException
     */
    public double getSubtotalDonaciones() throws RemoteException;

    /**
     * Realiza la operación real de devolver el total de donaciones,
     * calcular todos los subtotales y juntarlos
     * @param id del cliente
     * @return
     * @throws RemoteException
     */
    public double getTotalDonaciones(int id) throws RemoteException;
} 
