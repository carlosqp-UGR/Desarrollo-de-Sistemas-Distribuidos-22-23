service Calculadora{
	void ping(),
	i32 suma(1:i32 num1, 2:i32 num2),
	i32 resta(1:i32 num1, 2:i32 num2),
	i32 multiplicacion(1:i32 num1, 2:i32 num2),
	double division(1:i32 num1, 2:i32 num2),
	double raiz(1:i32 num1),
	double modulo(1:list<double> v1),
	list<double> normalizar(1:list<double> v1),
	list<double> suma_v(1:list<double> v1, 2:list<double> v2),
	list<double> resta_v(1:list<double> v1, 2:list<double> v2),
	list<list<double>> suma_m(1:list<list<double>> m1, 2:list<list<double>> m2),
	list<list<double>> resta_m(1:list<list<double>> m1, 2:list<list<double>> m2),
	list<list<double>> multiplicacion_m(1:list<list<double>> m1, 2:list<list<double>> m2)
}
