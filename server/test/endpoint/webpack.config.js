const clone = require('ramda/src/clone');

var config = clone(require('../../webpack.config'));
var path = require('path');
const nodeExternals = require('webpack-node-externals');

var jsPath = path.resolve(__dirname, './');

config.entry = path.resolve(jsPath, './test.js');
config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: 'test.js',
  libraryTarget: 'commonjs'
};
config.mode = "development";
config.target = 'node';
config.externals = [nodeExternals()];

module.exports = config;
