var path = require('path');
const clone = require('ramda/src/clone');

const nodeExternals = require('webpack-node-externals');
var config = clone(require('../../../server/webpack.config'));

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
