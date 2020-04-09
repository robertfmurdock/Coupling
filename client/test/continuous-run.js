const Server = require('karma').Server;


const server = new Server({port: 9876, configFile: __dirname + '/karma.conf.js'});

server.start();

const webpackRunner = require('../../test/webpackRunner');
const productionWebpackConfig = require('../webpack.config');

webpackRunner.watch(productionWebpackConfig, () => {
  console.log('Rebuild client module.');
});