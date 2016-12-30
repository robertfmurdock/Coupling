var path = require('path');
var webpackConfig = require('../../../client/webpack.config');

const config = {
  module: webpackConfig.module,
  resolve: webpackConfig.resolve,
  devtool: 'inline-source-map'
};

module.exports = config;
