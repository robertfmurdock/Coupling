var path = require('path');
const clone = require('ramda/src/clone');

const nodeExternals = require('webpack-node-externals');
var config = clone(require('../../webpack.config'));

var jsPath = path.resolve(__dirname, './');

config.entry = path.resolve(jsPath, './test.js');
config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: 'test.js',
  libraryTarget: 'commonjs'
};
config.mode = "development";
config.devtool = "inline-source-map";
config.target = 'node';
config.externals = [nodeExternals(), nodeExternals({modulesDir: path.resolve(__dirname, '../build/js/node_modules')}),];

function testResolve() {
  let resolve = clone(config.resolve);
  resolve.modules = [
    path.resolve(__dirname, '../../build/classes/kotlin/main'),
    path.resolve(__dirname, '../../build/classes/kotlin/test'),
    path.resolve(__dirname, '../../../build/js/node_modules')
  ];

  return resolve;
}

config.resolve = testResolve();

module.exports = config;
