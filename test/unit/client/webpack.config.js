const _ = require('underscore');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const webpackConfig = _.clone(require('../../../client/webpack.config'));

const config = {
  module: webpackConfig.module,
  resolve: webpackConfig.resolve,
  externals: webpackConfig.externals,
  plugins: [
    new ExtractTextPlugin({filename: './styles.css', allChunks: true}),
    new webpack.ProvidePlugin({'window.jQuery': 'jquery', $: 'jquery', 'jQuery': 'jquery'})
  ],
  devtool: 'inline-source-map'
};

module.exports = config;
