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
config.target = 'node';
config.externals = [nodeExternals()];


function testResolve() {
  let resolve = clone(config.resolve);
  resolve.modules = [
    path.resolve(__dirname, '../../../test-style/build/classes/kotlin/js/main'),
    path.resolve(__dirname, '../../../commonKt/build/classes/kotlin/js/main'),
    path.resolve(__dirname, '../../../commonKt/build/classes/kotlin/js/test'),
    path.resolve(__dirname, '../../../engine/build/classes/kotlin/js/main'),
    path.resolve(__dirname, '../../../engine/build/classes/kotlin/js/test'),
  ];

  return resolve;
}
config.resolve = testResolve();

module.exports = config;
