const Server = require('karma').Server;


const server = new Server({port: 9876, configFile: __dirname + '/karma.conf.js'});

server.start();