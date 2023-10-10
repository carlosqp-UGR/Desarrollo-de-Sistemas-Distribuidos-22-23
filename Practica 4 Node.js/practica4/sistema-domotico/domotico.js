// Informacion relativa a la BD: 
// use pruebaBaseDatos
// Hay que comprobar si la coleccion existe; si no se debe crear:  db.createCollection('domotico');
// Para eliminar los datos de la coleccion: db.domotico.remove({});

var http = require("http");
var url = require("url");
var fs = require("fs");
var path = require("path");
var socketio = require("socket.io");

var MongoClient = require('mongodb').MongoClient;
var MongoServer = require('mongodb').Server;
var mimeTypes = { "html": "text/html", "jpeg": "image/jpeg", "jpg": "image/jpeg", "png": "image/png", "js": "text/javascript", "css": "text/css", "swf": "application/x-shockwave-flash"};

var httpServer = http.createServer(
	function(request, response) {
        // Procesamos la peticion de la pagina
		var uri = url.parse(request.url).pathname;
		if (uri=="/cliente") uri = "/cliente.html";
        else if (uri=="/sensores") uri = "/sensores.html";
		var fname = path.join(process.cwd(), uri);
		fs.exists(fname, function(exists) {
			if (exists) {
				fs.readFile(fname, function(err, data){
					if (!err) {
						var extension = path.extname(fname).split(".")[1];
						var mimeType = mimeTypes[extension];
						response.writeHead(200, mimeType);
						response.write(data);
						response.end();
					}
					else {
						response.writeHead(200, {"Content-Type": "text/plain"});
						response.write('Error de lectura en el fichero: '+uri);
						response.end();
					}
				});
			}
			else{
				console.log("Peticion invalida: "+uri);
				response.writeHead(200, {"Content-Type": "text/plain"});
				response.write('404 Not Found\n');
				response.end();
			}
		});
	}
);

// Inicializar y modificar aquí variables y funciones globales
// 15º <= temperatura <= 35º
const minTemperatura = 15;
const maxTemperatura = 35;
var temperatura = 25;

// 50 lux <= luminosidad <= 250 lux
const minLuminosidad = 50;
const maxLuminosidad = 250;
var luminosidad = 100;

// Tratamiento de eventos escuchados con BD
MongoClient.connect("mongodb://127.0.0.1:27017", { useUnifiedTopology: true }, function(err, db) {
}).then((db) => {
	httpServer.listen(8080);
	var io = socketio(httpServer);

	var dbo = db.db("pruebaBaseDatos");
	// Comprueba si la coleccion domotico existe, y si no la crea
	dbo.listCollections({name:'domotico'})
	.next()
	.then((info) => {
		if(info) {
			// La colección ya existe
			console.log("La colección 'domotico' existe");
		} else {
			// Si no existe, la crea
			dbo.createCollection('test')
			.then((collection) => {
				console.log("Colección 'domotico' creada");
			}).catch((err)=> {
				console.log(err);
			})
		}
	}).catch((err)=> {
		console.log(err);
	});

	/// FUNCIONES DEL PROGRAMA
	// Funciones de subir/bajar sensores. Tras realizar la operacion, inserta el registro en la BD
	function subirAC() {
		temperatura += 2;
		insertarRegistro();
	}
	
	function bajarAC() {
		temperatura -= 2;
		insertarRegistro();
	}
	
	function subirPersiana() {
		luminosidad += 20;
		insertarRegistro();
	}
	
	function bajarPersiana() {
		luminosidad -= 20;
		insertarRegistro();
	}

	// Funcion que utiliza la base de datos;
	// Siempre que inserta nueva informacion en la bd envia un aviso de actualizar historial
	// Para ello debe obtener información de todos los registros de la coleccion domotico
	function insertarRegistro() {

		// Inserta el nuevo registro
		var data = {fecha:new Date(),temperatura:temperatura, luminosidad:luminosidad};
		dbo.collection('domotico').insertOne(data, {safe:true}).then((result) => {
			// console.log(data);
			console.log("Registrado nuevo cambio en los sensores: " + JSON.stringify(data));

		}).catch((err)=> {
			console.error(err);
		});

		// Vuelve a enviar todos los registros de la coleccion (actualizada)
		dbo.collection('domotico').find().toArray().then((results) => {
			io.sockets.emit('actualizarHistorial',results);
		}).catch((err) => {
			console.error(err)
		});
	}

	// Función que implementa la lógica del agente:
	// - Comprueba los umbrales de los sensores
	// - Crea y emite el aviso
	// - Modifica los sensores subiendo/bajando el motor(A/C o persiana) las veces necesarias hasta que quede en un umbral aceptable
	function agente() {

		// Crea el aviso
		var aviso = "[Temperatura: ";
		if(minTemperatura<=temperatura&&temperatura<=maxTemperatura) {
			aviso += "OK, ";
		} else {
			aviso += "ALERTA, ";
		}

		aviso += "Luminosidad: ";
		if(minLuminosidad<=luminosidad&&luminosidad<=maxLuminosidad) {
			aviso += "OK]";
		} else {
			aviso += "ALERTA]";
		}

		// Enviar el aviso del agente
		io.sockets.emit('avisosAgente', aviso);

		// El agente ejecuta su funcion (si es necesario)
		var agente_actua = false;

		// Para la luminosidad, el agente actua si es necesario
		if(luminosidad<minLuminosidad) {
			agente_actua = true;
			console.log("Agente dice: ¡Demasiada oscuridad! Debemos subir la persiana");
			while(luminosidad<minLuminosidad) {
				console.log("Agente dice: He subido la persiana");
				subirPersiana();
			}
		} else if (luminosidad>maxLuminosidad) {
			agente_actua = true;
			console.log("Agente dice: ¡Demasiada luz! Debemos bajar la persiana");
			while(luminosidad<minLuminosidad) {
				console.log("Agente dice: He bajado la persiana");
				bajarPersiana();
			}
		}

		// Para la temperatura, el agente no realiza ninguna accion (lo pone en el guión de practicas)
		// Si se quisiera ejecutar alguna accion, se deberia hacer lo mismo que con la luminosidad


		// Si el agente ha actuado, entonces reenvia los datos actualizados
		if(agente_actua) {
			// Sólo envía el cambio final, no los cambios intermedios
			io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
			// Vuelve a llamar a la funcion para generar nuevamente el mensaje y volver a emitirlo
			agente();
		}
	}

	io.sockets.on('connection',
		function(client) {
			console.log('El usuario ' + client.request.connection.remoteAddress + ' se ha conectado');

			// Inicialmente se le envía informacion de los sensores
			io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
			// Y se envía la informacion del historial
			dbo.collection('domotico').find().toArray().then((results) => {
				io.sockets.emit('actualizarHistorial',results);
			}).catch((err) => {
				console.error(err)
			});

			/// Admin
			client.on('cambiarTemperatura', function(data) {
				temperatura = parseInt(data.temperatura);
				// Envía la nueva temperatura
				io.sockets.emit('actualizarSensores', {temperatura: temperatura, luminosidad: luminosidad});
				// Inserta el cambio en la BD
				insertarRegistro();
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			client.on('cambiarLuminosidad', function(data) {
				luminosidad = parseInt(data.luminosidad);
				// Envía el valor del nuevo sensor
				io.sockets.emit('actualizarSensores', {temperatura: temperatura, luminosidad: luminosidad});
				// Inserta el cambio en la BD
				insertarRegistro();
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			// Cliente
			client.on('subirAC', function() {
				// Realiza la operacion (e inserta los cambios en la BD)
				subirAC();
				// Envía los valores actualizados a los clientes
				io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			client.on('bajarAC', function() {
				// Realiza la operacion (e inserta los cambios en la BD)
				bajarAC();
				// Envía los valores actualizados a los clientes
				io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			client.on('subirPersiana', function() {
				// Realiza la operacion (e inserta los cambios en la BD)
				subirPersiana();
				// Envía los valores actualizados a los clientes
				io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			client.on('bajarPersiana', function() {
				// Realiza la operacion (e inserta los cambios en la BD)
				bajarPersiana();
				// Envía los valores actualizados a los clientes
				io.sockets.emit('actualizarSensores',{temperatura: temperatura, luminosidad: luminosidad});
				// Llama al agente para que cree el aviso y modifique los valores si es necesario
				agente();
			});

			client.on('disconnect', function() {
				console.log('El usuario '+client.request.connection.remoteAddress+' se ha desconectado');
			});
		}
	);

}).catch((err) => {
	console.error(err)
});

console.log("Servicio Web + MongoDB + Socket.io iniciado");