//  Código del cliente
// import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        // Crea e instala el gestor de seguridad
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            // Realiza la conexion con un servidor replicado (en este caso el 0)
            // Crea el stub para el cliente especificando el nombre del servidor
            Registry mireg = LocateRegistry.getRegistry("127.0.0.1", 1099);
            
            // Se conecta a la replica0
            String server_name = "replica0";

            System.out.println("BIENVENIDO AL SERVIDOR DE DONACIONES\n\nRealizando conexión con un servidor de donaciones (" + server_name +")");
            interfazClienteServidor server = (interfazClienteServidor)mireg.lookup(server_name);

            // Inicia la funcionalidad del programa (menú)
            Scanner sc = new Scanner(System.in);
            int id = -1;
            
            while (true) {
                System.out.println("\n\nAntes de continuar, introduzca su identificador de cliente:");
                id = sc.nextInt();
                sc.nextLine(); // Limpiar buffer
                
                Boolean continuar = true;
                while (continuar) {
                    System.out.println("\t1. Registrarme");
                    System.out.println("\t2. Realizar donacion");
                    System.out.println("\t3. Consultar total donaciones");
                    System.out.println("\t4. Cerrar sesion (vuelve al paso de introduzca su id de cliente)");
                    int opcionMenu = sc.nextInt();
                    sc.nextLine(); // Limpiar buffer
                    
                    switch (opcionMenu) {
                        case 1: // Registro
                            System.out.println("Realizando solicitud de registro al servidor...");
                            if(server.registrar(id)) System.out.println("\t... registro de cliente realizado con éxito");
                            else System.out.println("\t... error en el registro, puede que ya se haya registrado anteriormente.");
                            break;
                        case 2:
                            System.out.println("Introduzca la cantidad a donar:");
                            double cantidad = sc.nextDouble();
                            sc.nextLine(); // Limpiar buffer
                            System.out.println("Realizando solicitud de donacion al servidor...");
                            if(server.donar(id, cantidad)) System.out.println("\t... donacion realizada con éxito");
                            else System.out.println("\t... error al efectuar la donación, asegúrese de que se ha registrado previamente y de que la cantidad es positiva");
                            break;
                        case 3:
                            System.out.println("Realizando solicitud de consulta de total donaciones al servidor...");
                            double total = server.totalDonaciones(id);
                            if(total>=0) System.out.println("El total recaudado hasta el momento son " + total + "$");
                            else System.out.println ("Debes registrarte (si no lo estás) y donar para poder acceder a esa información");
                            break;
                        case 4:
                            System.out.println("...Cerrando sesion");
                            continuar = false;
                            break;
                        default:
                            System.out.println("Opción no válida");
                            break;
                    }
                }
            }            
        } catch(NotBoundException | RemoteException e) {
            System.err.println("Exception del sistema: " + e);
        }

        System.exit(0);

    }
}