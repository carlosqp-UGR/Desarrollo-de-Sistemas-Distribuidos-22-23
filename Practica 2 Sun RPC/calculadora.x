/* Archivo calculadora.x: Realiza la función de calculadora (operaciones básicas
 de suma, resta, multiplicación y división) de números reales. */ 

/* Estructura que almacena la operacion de entrada (num1 'operador' num2) */ 
struct inputs {
	int num1;
	int num2;
};

program CALCULADORAPROG {
	version CALCULADORAVER {
		int SUMA(inputs)=1;	/* Numero de procedimiento (único entre los que hay) */
		int RESTA(inputs)=2;
		int MULTIPLICACION(inputs)=3;
		float DIVISION(inputs)=4;
	} =1;	/* Numero de version */
} = 0x200000ff;	/* Numero de programa */

