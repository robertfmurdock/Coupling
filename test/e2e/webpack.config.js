const config = require('../../server/webpack.config');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

config.entry = {
  config: path.resolve(jsPath, './protractor-conf.ts'),
  test: path.resolve(jsPath, './test.js')
};

config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.target = 'node';
config.externals = [nodeExternals()];
config.mode = "development";
module.exports = config;
