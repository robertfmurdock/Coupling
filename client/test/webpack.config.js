const clone = require('ramda/src/clone');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const webpackConfig = clone(require('../webpack.config'));

webpackConfig.externals.jquery = 'jQuery';

const config = {
  module: webpackConfig.module,
  resolve: webpackConfig.resolve,
  externals: webpackConfig.externals,
  mode: "development",
  plugins: [
    new ExtractTextPlugin({filename: './styles.css', allChunks: true}),
    new webpack.DllReferencePlugin({
      context: '.',
      manifest: require('../../public/app/build/vendor/vendor-manifest.json')
    }),
    new webpack.ProvidePlugin({'window.jQuery': 'jquery', $: 'jquery', 'jQuery': 'jquery'})
  ],
  devtool: 'inline-source-map'
};

module.exports = config;
