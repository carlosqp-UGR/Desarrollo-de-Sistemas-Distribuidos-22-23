#!/bin/sh -e
# ejecutar = Macro para compilacion y ejecucion del programa ejemplo
# en una sola maquina Unix de nombre localhost.

echo
echo "Lanzando el ligador de RMI … "
rmiregistry &

echo
echo "Compilando con javac ..."
javac *.java

echo
echo "Lanzando el servidor en otra terminal"
gnome-terminal --working-directory=$PWD -- java -cp . -Djava.rmi.server.codebase=file:./ -Djava.rmi.server.hostname=localhost -Djava.security.policy=server.policy Ejemplo &

# Damos tiempo a que se lance el programa
sleep 5

echo
echo "Lanzando el programa cliente multihebrado"
echo

java -cp . -Djava.security.policy=server.policy Cliente_Ejemplo_Multi_Threaded localhost 5

sleep 2