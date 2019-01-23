const clone = require('ramda/src/clone');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const webpackConfig = clone(require('../webpack.config'));
const path = require('path');

webpackConfig.externals.jquery = 'jQuery';

function testResolve() {
  let resolve = clone(webpackConfig.resolve);
  resolve.modules = [
    path.resolve(__dirname, '../../commonKt/build/classes/kotlin/js/main'),
    path.resolve(__dirname, '../../commonKt/build/classes/kotlin/js/test'),
    path.resolve(__dirname, '../../engine/build/classes/kotlin/js/main'),
    path.resolve(__dirname, '../../engine/build/classes/kotlin/js/test'),
    path.resolve(__dirname, '../node_modules')
  ];

  return resolve;
}

const config = {
  module: webpackConfig.module,
  resolve: testResolve(),
  externals: webpackConfig.externals,
  mode: "development",
  plugins: [
    new ExtractTextPlugin({filename: './styles.css', allChunks: true}),
    new webpack.DllReferencePlugin({
      context: '.',
      manifest: require('../build/lib/vendor/vendor-manifest.json')
    }),
    new webpack.ProvidePlugin({'window.jQuery': 'jquery', $: 'jquery', 'jQuery': 'jquery'})
  ],
  devtool: 'inline-source-map'
};

module.exports = config;
