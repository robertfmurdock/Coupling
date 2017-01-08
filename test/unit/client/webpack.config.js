var path = require('path');
var _ = require('underscore');
var webpackConfig = _.clone(require('../../../client/webpack.config'));

const config = {
  module: webpackConfig.module,
  resolve: webpackConfig.resolve,
  devtool: 'inline-source-map'
};

module.exports = config;
