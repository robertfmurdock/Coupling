var config = require('../../server/webpack.config');
var path = require('path');

var jsPath = path.resolve(__dirname, './');
var nodeExternals = require('webpack-node-externals');

config.entry = {
  config: path.resolve(jsPath, './protractor-conf.js'),
  test: path.resolve(jsPath, './test.js')
};

config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.target = 'node';
config.externals = [nodeExternals()];
module.exports = config;
