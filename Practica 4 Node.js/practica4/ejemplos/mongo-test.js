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
		var uri = url.parse(request.url).pathname;
		if (uri=="/") uri = "/mongo-test.html";
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


MongoClient.connect("mongodb://127.0.0.1:27017", { useUnifiedTopology: true }, function(err, db) {
}).then((db) => {
	httpServer.listen(8080);
	var io = socketio(httpServer);

	var dbo = db.db("pruebaBaseDatos");
	// Comprueba si existe la coleccion en la base de datos
	dbo.listCollections({name:'test'})
	.next()
	.then((info) => {
		if(info) {
			// La colección ya existe
			console.log("La colección 'test' existe");
		} else {
			// Si no existe, la crea
			dbo.createCollection('test')
			.then((collection) => {
				console.log("Colección 'test' creada");
			}).catch((err)=> {
				console.log(err);
			})
		}
	}).catch((err)=> {
		console.log(err);
	})

	io.sockets.on('connection',
		function(client) {

			client.emit('my-address', {host:client.request.connection.remoteAddress, port:client.request.connection.remotePort});
			
			client.on('poner',  function(data) {
				dbo.collection('test').insertOne(data, {safe:true}).then((result) => {
					console.log(data)
				}).catch((err) => {
					console.error(err)
				});
			});

			client.on('obtener', function(data) {
				dbo.collection('test').find(data).toArray().then((results) => {
					client.emit('obtener', results);
				}).catch((err) => {
					console.error(err)
				});
			});
	});

}).catch((err)=> {
	console.error(err);
});

console.log("Servicio MongoDB iniciado");

