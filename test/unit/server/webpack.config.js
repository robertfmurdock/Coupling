var config = require('../../../server/webpack.config');
var path = require('path');

var jsPath = path.resolve(__dirname, './');

config.entry = path.resolve(jsPath, './test.js');
config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: 'test.js',
  libraryTarget: 'commonjs'
};

module.exports = config;
