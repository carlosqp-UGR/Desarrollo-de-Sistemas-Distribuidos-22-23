import glob
import sys

from calculadora import Calculadora

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

import logging

logging.basicConfig(level=logging.DEBUG)


class CalculadoraHandler:
    def __init__(self):
        self.log = {}

    def ping(self):
        print("me han hecho ping()")

    def suma(self, num1, num2):
        print("sumando " + str(num1) + " con " + str(num2))
        return num1 + num2

    def resta(self, num1, num2):
        print("restando " + str(num1) + " con " + str(num2))
        return num1 - num2

    def multiplicacion(self, num1, num2):
        print("multiplicando " + str(num1) + " con " + str(num2))
        return num1 * num2

    def division(self, num1, num2):
        print("dividiendo " + str(num1) + " con " + str(num2))
        try:
            return num1/num2
        except Exception:
            print("Math Error: cannot divide by 0")

    def raiz(self, num1):
        try:
            print("calculando raiz de " + str(num1))
            return pow(num1,1/2);
        except Exception:
            print("Math Error: negative square root");


    def modulo(self, v1):
        print("Calculando modulo de " + str(v1));

        sum = 0;
        for i in range(len(v1)):
            sum += v1[i]*v1[i];

        return pow(sum,1/2);


    def normalizar(self, v1):
        print("Normalizando " + str(v1));

        mod = self.modulo(v1);
        res = [];
        for i in range(len(v1)):
            res.append( v1[i]/mod );
        return res;


    def suma_v(self, v1, v2):
        if len(v1)!=len(v2):
            print('Math error: vectors must have the same size!!');
        else:
            print("Sumando v1" + str(v1) + " y v2" + str(v2));

            res = [];
            for i in range(len(v1)):
                res.append( v1[i] + v2[i] );
            return res;


    def resta_v(self, v1, v2):        
        if len(v1)!=len(v2):
            print('Math error: vectors must have the same size!!');
        else:
            print("Restando v1" + str(v1) + " y v2" + str(v2));

            res = [];
            for i in range(len(v1)):
                res.append( v1[i] - v2[i] );
            return res;


    def suma_m(self, m1, m2):
        if len(m1)!=len(m2) and len(m1[0])!=len(m2[0]):
            print('Math error: matrix must have the same size!!');
        else:
            print("Sumando m1" + str(m1) + " y m2" + str(m2));
            res = []
            for r in range(len(m1)):
                aux = []
                for c in range(len(m1[0])):
                    aux.append(m1[r][c] + m2[r][c])
                res.append( aux )
            return res


    def resta_m(self, m1, m2):        
        if len(m1)!=len(m2) and len(m1[0])!=len(m2[0]):
            print('Math error: matrix must have the same size!!');
        else:
            print("Restando m1" + str(m1) + " y m2" + str(m2));
            res = []
            for r in range(len(m1)):
                aux = []
                for c in range(len(m1[0])):
                    aux.append(m1[r][c] - m2[r][c])
                res.append( aux )
            return res


    def multiplicacion_m(self, m1, m2):
        if len(m1[0])!=len(m2):
            print('Math error: m1 num columns == m2 num rows');
        else:
            print("multiplicando m1" + str(m1) + " y m2" + str(m2));

            # Asignar espacio al producto e inicializar a 0 para mayor comodidad
            res = []
            for r in range(len(m1)):
                res.append([])
                for c in range(len(m2[0])):
                    res[r].append(0)

            for r in range(len(m1)):
                for c in range(len(m2[0])):
                    for k in range(len(m2)):
                        res[r][c] += (m1[r][k]*m2[k][c])

            return res


if __name__ == "__main__":
    handler = CalculadoraHandler()
    processor = Calculadora.Processor(handler)
    transport = TSocket.TServerSocket(host="127.0.0.1", port=9090)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print("iniciando servidor...")
    server.serve()
    print("fin")
