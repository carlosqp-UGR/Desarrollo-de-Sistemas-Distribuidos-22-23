import argparse

from calculadora import Calculadora

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

# Funcion auxiliar para imprimir las matrices
def print_m(m1):
	for i in range(len(m1)):
		print(m1[i])


# Leemos y procesamos los argumentos (calculadora básica)
parser = argparse.ArgumentParser();

#argumento opcional
parser.add_argument("--host", type=str, help= 'IP del servidor')
#argumentos obligatorios
parser.add_argument("mode", type=str, help='modo: b(basico) | v(vectores) | m(matrices)')

args = parser.parse_args();

if args.host:
	transport = TSocket.TSocket(args.host, 9090)
else:
	transport = TSocket.TSocket("localhost", 9090)

transport = TTransport.TBufferedTransport(transport)
protocol = TBinaryProtocol.TBinaryProtocol(transport)

client = Calculadora.Client(protocol)

transport.open()

print("hacemos ping al server")
client.ping()


if args.mode=="b":
	num1 = int(input('primer operando:	'));
	op = input('operacion + | - | * | / | r(raiz cuadrada):	');

	if op == 'r':
		try:
			resultado = client.raiz(num1);
			print("raiz(" + str(num1) + ") = " + str(resultado));
		except Exception:
			print("Math Error: negative square root");
	else: 
		num2 = int(input('segundo operando:	'));

		if op=='+':
			resultado = client.suma(num1, num2);
			print(str(num1) + "+" + str(num2) + " = "+ str(resultado));
		elif op =='-':
			resultado = client.resta(num1, num2);
			print(str(num1) + "-" + str(num2) + " = "+ str(resultado));
		elif op == '*':
			resultado = client.multiplicacion(num1, num2);
			print(str(num1) + "*" + str(num2) + " = "+ str(resultado));
		elif op == '/':
			try:
				resultado = client.division(num1, num2);
				print(str(num1) + "/" + str(num2) + " = "+ str(resultado));
			except Exception:
				print("Math Error: cannot divide by 0")
		else:
			print("Error: Invalidad Operator")

elif args.mode=='v':

	valid_size = False;
	while not valid_size:
		size_v  = int(input('tamaño vector(es):	'));
		if size_v >= 1 :
			valid_size = True;
		else:	
			print('Enter a positive (>=1) integer')

	v1 = [];
	for n in range(size_v):
		v1.append( float(input('v1 [' + str(n) + ']	= ')) );

	op = input('operacion mod(modulo) | norm(normalizar) | + | - :	');

	if op == 'mod':
		resultado = client.modulo(v1);
		print('modulo (' + str(v1) + ') = ' + str(resultado));
	elif op == 'norm':
		resultado = client.normalizar(v1);
		print('normalizar(' + str(v1) + ') = ' + str(resultado));
	elif op != '+' and op != '-':
		print("Error: Invalidad Operator")
	else:
		v2 = [];
		for n in range(size_v):
			v2.append( float(input('v2 [' + str(n) + ']	= ')) );

		if op=='+':
			resultado = client.suma_v(v1, v2);
			print(str(v1) + ' + ' + str(v2) + ' = ' + str(resultado));
		else:
			resultado = client.resta_v(v1, v2);
			print(str(v1) + ' - ' + str(v2) + ' = ' + str(resultado));
elif args.mode=='m':
	
	while True:
		rows  = int(input('numero de filas:	'));
		if rows >= 1 : 
			break
		else:	
			print('Enter a positive (>=1) integer')


	while True:
		cols = int(input('numero de columnas:	'));
		if cols >=1 : 
			break
		else:	
			print('Enter a positive (>=1) integer')


	m1 = []
	for r in range(rows):
		row_r = [];
		for c in range(cols):
			row_r.append( float(input('m1 [' + str(r) + '] ' + '[' + str(c) + '] = ') ) );
		m1.append(row_r);

	# print_m(m1);
	op = input('operacion + | - | * :	');
	if op!='+' and op!='-' and op!='*':
		print("Error: Invalidad Operator")
	else:

		while True:
			rows  = int(input('numero de filas:	'));
			if rows >= 1 : 
				break
			else:	
				print('Enter a positive (>=1) integer')


		while True:
			cols = int(input('numero de columnas:	'));
			if cols >=1 : 
				break
			else:	
				print('Enter a positive (>=1) integer')

		m2 = []
		for r in range(rows):
			row_r = [];
			for c in range(cols):
				row_r.append( float(input('m2 [' + str(r) + '] ' + '[' + str(c) + '] = ') ) );
			m2.append(row_r);

		if op=='+':
			try:
				resultado = client.suma_m(m1,m2)
				print('Resultado de m1 + m2 es...')
				print_m(resultado)
			except Exception:
				print('Math error: matrix must have the same size!!');
		elif op=='-':
			try:
				resultado = client.resta_m(m1,m2)
				print('Resultado de m1 - m2 es...')
				print_m(resultado)
			except Exception:
				print('Math error: matrix must have the same size!!');
		elif op=='*':			
			try:
				resultado = client.multiplicacion_m(m1,m2)
				print('Resultado de m1 * m2 es...')
				print_m(resultado)
			except Exception:
				print('Math error: m1 num columns == m2 num rows!!');
else:
	print("modo inválido: ver python3 cliente.py -h para modos de uso");

transport.close()
