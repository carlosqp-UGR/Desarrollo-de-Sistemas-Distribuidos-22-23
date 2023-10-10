#!/bin/bash

# Compilar
make -f Makefile.calculadora

# Lanza el servidor en bg y muestra su salida por terminal
echo '------------- Salida Server  -------------'
./calculadora_server &

# Lanza varias veces el cliente con distintas operaciones 
# y guarda la salida en el archivo cliente.txt
./calculadora_client localhost 2 + 8 > cliente.txt
./calculadora_client localhost 2 - 10 >> cliente.txt
./calculadora_client localhost 2 x 50 >> cliente.txt
./calculadora_client localhost 2 / 9 >> cliente.txt
./calculadora_client localhost 2 / 0 >> cliente.txt

#Imprime la salida del cliente
echo '------------- Salida Cliente -------------'
cat cliente.txt
